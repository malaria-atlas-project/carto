package uk.ac.ox.map.carto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gdk.PixbufFormat;
import org.gnome.gtk.Gtk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.AdminUnitRisk;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.CountryPopulation;
import uk.ac.ox.map.base.model.adminunit.Exclusion;
import uk.ac.ox.map.base.model.adminunit.MAPRegion;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.carto.Duffy;
import uk.ac.ox.map.base.model.pr.PRPoint;
import uk.ac.ox.map.base.model.pr.Parasite;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
import uk.ac.ox.map.base.service.PRService;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.MapKeyItem;
import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.layer.LayerFactory;
import uk.ac.ox.map.carto.raster.WMSRaster;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.text.MapTextFactory;
import uk.ac.ox.map.carto.text.MapTextResource;
import uk.ac.ox.map.carto.util.GeometryUtil;
import uk.ac.ox.map.carto.util.OutputUtil;
import uk.ac.ox.map.carto.util.OverlayUtil;
import uk.ac.ox.map.carto.util.StringUtil;
import uk.ac.ox.map.carto.util.SystemUtil;
import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.h5.H5FloatRaster;
import uk.ac.ox.map.h5.H5IntRaster;
import uk.ac.ox.map.h5.H5RasterFactory;
import uk.ac.ox.map.imageio.ColourMap;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

import freemarker.template.Template;

public class CountryLevelMaps {
  

