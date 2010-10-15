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
import uk.ac.ox.map.base.model.adminunit.MAPRegion;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.carto.Layer;
import uk.ac.ox.map.base.model.carto.MapConf;
import uk.ac.ox.map.base.model.carto.MapLayer;
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
import uk.ac.ox.map.imageio.FltReader;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class PRMaps {

	static final Logger logger = LoggerFactory.getLogger(PRMaps.class);
	static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(), 0.2);
	static final PRService prService = new PRService();
	static final AdminUnitService adminUnitService = new AdminUnitService();
	static Map<String, String> rasLayers = new HashMap<String, String>();

	public static void main(String[] args) throws IOException, InterruptedException {
		Gtk.init(null);
		int w = 500;
		int h = 707;
		
		Map<String, Rectangle> frameConfP = new HashMap<String, Rectangle>();
		frameConfP.put("dataFrame", new Rectangle(20, 40, 460, 460));
		frameConfP.put("scalebar", new Rectangle(20, 530, 430, 20));
		frameConfP.put("legendFrame", new Rectangle(390, 555, 150, 200));
		frameConfP.put("continuousScale", new Rectangle(390, 675, 100, 15));
		frameConfP.put("titleFrame", new Rectangle(0, 3, 500, 0));
		frameConfP.put("mapTextFrame", new Rectangle(50, 555, 320, 0));
		
		
		Map<String, Rectangle> frameConfLS = new HashMap<String, Rectangle>();
		frameConfLS.put("dataFrame", new Rectangle(20, 40, 667, 330));
		frameConfLS.put("scalebar", new Rectangle(20, 390, 637, 20));
		frameConfLS.put("legendFrame", new Rectangle(480, 410, 120, 200));
		frameConfLS.put("continuousScale", new Rectangle(587, 440, 100, 15));
		frameConfLS.put("titleFrame", new Rectangle(10, 3, 687, 0));
		frameConfLS.put("mapTextFrame", new Rectangle(50, 410, 420, 0));
		
//		drawTestFrame(frameConf, w, h);
//		drawTestFrame(frameConf, h, w);
//		drawMap("pf", "Africa+", frameConf, w, h);
//		drawMeanMap("pf", "Africa+", frameConf, w, h);
		
		
		List<MapConf> mapConfs = adminUnitService.getMaps();
		for (MapConf mapConf : mapConfs) {
			System.out.println(mapConf.getTitle());
//			if (mapConf.getId() != 1) continue;
			drawMap("pf", "CSE Asia", mapConf, frameConfLS, h, w);
			drawMap("pf", "Africa+", mapConf, frameConfP, w, h);
			drawMap("pf", "Americas", mapConf, frameConfP, w, h);
			drawMap("pf", "World", mapConf, frameConfLS, h, w);
		}
		
	}
	
	public static void drawMap(String parasite, String regionName, MapConf mapConf, Map<String, Rectangle> frameConf, int w, int h) throws IOException, InterruptedException {
		
		List<MapLayer> mls = adminUnitService.getMapLayers(mapConf.getId());
		
		List<Layer> layers = new ArrayList<Layer>();
		for (MapLayer mapLayer : mls) {
			layers.add(adminUnitService.getLayer(mapLayer.getLayerId()));
		}
		
		
		String parasiteStr;
		if (parasite.equals("pf"))
			parasiteStr = "Pf";
		else
			parasiteStr = "Pv";
		
		ContinuousScale cs;
		if (mapConf.getName().equals("Uncertainty")) {
			cs = new ContinuousScale(frameConf.get("continuousScale"), "Standard deviation", "(in units of <i>" + parasiteStr + "</i>PR<sub><small>2-10</small></sub>, 0-100%)");
			cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0.0, false);
			cs.addColorStopRGB("",  0.2, 0.0, 1.0, 0.0, false);
			cs.addColorStopRGB("40", 0.4, 0.0, 0.0, 1.0, false);
			cs.finish();
		} else if (mapConf.getName().startsWith("Attack")) {
			cs = new ContinuousScale(frameConf.get("continuousScale"), "Attack rate", null);
			cs.addColorStopRGB("0", 0.0, 0.0, 1.0, 1.0, false);
			cs.addColorStopRGB("",  0.35, 0.0, 0.0, 1.0, false);
			cs.addColorStopRGB("70",  0.7, 1.0, 0.0, 1.0, false);
			cs.finish();
		} else {
			cs = new ContinuousScale(frameConf.get("continuousScale"), "Probability", "(in units of <i>" + parasiteStr + "</i>PR<sub><small>2-10</small></sub>, 0-100%)");
			cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0, false);
			cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0, false);
			cs.finish();
		}
		

		/*
		 * Get country then use extent to create map canvas and get appropriate admin units.
		 */
		MAPRegion region = adminUnitService.getRegion(regionName);
		Envelope env = new Envelope(region.getMinX(), region.getMaxX(), region.getMinY(), region.getMaxY());
		
		// Legend items
		List<LegendItem> legend = new ArrayList<LegendItem>();
		HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
		colours.put(0, Palette.GREY_20.get());
		colours.put(1, Palette.GREY_40.get());
		colours.put(2, Palette.GREY_60.get());
		MapTextResource mtr = new MapTextResource();

		/*
		 * expand by 5% (1/20 * width)
		 */
		env.expandBy(env.getWidth()/40);
		
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
		 * Draw layers FIXME much emergency hackery!!
		 */
		
		for (Layer layer : layers) {
			if (layer.getLayerType().equals("WMS")) {
				RasterLayer ras = new WMSRaster(resizedEnv, 0.0416666666, layer.getLayerLocation());
				df.addRasterLayer(ras);
			} else {
				RasterLayer ras = FltReader.openFloatFile(new File("/home/will/map1_public/temp/pr/"), layer.getLayerLocation(), cs, resizedEnv);
				df.addRasterLayer(ras);
			}
		}
		
		/* Get admin units (level 0)
		 * overlapping data frame and draw them
		 * to produce a mask
		 */
		List<AdminUnit> adminUnits = adminUnitService.getMaskByRegion(resizedEnv, region);
		FillStyle greyFS = new FillStyle(Palette.WHITE.get());
		List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
		for (AdminUnit admin0 : adminUnits) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(Palette.GREY_40.get(), 0.1));
			adminPolySym.add(ps);
		}
		df.drawFeatures(adminPolySym);
		
		/* Get admin units (level 0)
		 * overlapping data frame and draw them, avoiding country in question
		 * to produce a mask
		 */
		FillStyle transparent = new FillStyle(new Colour("#ffffff", 0));
		List<AdminUnit> adminUnits2 = adminUnitService.getCountriesByRegion(resizedEnv, region);
		List<PolygonSymbolizer> adminPolySym2 = new ArrayList<PolygonSymbolizer>();
		for (AdminUnit admin0 : adminUnits2) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), transparent, new LineStyle(Palette.GREY_60.get(), 0.2));
			adminPolySym2.add(ps);
		}
		df.drawFeatures(adminPolySym2);

		/*
		 * Draw on water bodies
		 */
		List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
		List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
		Colour waterColour = Palette.WATER.get();
		FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
		LineStyle ls = new LineStyle(Palette.WATER.get(), 0.1);
		for (WaterBody waterBody : water) {
			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) waterBody.getGeom(), fs, ls);
			waterBodies.add(ps);
		}
		df.drawFeatures(waterBodies);
		
		/*
		 * Draw on points
		 */
		if (mapConf.getName().equals("Limits")) {
			List<PRPoint> prPoints = prService.getPointsByRegion(regionName, parasite);
			for (PRPoint prPoint : prPoints) {
				df.drawPoint(prPoint.getLongitude(), prPoint.getLatitude(), prPoint.getColour());
			}
			System.out.println(prPoints.size());
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
		legend.add(new LegendItem("Water", waterColour));
		legend.add(new LegendItem("Malaria free", Palette.GREY_20.get()));

		legend.add(new LegendItem(String.format("<i>%s</i>API &lt; 0.1‰", parasiteStr), colours.get(1)));
		
		if (mapConf.getName().equals("Limits")){
			legend.add(new LegendItem(String.format("<i>%s</i>API ≥ 0.1‰", parasiteStr), colours.get(2)));
		}

		mapCanvas.drawLegend(frameConf.get("legendFrame"), legend);

		cs.draw(mapCanvas);
		
		/*
		 * Title
		 */
		mapCanvas.setTitle(String.format(mapConf.getTitle(), region.getDescription()), frameConf.get("titleFrame"), 8);
		
		List<String> mapTextItems = new ArrayList<String>();
		
		String pointText = "The %s <i>P. falciparum</i> parasite rate surveys available for predicting prevalance within the stable limits were collected between %s and %s.";
		if (mapConf.getName().equals("Limits")) {
			Map<String, Object> yrs = prService.getSurveysByRegion(region.getId());
			mapTextItems.add(String.format(pointText, yrs.get("n"), yrs.get("min"), yrs.get("max")));
		}
		
		/*
		String citation = "<b>Citation:</b> Guerra, C.A. <i>et al.</i> The international limits and population at risk of <i>Plasmodium vivax</i> transmission in 2009. <i>Public Library of Science Neglected Tropical Diseases</i>, <b>4</b>(8): e774.";
		mapTextItems.add(citation);
		
		mapTextItems.add((String) mtr.getObject("copyright"));
		*/
		
		mapCanvas.drawTextFrame(mapTextItems, frameConf.get("mapTextFrame"), 6, 10);
		mapSurface.finish();

		if (regionName.equals("CSE Asia") || regionName.equals("World")) {
			SystemUtil.addBranding("pr", regionName, mapConf.getName(), "landscape_n");
		} else {
			SystemUtil.addBranding("pr", regionName, mapConf.getName(), "portrait_n");
		}
	}

	public static void drawTestFrame(Map<String, Rectangle> frameConf, int w, int h) throws IOException, InterruptedException {
		
		PdfSurface mapSurface = new PdfSurface("/tmp/testframe.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		for (String key : frameConf.keySet()) {
			mapCanvas.drawTestRectangle(frameConf.get(key));
		}
			
		mapSurface.finish();
	}

}
