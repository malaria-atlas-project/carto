package uk.ac.ox.map.carto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gtk.Gtk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.AdminUnitRisk;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.Exclusion;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.LegendItem;
import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.raster.WMSRaster;
import uk.ac.ox.map.carto.style.Colour;
import uk.ac.ox.map.carto.style.DummyRenderScale;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.text.MapTextResource;
import uk.ac.ox.map.carto.util.StringUtil;
import uk.ac.ox.map.carto.util.SystemUtil;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class LimitsMaps {

	static final int w = 500;
	static final int h = 707;
	static final Logger logger = LoggerFactory.getLogger(LimitsMaps.class);
	static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(), 0.2);
	static final File tempLimsLoc = new File("/home/will/map1_srv/data/mastergrids/cleandata/tempsuitability/pf/");
	static final File aridityMaskFilePath = new File("/home/will/map1_srv/data/mastergrids/cleandata/Globcover/classmasks/200_bare_areas/");
	static final AdminUnitService adminUnitService = new AdminUnitService();

	public static void main(String[] args) throws IOException, InterruptedException {
		Gtk.init(null);

		String countryId = "BWA";
		Country country = adminUnitService.getCountry(countryId);
//		drawMap(adminUnitService, country, "pf");
//		drawMap(adminUnitService, country, "pv");

//		 drawMap(adminUnitService, "pf");
		 drawMap("pv");
	}

	@SuppressWarnings("unused")
	private static void drawMap(String parasite) throws IOException, InterruptedException {
		List<Country> pfCountries = adminUnitService.getCountries(parasite, true);
		for (Country country : pfCountries) {

			logger.debug("Processing country: {}, {}", country.getId(), country.getName());

			if (country.getId().equals("CHN"))
				continue;
			File f = new File(String.format("/home/will/maps/pdf/%s_%s.pdf", country.getId(), parasite));
			if (f.exists())
				continue;
			drawMap(adminUnitService, country, parasite);
		}
	}

	public static void drawMap(AdminUnitService adminUnitService, Country country, String parasite) throws IOException, InterruptedException {

		// Legend items
		List<LegendItem> legend = new ArrayList<LegendItem>();

		/*
		 * Get country then use extent to: 1. Create map canvas 2. Get
		 * appropriate admin units
		 */

		Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());

		/*
		 * Create dataframe with specified envelope
		 */
		DataFrame df = new DataFrame.Builder(env, new Rectangle(20, 40, 460, 460), null).build();
		df.setBackgroundColour(Palette.WATER.get());

		/*
		 * Get envelope as resized by dataframe Get admin units (level 0)
		 * overlapping data frame and draw them
		 */
		Envelope resizedEnv = df.getEnvelope();
		List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);

		FillStyle greyFS = new FillStyle(Palette.GREY_20.get());
		List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
		for (AdminUnit admin0 : adminUnits) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, defaultLineStyle);
			adminPolySym.add(ps);
		}
		df.drawFeatures(adminPolySym);

		/*
		 * Draw the risk units
		 */
		HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
		colours.put(0, Palette.WHITE.get());
		colours.put(1, Palette.ROSE.get());
		colours.put(2, Palette.CORAL.get());

		/*
		 * TODO: possible to obtain a nice feature class, colours included,
		 * direct from the hibernate service?
		 */

		/*
		 * Get risk admin units Check if we have no data
		 */
		boolean noDataPresent = false;
		List<PolygonSymbolizer> pfFeats = new ArrayList<PolygonSymbolizer>();

		List<AdminUnitRisk> riskUnits = adminUnitService.getRiskAdminUnits(country, parasite);

		for (AdminUnitRisk adminUnitRisk : riskUnits) {
			FillStyle fs = new FillStyle(colours.get(adminUnitRisk.getRisk()), FillType.SOLID);
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) adminUnitRisk.getGeom(), fs, defaultLineStyle);
			pfFeats.add(ps);
			if (adminUnitRisk.getRisk() == 9)
				noDataPresent = true;
		}
		df.drawFeatures(pfFeats);

		/*
		 * Draw limits layer
		 */
		DummyRenderScale dr = new DummyRenderScale();
		dr.putColour(1, Palette.BLACK.get(0.5));
		dr.putColour(0, Palette.BLACK.get(0));