	static final Logger logger = LoggerFactory.getLogger(CountryLevelMaps.class);
	static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(), 0.2);
	
	static final PRService prService = new PRService();
	static final AdminUnitService adminUnitService = new AdminUnitService();
	
	static final CartoService cartoService = new CartoService();
	private static LayerFactory layerFactory = new LayerFactory();
	
	private static final MapTextFactory mapTextFactory = new MapTextFactory();
	
	public static void main(String[] args) throws Exception {
		
		Gtk.init(null);
		AdminUnitService adminUnitService = new AdminUnitService();
		H5RasterFactory h5RFpop = new H5RasterFactory("/home/will/workspace/ImageIO/pop10.h5");
		H5RasterFactory h5RFpfLims = new H5RasterFactory("/home/will/workspace/ImageBase/pf_limits.h5");
		H5RasterFactory h5RFpvLims = new H5RasterFactory("/home/will/workspace/ImageBase/pv_limits.h5");
		H5RasterFactory h5RFPfPR = new H5RasterFactory("/home/will/workspace/ImageBase/pf_pr.h5");
		H5RasterFactory h5RFPfStd = new H5RasterFactory("/home/will/workspace/ImageBase/pf_std.h5");
		
		Map<String, Rectangle> frameConfP = new HashMap<String, Rectangle>();
		frameConfP.put("dataFrame", new Rectangle(20, 40, 460, 460));
		frameConfP.put("scalebar", new Rectangle(20, 525, 430, 20));
		frameConfP.put("legendFrame", new Rectangle(365, 545, 170, 200));
		frameConfP.put("continuousScale", new Rectangle(390, 675, 100, 15));
		frameConfP.put("titleFrame", new Rectangle(0, 3, 500, 0));
		frameConfP.put("mapTextFrame", new Rectangle(50, 545, 300, 0));
		
		Map<String, Rectangle> frameConfLS = new HashMap<String, Rectangle>();
		frameConfLS.put("dataFrame", new Rectangle(20, 40, 667, 330));
		frameConfLS.put("scalebar", new Rectangle(20, 390, 637, 20));
		frameConfLS.put("legendFrame", new Rectangle(480, 410, 120, 200));
		frameConfLS.put("continuousScale", new Rectangle(587, 440, 100, 15));
		frameConfLS.put("titleFrame", new Rectangle(10, 3, 687, 0));
		frameConfLS.put("mapTextFrame", new Rectangle(50, 410, 420, 0));
		
		List<Country> countries = adminUnitService.getMECountries();
		
		Parasite currentParasite = Parasite.Pf;
		boolean drawRegMaps = false;
		if(drawRegMaps) {
//				drawLimitsMap(c, Parasite.Pf, h5RFpfOld, frameConfP, 500, 707, false, false);
			drawLimitsMapRegion("North Africa Globcover", currentParasite, h5RFpfLims, frameConfP, 500, 707, false, false);
			drawLimitsMapRegion("Southern Africa Globcover", currentParasite, h5RFpfLims, frameConfP, 500, 707, false, false);
//			drawLimitsMapRegion("North Africa", currentParasite, h5RFpfOld, frameConfP, 500, 707, false, false);
//			drawLimitsMapRegion("Southern Africa", currentParasite, h5RFpfOld, frameConfP, 500, 707, false, false);
		}
		
		List<String> duffyCountryIds = adminUnitService.getDuffyCountryIds();
		List<String> toProcess = new ArrayList<String>();
		toProcess.add("LKA");
//		toProcess.add("THA");
//		toProcess.add("VNM");
//		toProcess.add("MMR");
//		toProcess.add("KHM");
//		toProcess.add("LAO");
//		toProcess.add("CHN");
		
		for (Country c : countries) {
			logger.debug(c.getId());
			if (!toProcess.contains(c.getId())) {
			  continue;
			}
//			if(!(c.getId().equals("NAM") || c.getId().equals("DJI") || c.getId().equals("SWZ")|| c.getId().equals("BTN")))
//				continue;
			
			if (c.getPfEndemic()) {
				drawPRMap(c, Parasite.Pf, frameConfP, 500, 707, h5RFPfPR, h5RFpfLims);
//				drawAPIMap(c, Parasite.Pf, frameConfP, 500, 707);
//				drawLimitsMap(c, Parasite.Pf, h5RFpfLims, frameConfP, 500, 707, false, false);
			}
			
			if (c.getPvEndemic()) {
//				drawLimitsMap(c, Parasite.Pv, h5RFpvLims, frameConfP, 500, 707, false, duffyCountryIds.contains(c.getId()));
//				drawAPIMap(c, Parasite.Pv, frameConfP, 500, 707);
			}
		}
		
//			drawRegionPopMap(adminUnitService, "Africa+", frameConfP, templ, 500, 707);
//			drawRegionPopMap(adminUnitService, "CSE Asia", frameConfLS, templ, 707, 505);
//			drawRegionPopMap(adminUnitService, "Americas", frameConfP, templ, 500, 707);
	}

	
	public static void drawAPIMap(Country country, Parasite parasite, Map<String, Rectangle> frameConf, int w, int h) throws OutOfMemoryError, Exception {
	
			// Legend items
			List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
	
			/*
			 * Get country then use extent to: 1. Create map canvas 2. Get
			 * appropriate admin units
			 */
			Envelope env; env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY()); 
			env.expandBy(env.getWidth()/20);
	
			/*
			 * Create dataframe with specified envelope
			 */
			DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null).build();
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
			List<PolygonSymbolizer> waterBodies = layerFactory.getPolygonLayer(
				adminUnitService.getWaterBodies(resizedEnv),
				new FillStyle(Palette.WATER.get(), FillType.SOLID),	
				new LineStyle(Palette.WATER.get(), 0.2)
			);
			df.drawFeatures(waterBodies);
	
			/*
			 * Draw the rest of the canvas
			 */
			PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
			MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
			mapCanvas.addDataFrame(df);
			mapCanvas.drawDataFrames();
	
			mapCanvas.setScaleBar(frameConf.get("scalebar"), df.getScale(), 7);
	
			/*
			 * Draw the legend TODO: combine this and the styling in a layer
			 */
			legend.add(new MapKeyItem("Water", Palette.WATER.get()));
			legend.add(new MapKeyItem("Malaria free", colours.get(0)));
	
			/*
			 * Pv/Pv risk legend items
			 */
			legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite.toString()), colours.get(1)));
			legend.add(new MapKeyItem(String.format("<i>%s</i>API ≥ 0.1‰", parasite.toString()), colours.get(2)));
	
			MapKeyItem ithgLi = new MapKeyItem("ITHG exclusion", exclColour);
			MapKeyItem mohLi = new MapKeyItem("MoH exclusion", exclColour);
			MapKeyItem medLi = new MapKeyItem("Med intel exclusion", exclColour);
	
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
				legend.add(new MapKeyItem("No data", colours.get(9)));
	
			mapCanvas.drawKey(frameConf.get("legendFrame"), legend, 6);
	
			/*
			 * Text stuff TODO: factor all this string building out
			 */
			MapTextResource mtr = new MapTextResource();
			
			String mapTitle = "<i>%s</i> risk derived from medical intelligence in %s";
			mapTitle = String.format(mapTitle, parasite.getSpecies(), country.getName());
			mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 9.5);
	
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
			StringBuilder sb = new StringBuilder();
			for (String string : mapTextItems) {
				sb.append(string);
				sb.append("<br>");
			}
			mapCanvas.drawTextFrame(sb.toString(), frameConf.get("mapTextFrame"), 6, 10);
			mapSurface.finish();
	
			SystemUtil.addBranding(parasite.toString().toLowerCase()+"_api", country.getId(), parasite.toString() + "_med_intel", "branding_final");
		}


	public static void drawLimitsMap(Country country, Parasite parasite, H5RasterFactory h5Fact, Map<String, Rectangle> frameConf, int w, int h, boolean prPointsRequired, boolean duffyRequired) throws OutOfMemoryError, Exception {
	
		
		// Legend items
		List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
		HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
		colours.put(0, Palette.GREY_20.get());
		colours.put(1, Palette.GREY_40.get());
		colours.put(2, Palette.GREY_60.get());
		
		/*
		 * 1. Draw limits raster
		 * 2. Draw mask (countries)
		 * 3. Draw PR points
		 */
	
		/*
		 * Get country then use extent to create map canvas and get appropriate admin units.
		 */
		Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
		//Expand by 5%
		env.expandBy(env.getWidth()/20);
		
		/*
		 * Create dataframe with specified envelope
		 */
		DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null).build();
		df.setBackgroundColour(Palette.WATER.get());
		
		/*
		 * Get envelope as resized by dataframe 
		 */
		Envelope resizedEnv = df.getEnvelope();
		List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);
		
		/*
		 * Draw limits layer
		 */
		ColourMap cm = new ColourMap();
		cm.addColour(0, colours.get(0));
		cm.addColour(1, colours.get(1));
		cm.addColour(2, colours.get(2));
		
		cm.finish();
		H5IntRaster.Transform tm= new H5IntRaster.Transform() {
			@Override
			public int transform(int inVal) {
				return inVal;
			}
		};
		
		H5IntRaster h5r = h5Fact.getRaster(resizedEnv, tm, cm, 1);
		h5r.getPixbuf().save("/tmp/pixbuftest.png", PixbufFormat.PNG);
		df.addRasterLayer(h5r);
		
		/*
		 * Duffy mask
		 */
		if (duffyRequired) {
			Duffy duf = cartoService.getDuffy();
			FillStyle fsDuffy = new FillStyle(Palette.WHITE.get(0), FillType.DUFFY);
			PolygonSymbolizer dufPs = new PolygonSymbolizer((MultiPolygon) duf.getGeom(), fsDuffy, new LineStyle(Palette.WHITE.get(0), 0));
			df.drawMultiPolygon(dufPs);
		}
		
		/* Get admin units (level 0)
		 * overlapping data frame and draw them, avoiding country in question
		 * to produce a mask
		 */
		FillStyle greyFS = new FillStyle(Palette.WHITE.get());
		List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
		for (AdminUnit admin0 : adminUnits) {
			if (admin0.getCountryId().equals(country.getId())) {
				continue;
			}
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, defaultLineStyle);
			adminPolySym.add(ps);
		}
		df.drawFeatures(adminPolySym);
	
		/*
		 * Draw on water bodies
		 */
		List<PolygonSymbolizer> waterBodies = layerFactory.getPolygonLayer(
			adminUnitService.getWaterBodies(resizedEnv),
			new FillStyle(Palette.WATER.get(), FillType.SOLID),	
			new LineStyle(Palette.WATER.get(), 0.2)
		);
		df.drawFeatures(waterBodies);
		
		/*
		WorldInverse wi = adminUnitService.getWorldInverse();
		df.drawMultiPolygon(
			new PolygonSymbolizer(
				(MultiPolygon) wi.getGeom(), 
				new FillStyle(Palette.WATER.get(), FillType.SOLID),	
				new LineStyle(Palette.WATER.get(), 0.2)
			)
		);
		*/
		
		/*
		 * Draw on points
		 */
		if (prPointsRequired) {
			List<PRPoint> pvPoints = prService.getPoints(country.getId(), parasite);
			for (PRPoint pvPoint : pvPoints) {
				df.drawPoint(pvPoint.getLongitude(), pvPoint.getLatitude(), pvPoint.getColour());
			}
		}
	
		/*
		 * Draw the rest of the canvas
		 */
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.addDataFrame(df);
		mapCanvas.drawDataFrames();
		mapCanvas.setScaleBar(frameConf.get("scalebar"), df.getScale(), 7);
	
		/*
		 * Draw the legend TODO: combine this and the styling in a layer
		 */
		legend.add(new MapKeyItem("Water", Palette.WATER.get()));
		legend.add(new MapKeyItem("Malaria free", Palette.GREY_20.get()));
	
		legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite), colours.get(1)));
		legend.add(new MapKeyItem(String.format("<i>%s</i>API ≥ 0.1‰", parasite), colours.get(2)));
		
		/*
		 * Add duffy if required
		 */
		if (duffyRequired) {
			MapKeyItem li = new MapKeyItem("Duffy negativity ≥ 90%", Palette.BLACK.get(0));
			li.duffy = true;
			legend.add(li);
		}
	
		mapCanvas.drawKey(frameConf.get("legendFrame"), legend, 6);
	
		if(prPointsRequired) {
			ContinuousScale cs = new ContinuousScale(frameConf.get("continuousScale"), "Probability", "(in units of <i>" + parasite + "</i>PR<sub><small>2-10</small></sub>, 0-100%)");
			cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0, false);
			cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0, false);
			cs.finish();
			cs.draw(mapCanvas);
		}
		
		/*
		 * Title
		 */
		String mapTitle = "<i>%s</i> risk including biological masks in %s";
		mapTitle = String.format(mapTitle, parasite.getSpecies(), country.getName());
		mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);
		
		/*
		 * Map text
		 */
		
		Map<String, Object> x = prService.getSurveyMetadata(country.getId(), parasite);
		Integer startYear = (Integer) x.get("min");
		Integer endYear = (Integer) x.get("max");
		Long nSurveys = (Long) x.get("n");
		Map<String, Object> context = new HashMap<String, Object>();
		
		if (startYear != null) {
			context.put("yearStart", startYear.toString());
		}
		if (endYear != null) {
			context.put("yearEnd", endYear.toString());
		}
		
		context.put("duffyRequired", duffyRequired);
		context.put("nSurveys", nSurveys);
		//TODO: hack to avoid putting text on
		if (!prPointsRequired) {
			context.put("nSurveys", 0);
		}
		
		context.put("parasiteAbbr", parasite.getSpeciesAbbr());
		context.put("parasite", parasite);
		
		String mapText = mapTextFactory.processTemplate(context, "limitsText.ftl");
		mapCanvas.drawTextFrame(mapText, frameConf.get("mapTextFrame"), 6, 10);
		mapSurface.finish();
	
		SystemUtil.addBranding(parasite.toString().toLowerCase()+"_limits", country.getId(), parasite.toString() + "_limits", "branding_final");
	}


	public static void drawPRMap(Country country, Parasite parasite, Map<String, Rectangle> frameConf, int w, int h, H5RasterFactory h5RFPr, H5RasterFactory h5RFPfLims) throws Exception {
		
		/*
		 * Get country then use extent to create map canvas and get appropriate admin units.
		 */
		Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
		if (country.getId().equals("ARG")) {
			env = new Envelope(-73.5829, -53.5918, -55.0613, -21.7812);
		}
		
		
		HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
		colours.put(0, Palette.GREY_20.get());
		colours.put(1, Palette.GREY_40.get());
		colours.put(2, Palette.GREY_60.get(0));
		
		/*
		 * expand by 5% (1/20 * width)
		 */
		env.expandBy(env.getWidth()/20);
		/*
		 * Create dataframe with specified envelope
		 */
		DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null).build();
		df.setBackgroundColour(Palette.WATER.get());
		
		/*
		 * Get envelope as resized by dataframe 
		 */
		Envelope resizedEnv = df.getEnvelope();
		List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);
		
		/*
		 * Add PR layer
		 */
		ContinuousScale cs = new ContinuousScale(frameConf.get("continuousScale"), "Probability", "(in units of <i>" + Parasite.Pf.toString() + "</i>PR<span rise='-3000' size='x-small'>2-10</span>, 0-100%)");
		cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0, false);
		cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0, false);
		cs.finish();
		
		H5FloatRaster ras = h5RFPr.getRaster(resizedEnv, new H5FloatRaster.Transform() {
			@Override
			public float transform(float fV) {
				return fV;
			}
		}, cs, 1);
		df.addRasterLayer(ras);
		
		/*
		 * Draw limits layer
		 */
		ColourMap cm = new ColourMap();
		cm.addColour(0, colours.get(0));
		cm.addColour(1, colours.get(1));
		cm.addColour(2, colours.get(2));
		
		cm.finish();
		H5IntRaster.Transform tm= new H5IntRaster.Transform() {
			@Override
			public int transform(int inVal) {
				return inVal;
			}
		};
		
		H5IntRaster h5r = h5RFPfLims.getRaster(resizedEnv, tm, cm, 1);
		h5r.getPixbuf().save("/tmp/pixbuftest.png", PixbufFormat.PNG);
		df.addRasterLayer(h5r);
		
		/* Get admin units (level 0)
		 * overlapping data frame and draw them, avoiding country in question
		 * to produce a mask
		 */
		MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
		Geometry tempG = rect;
		
		FillStyle greyFS = new FillStyle(Palette.WHITE.get());
		List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
		for (AdminUnit admin0 : adminUnits) {
			if (admin0.getCountryId().equals(country.getId())) {
				/*
				 * get difference for sea_mask
				 */
				OverlayOp ol = new OverlayOp(tempG, admin0.getGeom());
				tempG = ol.getResultGeometry(OverlayOp.DIFFERENCE);
			} else {
				PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(Palette.GREY_70.get(), 0.2));
				adminPolySym.add(ps);
			}
		}
		
		/*
		 * Sea mask
		 */
		MultiPolygon mp;
		if (tempG.getNumGeometries() > 1) {
			mp = (MultiPolygon) tempG;
		} else {
			Polygon p = (Polygon) tempG;
			mp = new MultiPolygon(new Polygon[]{p}, new GeometryFactory());
		}
		PolygonSymbolizer aps = new PolygonSymbolizer(mp, new FillStyle(Palette.WATER.get()), new LineStyle(Palette.WATER.get(), 0.2));
		df.drawMultiPolygon(aps);
		df.drawFeatures(adminPolySym);
		
		/*
		 * Draw on water bodies
		 */
		List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
		List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
		FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
		for (WaterBody waterBody : water) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) waterBody.getGeom(), fs, new LineStyle(Palette.WATER.get(), 0.2));
			waterBodies.add(ps);
		}
		df.drawFeatures(waterBodies);
		
		/*
		 * Draw the rest of the canvas
		 */
		PdfSurface mapSurface = new PdfSurface(OutputUtil.getOutputLocation("pf_pr", country.getId()+"_Pf_PR"), w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.addDataFrame(df);
		mapCanvas.drawDataFrames();

		Rectangle frame = new Rectangle(20, 530, 430, 20);
		mapCanvas.setScaleBar(frame, df.getScale(), 7);

		/*
		 * Draw the legend.
		 */
		List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
		legend.add(new MapKeyItem("Water", Palette.WATER.get()));
		legend.add(new MapKeyItem("Malaria free", Palette.GREY_20.get()));
		legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite), colours.get(1)));

		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawKey(legendFrame, legend, 6);

		cs.draw(mapCanvas);
		
		/*
		 * Title
		 */
		String mapTitle = String.format("The spatial distribution of <i>%s</i> malaria endemicity in %s", parasite.getSpecies(), country.getName());
		mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);
		
		/*
		 * Text
		 */
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("country_name", country.getName());
		
		String mapText = mapTextFactory.processTemplate(context, "prText.ftl");
		mapCanvas.drawTextFrame(mapText, frameConf.get("mapTextFrame"), 6, 10);
  	
		OverlayUtil.drawStaticOverlays(mapCanvas);
  	
		mapSurface.finish();
	}
	
	public static void drawRegionPopMap(String regionName, Map<String, Rectangle> frameConf, Template templ, int w, int h) throws Exception {
		
		/*
		 * Get country then use extent to create map canvas and get appropriate admin units.
		 */
		MAPRegion region = adminUnitService.getRegion(regionName);
		Envelope env = new Envelope(region.getMinX(), region.getMaxX(), region.getMinY(), region.getMaxY());
		/*
		 * expand by 5% (1/20 * width)
		 */
		env.expandBy(env.getWidth()/50);
		/*
		 * Create dataframe with specified envelope
		 */
		DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null).build();
		df.setBackgroundColour(Palette.WATER.get());
		
		/*
		 * Get envelope as resized by dataframe 
		 */
		Envelope resizedEnv = df.getEnvelope();
		
		/*
		 * Add population layer
		 */
		String wmsUrl = "http://map1.zoo.ox.ac.uk/geoserver/wms?transparent=true&LAYERS=Pop:pop_10_log_10&STYLES=&SRS=EPSG:4326&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&EXCEPTIONS=application%2Fvnd.ogc.se_inimage";
		RasterLayer ras = new WMSRaster(resizedEnv, 0.00833333333 * 6, wmsUrl);
		df.addRasterLayer(ras);
		
		/* Get admin units (level 0)
		 * overlapping data frame and draw them, avoiding country in question
		 * to produce a mask
		 */
		
		
		/*
		 * Draw on water bodies
		 */
		List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
		List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
		FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
		for (WaterBody waterBody : water) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) waterBody.getGeom(), fs, new LineStyle(Palette.WATER.get(), 0.2));
			waterBodies.add(ps);
		}
		df.drawFeatures(waterBodies);
		
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
		List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
		legend.add(new MapKeyItem("Water", Palette.WATER.get()));
	
		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawKey(legendFrame, legend, 6);
	
		ContinuousScale cs = new ContinuousScale(new Rectangle(390, 600, 15, 90), "Population", null);
		
		double incr = 1d/6;
		DecimalFormat decF = new DecimalFormat("#,###,###");
		
		cs.addColorStopRGB("&lt;1", incr * 0, 26d/255, 152d/255, 80d/255, false);
		cs.addColorStopRGB(decF.format(Math.pow(10, 1)), incr * 1, 0, 0, 0, true);
		cs.addColorStopRGB(decF.format(Math.pow(10, 2)), incr * 2, 0, 0, 0, true);
		
		cs.addColorStopRGB(decF.format(Math.pow(10, 3)), incr * 3, 234d/255, 230d/255, 138d/255, false);
		cs.addColorStopRGB(decF.format(Math.pow(10, 4)), incr * 4, 0, 0, 0, true);
		cs.addColorStopRGB(decF.format(Math.pow(10, 5)), incr * 5, 0, 0, 0, true);
		
		cs.addColorStopRGB(decF.format(Math.pow(10, 6)), incr * 6, 234d/255, 4d/255, 3d/255, false);
		
		cs.printHex();
		/*
		for (int i = 0; i < 7; i++) {
			cs.addColorStopRGB(decF.format(Math.pow(10, i)), incr * i, 1.0, 1-(incr * i), 0);
		}
		*/
		
		cs.finish();
		cs.draw(mapCanvas);
		
		/*
		 * Title
		 */
		String mapTitle = String.format("The population count per km<sup>2</sup> in %s in 2010", regionName);
		mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);
		
		mapSurface.finish();
	
		if (regionName.equals("CSE Asia") || regionName.equals("World")) {
			SystemUtil.addBranding("population", regionName, "population", "landscape_n");
		} else {
			SystemUtil.addBranding("population", regionName, "population", "portrait_n");
		}
		
	}


	public static void drawLimitsMapRegion(String regionName, Parasite parasite, H5RasterFactory h5Fact, Map<String, Rectangle> frameConf, int w, int h, 
			boolean prPointsRequired, boolean duffyReq) throws OutOfMemoryError, Exception {
	
		
		// Legend items
		List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
		HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
		colours.put(0, Palette.GREY_20.get());
		colours.put(1, Palette.GREY_40.get());
		colours.put(2, Palette.GREY_60.get());
		
		/*
		 * 1. Draw limits raster
		 * 2. Draw mask (countries)
		 * 3. Draw PR points
		 */
	
		/*
		 * Get country then use extent to create map canvas and get appropriate admin units.
		 */
		Envelope env = null; 
		if(regionName.startsWith("North Africa"))
			env = new Envelope(-20, 55, 10, 30);
		else
			env = new Envelope(0, 55, -35, -10);
			
		//Expand by 5%
//		env.expandBy(env.getWidth()/20);
		
		/*
		 * Create dataframe with specified envelope
		 */
		DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null).build();
		df.setBackgroundColour(Palette.WATER.get());
		
		/*
		 * Get envelope as resized by dataframe 
		 */
		Envelope resizedEnv = df.getEnvelope();
		List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);
		
		/* Get admin units (level 0)
		 * overlapping data frame and draw them, avoiding country in question
		 * to produce a mask
		 */
		FillStyle greyFS = new FillStyle(Palette.WHITE.get());
		List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
		for (AdminUnit admin0 : adminUnits) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, defaultLineStyle);
			adminPolySym.add(ps);
		}
		df.drawFeatures(adminPolySym);
		
		/*
		 * Draw limits layer
		 */
		ColourMap cm = new ColourMap();
		cm.addColour(0, colours.get(0));
		cm.addColour(1, colours.get(1));
		cm.addColour(2, colours.get(2));
		
		cm.finish();
		H5IntRaster.Transform tm= new H5IntRaster.Transform() {
			@Override
			public int transform(int inVal) {
				return inVal;
			}
		};
		
		H5IntRaster h5r = h5Fact.getRaster(resizedEnv, tm, cm, 1);
