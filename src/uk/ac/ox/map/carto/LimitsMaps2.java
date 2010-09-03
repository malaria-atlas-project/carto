package uk.ac.ox.map.carto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gtk.Gtk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.Parasite;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.pr.PRPoint;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.PRService;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.LegendItem;
import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.raster.WMSRaster;
import uk.ac.ox.map.carto.style.Colour;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.text.MapTextResource;
import uk.ac.ox.map.carto.util.SystemUtil;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class LimitsMaps2 {

	static final int w = 500;
	static final int h = 707;
	static final Logger logger = LoggerFactory.getLogger(LimitsMaps2.class);
	static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(), 0.2);
	static final PRService prService = new PRService();

	public static void main(String[] args) throws IOException, InterruptedException {
		Gtk.init(null);
		AdminUnitService adminUnitService = new AdminUnitService();

		String countryId = "CHN";
		Country country = adminUnitService.getCountry(countryId);
//		drawMap(adminUnitService, country, "pf");
		drawMap(adminUnitService, country, "pv");

//		 drawMap(adminUnitService, "pf");
//		 drawMap(adminUnitService, "pv");
	}

	@SuppressWarnings("unused")
	private static void drawMap(AdminUnitService adminUnitService, String parasite) throws IOException, InterruptedException {
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

		Map<String, Object> x = prService.getStartYear(country.getId());
		Integer startYear = (Integer) x.get("min");
		Integer endYear = (Integer) x.get("max");
		Long nSurveys = (Long) x.get("n");
		
		// Legend items
		List<LegendItem> legend = new ArrayList<LegendItem>();
		HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
		colours.put(0, Palette.GREY_20.get());
		colours.put(1, Palette.GREY_40.get());
		colours.put(2, Palette.GREY_60.get());
		MapTextResource mtr = new MapTextResource();
		
		/*
		 * 1. Draw limits raster
		 * 2. Draw mask (countries)
		 * 3. Draw PR points
		 */

		/*
		 * Get country then use extent to create map canvas and get appropriate admin units.
		 */
		Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
		/*
		 * expand by 5% (1/20 * width)
		 */
		env.expandBy(env.getWidth()/20);
		
		/*
		 * Create dataframe with specified envelope
		 */
		DataFrame df = new DataFrame.Builder(env, new Rectangle(20, 40, 460, 460), null).build();
		df.setBackgroundColour(Palette.WATER.get());
		
		/*
		 * Get envelope as resized by dataframe 
		 */
		Envelope resizedEnv = df.getEnvelope();
		List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);
		
		/*
		 * Draw limits layer
		 */
		String wmsUrl = "http://map1.zoo.ox.ac.uk/geoserver/wms?LAYERS=Limits:pvlims4f&STYLES=&SRS=EPSG:4326&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&EXCEPTIONS=application%2Fvnd.ogc.se_inimage";
		RasterLayer ras = new WMSRaster(resizedEnv, 0.00833333333, wmsUrl);
		df.addRasterLayer(ras);
		
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
		List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
		List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
		Colour waterColour = Palette.WATER.get();
		FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
		for (WaterBody waterBody : water) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) waterBody.getGeom(), fs, defaultLineStyle);
			waterBodies.add(ps);
		}
		df.drawFeatures(waterBodies);
		
		/*
		 * Draw on points
		 */
		List<PRPoint> pvPoints = prService.getPoints(country.getId(), parasite);
		for (PRPoint pvPoint : pvPoints) {
			df.drawPoint(pvPoint.getLongitude(), pvPoint.getLatitude(), pvPoint.getColour());
		}
		System.out.println(pvPoints.size());

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
		legend.add(new LegendItem("Malaria free", Palette.GREY_20.get()));

		String apiLegendStr;
		if (parasite.equals("pf"))
			apiLegendStr = "Pf";
		else
			apiLegendStr = "Pv";
		legend.add(new LegendItem(String.format("<i>%s</i>API &lt; 0.1‰", apiLegendStr), colours.get(1)));
		legend.add(new LegendItem(String.format("<i>Pf</i>API ≥ 0.1‰", apiLegendStr), colours.get(2)));

		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawLegend(legendFrame, legend);

		ContinuousScale cs = new ContinuousScale(new Rectangle(390, 675, 100, 15), "Probability", "(in units of <i>Pv</i>PR<sub><small>2-10</small></sub>, 0-100%)");
		cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0);
		cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0);
		cs.finish();
		cs.draw(mapCanvas);
		
		/*
		 * Title
		 */
		Rectangle titleFrame = new Rectangle(0, 5, 500, 0);
		String mapTitle = "<i>Plasmodium vivax</i> malaria risk in %s";
		mapTitle = String.format(mapTitle, country.getName());
		mapCanvas.setTitle(mapTitle, titleFrame, 10);
		
		List<String> mapTextItems = new ArrayList<String>();
		
		if (nSurveys > 0) {
			String pointText = "The %s <i>P. vivax</i> parasite rate surveys available for predicting prevalance within the stable limits were collected between %s and %s.";
			pointText = String.format(pointText, nSurveys, startYear, endYear);
			mapTextItems.add(pointText);
		}
		String citation = "<b>Citation:</b> Guerra, C.A. <i>et al.</i> The international limits and population at risk of <i>Plasmodium vivax</i> transmission in 2009. <i>Public Library of Science Neglected Tropical Diseases</i>, <b>4</b>(8): e774.";
		mapTextItems.add(citation);
		
		mapTextItems.add((String) mtr.getObject("copyright"));
		
		
		
		Rectangle mapTextFrame = new Rectangle(50, 555, 320, 0);
		mapCanvas.drawTextFrame(mapTextItems, mapTextFrame, 6, 10);
		mapSurface.finish();

		SystemUtil.addBranding(country.getId(), parasite, "branding_final");
	}

}