//		RasterLayer ras = FltReader.openFloatFile(aridityMaskFilePath, "gc1k_200", dr, df.getEnvelope());
		String wmsUrl = "http://map1.zoo.ox.ac.uk/geoserver/wms?LAYERS=Base:pvlims&STYLES=&SRS=EPSG:4326&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&EXCEPTIONS=application%2Fvnd.ogc.se_inimage";
		RasterLayer ras = new WMSRaster(resizedEnv, 0.00833333333, wmsUrl);
		df.addRasterLayer(ras);
		
//		df.drawPoint(ras.getOrigin().x+1, ras.getOrigin().y+1, true);
		
		legend.add(new LegendItem("Aridity mask", Palette.BLACK.get(0.5)));

		DummyRenderScale dr2 = new DummyRenderScale();
		dr2.putColour(1, Palette.GREY_30.get(0.5));
		dr2.putColour(0, Palette.BLACK.get(0));
//		RasterLayer ras2 = FltReader.openFloatFile(tempLimsLoc, "templimspf_CLEAN", dr2, df.getEnvelope());
//		df.addRasterLayer(ras2);
		legend.add(new LegendItem("Limits", Palette.GREY_30.get(0.5)));
		

		/*
		 * Get excluded cities TODO: get enum type for exclusions
		 */
		List<PolygonSymbolizer> exclAreas = new ArrayList<PolygonSymbolizer>();

		List<Exclusion> exclusions = adminUnitService.getExclusions(country);
		Colour exclColour = Palette.WHITE.get();

		List<String> exclCities = new ArrayList<String>();
		List<String> exclIslands = new ArrayList<String>();
		List<String> exclAdminUnits = new ArrayList<String>();
		List<String> exclMoh = new ArrayList<String>();
		List<String> exclMedIntel = new ArrayList<String>();

		for (Exclusion exclusion : exclusions) {
			logger.info("Exclusion {}", exclusion.getExclusionType());
			if (exclusion.getExclusionType().equals("urban area"))
				exclCities.add(exclusion.getName());
			else if (exclusion.getExclusionType().equals("island"))
				exclIslands.add(exclusion.getName());
			else if (exclusion.getExclusionType().equals("ithg admin exclusion"))
				exclAdminUnits.add(exclusion.getName());
			else if (exclusion.getExclusionType().equals("moh exclusion"))
				exclMoh.add(exclusion.getName());
			else if (exclusion.getExclusionType().equals("med intel"))
				exclMedIntel.add(exclusion.getName());
			else
				throw new IllegalArgumentException("Unknown exclusion type: " + exclusion.getExclusionType());

			// MOH hack... damn exclusions
			FillStyle fsHatched = new FillStyle(Palette.WHITE.get(), FillType.HATCHED);
			FillStyle fsStipple = new FillStyle(Palette.WHITE.get(), FillType.STIPPLED);

			PolygonSymbolizer ps;
			if (exclusion.getExclusionType().equals("med intel")) {
				ps = new PolygonSymbolizer((MultiPolygon) exclusion.getGeom(), fsHatched, defaultLineStyle);
				exclAreas.add(ps);
			} else if (exclusion.getExclusionType().equals("moh exclusion")) {
				ps = new PolygonSymbolizer((MultiPolygon) exclusion.getGeom(), fsStipple, defaultLineStyle);
				exclAreas.add(ps);
			} else {
				ps = new PolygonSymbolizer((MultiPolygon) exclusion.getGeom(), fsHatched, defaultLineStyle);
				exclAreas.add(ps);
			}
		}
		/*
		 * Draw excluded areas
		 */
		df.drawFeatures(exclAreas);

		/*
		 * Draw on water bodies
		 */
		List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
		List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
		Colour waterColour = Palette.WATER.get();
		FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
		for (WaterBody waterBody : water) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) waterBody.getGeom(), fs, defaultLineStyle);
			waterBodies.add(ps);
		}
		// df.drawFeatures(waterBodies);

		/*
		 * Draw the rest of the canvas
		 */
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.addDataFrame(df);
		mapCanvas.drawDataFrames();

		Rectangle frame = new Rectangle(20, 530, 430, 20);
		mapCanvas.setScaleBar(frame, df.getScale(), 7);

		/*
		 * Draw the legend TODO: combine this and the styling in a layer
		 */
		legend.add(new LegendItem("Water", waterColour));
		legend.add(new LegendItem("Malaria free", colours.get(0)));

		/*
		 * Pv/Pv risk legend items
		 */
		String apiLegendStr;
		if (parasite.equals("pf"))
			apiLegendStr = "Pf";
		else
			apiLegendStr = "Pv";
		legend.add(new LegendItem(String.format("<i>%s</i>API &lt; 0.1‰", apiLegendStr), colours.get(1)));
		legend.add(new LegendItem(String.format("<i>Pf</i>API ≥ 0.1‰", apiLegendStr), colours.get(2)));

		LegendItem ithgLi = new LegendItem("ITHG exclusion", exclColour);
		LegendItem mohLi = new LegendItem("MoH exclusion", exclColour);
		LegendItem medLi = new LegendItem("Med intel exclusion", exclColour);

		// TODO: hatch hack
		ithgLi.hatched = true;
		mohLi.stippled = true;
		medLi.stippled = true;
		if (exclCities.size() > 0 || exclAdminUnits.size() > 0)
			legend.add(ithgLi);
		if (exclMedIntel.size() > 0)
			legend.add(medLi);
		if (exclMoh.size() > 0)
			legend.add(mohLi);
		// TODO: nodata shouldn't exist
		if (noDataPresent)
			legend.add(new LegendItem("No data", colours.get(9)));

		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawLegend(legendFrame, legend);

		/*
		 * Text stuff TODO: factor all this string building out
		 */
		MapTextResource mtr = new MapTextResource();
		Rectangle titleFrame = new Rectangle(0, 5, 500, 0);

		String mapTitle = null;
		if (parasite.compareTo("pf") == 0)
			mapTitle = String.format((String) mtr.getObject("pfTitle"), country.getName());
		else if (parasite.compareTo("pv") == 0)
			mapTitle = String.format((String) mtr.getObject("pvTitle"), country.getName());

		mapCanvas.setTitle(mapTitle, titleFrame, 11);

		/*
		 * Main map text
		 */

		List<Object[]> levelCounts = adminUnitService.getAdminLevels2(country, parasite);
		List<String> mapTextItems = new ArrayList<String>();

		List<Integer> years = adminUnitService.getYears(country, parasite);
		List<String> intelCountries = Arrays.asList(new String[] { "CPV", });

		if (intelCountries.contains(country.getId())) {
			mapTextItems.add((String) mtr.getObject("apiTextNationalMedIntel"));

		} else if (levelCounts.get(0)[0].equals("Admin0") && levelCounts.size() < 2) {
			mapTextItems.add((String) mtr.getObject("apiTextNoInfo"));
		} else {
			mapTextItems.add(StringUtil.formatAdminString1((String) mtr.getObject("apiText"), levelCounts, years));
		}

		List<String> modifiedRiskCountries = Arrays.asList(new String[] { "MMR", "IND" });
		String ithgText = "";
		if (modifiedRiskCountries.contains(country.getId())) {
			ithgText = (String) mtr.getObject("ithgText2");
		} else {
			ithgText = (String) mtr.getObject("ithgText1");
		}
		String formatString = "the following %s: %s";
		if (exclAdminUnits.size() > 0 || exclCities.size() > 0 || exclIslands.size() > 0) {

			List<String> txt = new ArrayList<String>();
			if (exclAdminUnits.size() > 0) {
				txt.add(StringUtil.formatPlaceName(formatString, "administrative unit", "administrative units", exclAdminUnits));
			}
			if (exclCities.size() > 0) {
				txt.add(StringUtil.formatPlaceName(formatString, "city", "cities", exclCities));
			}
			if (exclIslands.size() > 0) {
				txt.add(StringUtil.formatPlaceName(formatString, "island", "islands", exclIslands));
			}
			mapTextItems.add(String.format(ithgText, StringUtil.getReadableList(txt)));
		}

		if (exclMoh.size() > 0) {
			String mohText = (String) mtr.getObject("mohText");
			mapTextItems.add(String.format(mohText, StringUtil.formatPlaceName(formatString, "administrative unit", "administrative units", exclMoh)));
		}
		if (exclMedIntel.size() > 0) {
			String medIntelText = (String) mtr.getObject("medintelText");
			mapTextItems.add(String.format(medIntelText, StringUtil.formatPlaceName(formatString, "administrative unit", "administrative units", exclMedIntel)));
		}

		mapTextItems.add((String) mtr.getObject("copyright"));
		Rectangle mapTextFrame = new Rectangle(50, 555, 320, 0);
		mapCanvas.drawTextFrame(mapTextItems, mapTextFrame, 6, 10);
		mapSurface.finish();

		SystemUtil.addBranding(country.getId(), parasite, "portrait");
	}

}