//		h5r.getPixbuf().save("/tmp/pixbuftest.png", PixbufFormat.PNG);
		df.addRasterLayer(h5r);
		
		/*
		 * Duffy mask
		 */
		if (duffyReq) {
			Duffy duf = cartoService.getDuffy();
			FillStyle fsDuffy = new FillStyle(Palette.WHITE.get(0), FillType.DUFFY);
			PolygonSymbolizer dufPs = new PolygonSymbolizer((MultiPolygon) duf.getGeom(), fsDuffy, new LineStyle(Palette.WHITE.get(0), 0));
			df.drawMultiPolygon(dufPs);
		}
		
	
		/*
		 * Draw on water bodies
		 */
		List<PolygonSymbolizer> waterBodies = layerFactory.getPolygonLayer(
			adminUnitService.getWaterBodies(resizedEnv),
			new FillStyle(Palette.WATER.get(), FillType.SOLID),	
			new LineStyle(Palette.WATER.get(), 0.2)
		);
		df.drawFeatures(waterBodies);
		
		/*
		 * Draw the rest of the canvas
		 */
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.addDataFrame(df);
		mapCanvas.drawDataFrames();
		mapCanvas.setScaleBar(frameConf.get("scalebar"), df.getScale(), 7);
	
		/*
		 * Draw the legend TODO: combine this and the styling in a layer
		 */
		legend.add(new MapKeyItem("Water", Palette.WATER.get()));
		legend.add(new MapKeyItem("Malaria free", Palette.GREY_20.get()));
	
		legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite), colours.get(1)));
		legend.add(new MapKeyItem(String.format("<i>%s</i>API ≥ 0.1‰", parasite), colours.get(2)));
		
		/*
		 * Add duffy if required
		 */
		if (duffyReq) {
			MapKeyItem li = new MapKeyItem("Duffy negativity ≥ 90%", Palette.BLACK.get(0));
			li.duffy = true;
			legend.add(li);
		}
	
		mapCanvas.drawKey(frameConf.get("legendFrame"), legend, 6);
	
		if(prPointsRequired) {
			ContinuousScale cs = new ContinuousScale(frameConf.get("continuousScale"), "Probability", "(in units of <i>" + parasite + "</i>PR<sub><small>2-10</small></sub>, 0-100%)");
			cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0, false);
			cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0, false);
			cs.finish();
			cs.draw(mapCanvas);
		}
		
		/*
		 * Title
		 */
		String mapTitle = "<i>%s</i> malaria risk in %s";
		mapTitle = String.format(mapTitle, parasite.getSpecies(), regionName);
		mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);
		
		/*
		 * Map text
		 */
		
		Map<String, Object> x = prService.getSurveyMetadata(regionName, parasite);
		Integer startYear = (Integer) x.get("min");
		Integer endYear = (Integer) x.get("max");
		Long nSurveys = (Long) x.get("n");
		Map<String, Object> context = new HashMap<String, Object>();
		
		if (startYear != null) {
			context.put("yearStart", startYear.toString());
		}
		if (endYear != null) {
			context.put("yearEnd", endYear.toString());
		}
		
		context.put("nSurveys", nSurveys);
		//TODO: hack to avoid putting text on
		if (!prPointsRequired) {
			context.put("nSurveys", 0);
		}
		
		context.put("parasiteAbbr", parasite.getSpeciesAbbr());
		context.put("parasite", parasite);
		
		String mapText = mapTextFactory.processTemplate(context, "limitsText.ftl");
		mapCanvas.drawTextFrame(mapText, frameConf.get("mapTextFrame"), 6, 10);
		mapSurface.finish();
	
		SystemUtil.addBranding(parasite.toString().toLowerCase()+"_limits_reg", regionName, parasite.toString().toLowerCase(), "branding_final");
	}


  public static void drawPopMap(Country country, Map<String, Rectangle> frameConf, int w, int h, H5RasterFactory h5RF) throws Exception {
  	
  	/*
  	 * Get country then use extent to create map canvas and get appropriate admin units.
  	 */
  	Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
  	if (country.getId().equals("ARG")) {
  		env = new Envelope(-73.5829, -53.5918, -55.0613, -21.7812);
  	}
  	
  	/*
  	 * expand by 5% (1/20 * width)
  	 */
  	env.expandBy(env.getWidth()/20);
  	/*
  	 * Create dataframe with specified envelope
  	 */
  	DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null).build();
  	df.setBackgroundColour(Palette.WATER.get());
  	
  	/*
  	 * Get envelope as resized by dataframe 
  	 */
  	Envelope resizedEnv = df.getEnvelope();
  	List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);
  	
  	/*
  	 * Add population layer
  	 */
  	ContinuousScale cs = new ContinuousScale(new Rectangle(390, 600, 15, 90), "Population", null);
  	
  	double incr = 1d/6;
  	DecimalFormat decF = new DecimalFormat("#,###,###");
  	
  	cs.addColorStopRGB("&lt;1", incr * 0, 26d/255, 152d/255, 80d/255, false);
  	cs.addColorStopRGB(decF.format(Math.pow(10, 1)), incr * 1, 0, 0, 0, true);
  	cs.addColorStopRGB(decF.format(Math.pow(10, 2)), incr * 2, 0, 0, 0, true);
  	cs.addColorStopRGB(decF.format(Math.pow(10, 3)), incr * 3, 234d/255, 230d/255, 138d/255, false);
  	cs.addColorStopRGB(decF.format(Math.pow(10, 4)), incr * 4, 0, 0, 0, true);
  	cs.addColorStopRGB(decF.format(Math.pow(10, 5)), incr * 5, 0, 0, 0, true);
  	cs.addColorStopRGB(decF.format(Math.pow(10, 6)), incr * 6, 234d/255, 4d/255, 3d/255, false);
  	cs.finish();
  	
  	H5FloatRaster ras = h5RF.getRaster(resizedEnv, new H5FloatRaster.Transform() {
  		@Override
  		public float transform(float fV) {
  			if (fV < 1) {
  				fV = 1;
  			}
  			fV = (float) (Math.log10(fV))/6;
  			if (fV < 0) {
  				fV = 0;
  			}
  			return fV;
  		}
  	}, cs, 1);
  	df.addRasterLayer(ras);
  	
  	/* Get admin units (level 0)
  	 * overlapping data frame and draw them, avoiding country in question
  	 * to produce a mask
  	 */
  	MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
  	Geometry tempG = rect;
  	
  	FillStyle greyFS = new FillStyle(Palette.WHITE.get());
  	List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
  	for (AdminUnit admin0 : adminUnits) {
  		if (admin0.getCountryId().equals(country.getId())) {
  			/*
  			 * get difference for sea_mask
  			 */
  			OverlayOp ol = new OverlayOp(tempG, admin0.getGeom());
  			tempG = ol.getResultGeometry(OverlayOp.DIFFERENCE);
  		} else {
  			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(Palette.GREY_70.get(), 0.2));
  			adminPolySym.add(ps);
  		}
  	}
  	
  	/*
  	 * Sea mask
  	 */
  	MultiPolygon mp = GeometryUtil.toMultiPolygon(new GeometryFactory(), tempG);
  	PolygonSymbolizer aps = new PolygonSymbolizer(mp, new FillStyle(Palette.WATER.get()), new LineStyle(Palette.WATER.get(), 0.2));
  	df.drawMultiPolygon(aps);
  	df.drawFeatures(adminPolySym);
  	
  	
  	/*
  	 * Draw on water bodies
  	 */
  	List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
  	List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
  	FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
  	for (WaterBody waterBody : water) {
  		PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) waterBody.getGeom(), fs, new LineStyle(Palette.WATER.get(), 0.2));
  		waterBodies.add(ps);
  	}
  	df.drawFeatures(waterBodies);
  	
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
  	List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
  	legend.add(new MapKeyItem("Water", Palette.WATER.get()));
  
  	Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
  	mapCanvas.drawKey(legendFrame, legend, 6);
  
  	cs.draw(mapCanvas);
  	
  	/*
  	 * Title
  	 */
  	String mapTitle = String.format("The population count per km<sup>2</sup> in %s in 2010", country.getName());
  	mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);
  	
  	/*
  	 * Text
  	 */
  	CountryPopulation cp = adminUnitService.getCountryPop(country);
  	Map<String, Object> context = new HashMap<String, Object>();
  	Integer popTot = (int) (Math.round(((double) cp.getTotal()) / 1000) * 1000);
  	context.put("pop_total", popTot);
  	context.put("country_name", country.getName());
  	context.put("usesAfripop", cp.getUsesAfripop());
  	
  	String mapText = mapTextFactory.processTemplate(context, "populationText.ftl");
  	mapCanvas.drawTextFrame(mapText, frameConf.get("mapTextFrame"), 6, 10);
  	mapSurface.finish();
  
  	SystemUtil.addBranding("population", country.getId(), "population", "portrait_n");
  }
  
}
