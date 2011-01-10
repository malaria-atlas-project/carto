package uk.ac.ox.map.carto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.PixbufFormat;
import org.gnome.gtk.Gtk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.MAPRegion;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.carto.Layer;
import uk.ac.ox.map.base.model.carto.MapConf;
import uk.ac.ox.map.base.model.carto.MapLayer;
import uk.ac.ox.map.base.model.carto.Region;
import uk.ac.ox.map.base.model.carto.Region.Orientation;
import uk.ac.ox.map.base.model.pr.PRPoint;
import uk.ac.ox.map.base.model.pr.Parasite;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
import uk.ac.ox.map.base.service.PRService;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.MapKeyItem;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.text.MapTextFactory;
import uk.ac.ox.map.carto.util.OutputUtil;
import uk.ac.ox.map.carto.util.OverlayUtil;
import uk.ac.ox.map.carto.util.SystemUtil;
import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.h5.H5FloatRaster;
import uk.ac.ox.map.h5.H5IntRaster;
import uk.ac.ox.map.h5.H5RasterFactory;
import uk.ac.ox.map.imageio.ColourMap;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class CountryPRMaps {
	static Logger logger = LoggerFactory.getLogger(CountryPRMaps.class);
  
	static final AdminUnitService adminUnitService = new AdminUnitService();
	static final CartoService cartoService = new CartoService();
	private static final MapTextFactory mapTextFactory = new MapTextFactory();
	static final PRService prService = new PRService();
	
	static Map<String, Rectangle> frameConfP = new HashMap<String, Rectangle>();
	static Map<String, Rectangle> frameConfLS = new HashMap<String, Rectangle>();
	
	static H5RasterFactory pfLimsH5;
	static H5RasterFactory pfPRH5;
	static H5RasterFactory pfStdH5;
	static H5RasterFactory pfUncertaintyClass;
  
	public static void main(String[] args) throws Exception {
	  Gtk.init(args);
  	pfLimsH5 = new H5RasterFactory("/home/will/map1_public/mbgw2/pf_limits.h5");
  	pfPRH5 = new H5RasterFactory("/home/will/map1_public/mbgw2/pf_pr.h5");
  	pfStdH5 = new H5RasterFactory("/home/will/map1_public/mbgw2/pf_std.h5");
  	pfUncertaintyClass = new H5RasterFactory("/home/will/map1_public/mbgw2/s2_plmc.h5");
  	
		frameConfP.put("CANVAS", new Rectangle(0, 0, 500, 707));
		frameConfP.put("DATAFRAME", new Rectangle(20, 40, 460, 460));
		frameConfP.put("SCALEBAR", new Rectangle(20, 530, 430, 20));
		frameConfP.put("KEY", new Rectangle(390, 555, 150, 200));
		frameConfP.put("CONTINUOUSSCALE", new Rectangle(390, 675, 100, 15));
		frameConfP.put("TITLE", new Rectangle(0, 3, 500, 0));
		frameConfP.put("LEGEND", new Rectangle(50, 555, 320, 0));
		
		frameConfLS.put("CANVAS", new Rectangle(0, 0, 707, 500));
		frameConfLS.put("DATAFRAME", new Rectangle(20, 40, 667, 330));
		frameConfLS.put("SCALEBAR", new Rectangle(20, 390, 637, 20));
		frameConfLS.put("KEY", new Rectangle(480, 410, 120, 200));
		frameConfLS.put("CONTINUOUSSCALE", new Rectangle(587, 440, 100, 15));
		frameConfLS.put("TITLE", new Rectangle(10, 3, 687, 0));
		frameConfLS.put("LEGEND", new Rectangle(50, 410, 420, 0));
		
	  Parasite para = Parasite.Pf;
  	
		List<MapConf> mapConfs = adminUnitService.getMaps();
		for (MapConf mapConf : mapConfs) {
		  logger.debug(mapConf.getName());
		  
			if (mapConf.getName().startsWith("Attack")) continue;
			
//      	drawCountryPRMaps(mapConf, para, cs, pfPRH5, pfLimsH5);
      	drawRegionPRMaps(mapConf, para);
//      	drawCountryPRMaps(mapConf, para);
//      	drawCountryPRMaps(mapConf, para, cs2, pfStdH5, pfLimsH5);
      	
		}
	  
	}
	
	public static void drawCountryPRMaps(MapConf mapConf, Parasite para) throws Exception {
	  
	  List<Country> countries = adminUnitService.getMECountries();
	  for (Country country : countries) {
	    if(country.getPfEndemic()) {
        drawCountryPRMap(mapConf, para, country);
	    }
    }
	  
	}
	
	public static void drawCountryPRMap(MapConf mapConf, Parasite para, Country country) throws Exception{
    
  	Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY()); 
  	env.expandBy(env.getWidth()/20);
  	
  	/*
  	 * Create dataframe with specified envelope
  	 */
  	DataFrame df = new DataFrame.Builder(env, frameConfP.get("DATAFRAME"), null).build();
  	df.setBackgroundColour(Palette.WATER.get());
  	Envelope resizedEnv = df.getEnvelope();
  	
  	List<AdminUnit> maskPolys = adminUnitService.getMaskByCountry(resizedEnv, country.getId());
  	
  	Region region = new Region(country);
  	if(mapConf.getName().equals("Limits")) {
  	  drawSurveyMap(df, para, mapConf, region, maskPolys, frameConfP);
  	} else if (mapConf.getName().equals("Uncertainty class")){
  	  drawUCMap(df, para, mapConf, region, maskPolys, frameConfP);
  	} else {
    	drawPRMap(df, para, mapConf, region, maskPolys, frameConfP);
  	}
  }

  public static void drawRegionPRMaps(MapConf mapConf, Parasite para) throws Exception {
	  
	  List<String> regions = new ArrayList<String>();
	  regions.add("CSE Asia");
    regions.add("Africa+");
    regions.add("Americas");
//    regions.add("World");
    for (String string : regions) {
  	  MAPRegion reg = adminUnitService.getRegion(string);
  	  drawRegionPRMap(mapConf, para, reg);
    }
	}
	
	public static void drawRegionPRMap(MapConf mapConf, Parasite para, MAPRegion reg) throws Exception{
	  
		Envelope env = new Envelope(reg.getMinX(), reg.getMaxX(), reg.getMinY(), reg.getMaxY()); 
  	env.expandBy(env.getWidth()/20);
	  
	  Map<String, Rectangle> frameConf;
	  if(env.getWidth() > env.getHeight()) {
	    frameConf = frameConfLS;
	  } else {
	    frameConf = frameConfP;
	  }
	  
  	/*
  	 * Create dataframe with specified envelope
  	 */
  	DataFrame df = new DataFrame.Builder(env, frameConf.get("DATAFRAME"), null).build();
  	df.setBackgroundColour(Palette.WATER.get());
  	Envelope resizedEnv = df.getEnvelope();
  	
  	List<AdminUnit> maskPolys = adminUnitService.getMaskByRegion(resizedEnv, reg);
    
    Region region = new Region(reg);
    System.out.println(region.getOrientation().toString());
    
  	if(mapConf.getName().equals("Limits")) {
//  	  drawSurveyMap(df, para, mapConf, region, maskPolys, frameConf);
  	} else if(mapConf.getName().equals("Uncertainty")) {
  	  drawPRMap(df, para, mapConf, region, maskPolys, frameConf);
  	} else if (mapConf.getName().equals("Uncertainty class")){
//  	  drawUCMap(df, para, mapConf, region, maskPolys, frameConf);
  	} else {
//    	drawPRMap(df, para, mapConf, region, maskPolys, frameConf);
  	}
	}
	
	public static void drawPRMap(DataFrame df, Parasite parasite, MapConf mapConf, Region reg, List<AdminUnit> adminPolys, Map<String, Rectangle> frameConf) throws Exception {
    
  	/*
  	 * Get envelope as resized by dataframe 
  	 */
  	Envelope resizedEnv = df.getEnvelope();
  	
  	RasterLayer ras;
    
  	ContinuousScale cs = null;
  	if (mapConf.getName().equals("Probability")) {
    	cs = new ContinuousScale(frameConf.get("CONTINUOUSSCALE"), "Probability", "(in units of <i>" + parasite.toString() + "</i>PR<span rise='-3000' size='x-small'>2-10</span>, 0-100%)");
    	cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0, false);
    	cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0, false);
    	cs.finish();
    	
    	ras = pfPRH5.getRaster(resizedEnv, new H5FloatRaster.Transform() {
    		@Override
    		public float transform(float fV) {
    			return fV;
    		}
    	}, cs, 5);
    	
  	} else if (mapConf.getName().equals("Uncertainty")){
  		cs = new ContinuousScale(frameConf.get("CONTINUOUSSCALE"), "Standard deviation", "(in units of <i>" + parasite.toString() + "</i>PR<sub><small>2-10</small></sub>, 0-100%)");
  		cs.addColorStopRGB("0", 0.0, 0.0, 0.0, 1.0, false);
  		cs.addColorStopRGB("",  0.2, 0.0, 1.0, 0.0, false);
  		cs.addColorStopRGB("40",0.4, 1.0, 1.0, 0.0, false);
  		cs.finish();
  		
    	ras = pfStdH5.getRaster(resizedEnv, new H5FloatRaster.Transform() {
    		@Override
    		public float transform(float fV) {
    			return fV;
    		}
    	}, cs, 5);
  	  
  	} else {
  	  throw new Exception("Unknown map conf: " + mapConf.getName());
  	}
  	
  	/*
  	 * Get country then use extent to create map canvas and get appropriate admin units.
  	 */
  	
  	HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
  	colours.put(0, Palette.GREY_20.get());
  	colours.put(1, Palette.GREY_40.get());
  	colours.put(2, Palette.GREY_60.get(0));
  	
  	
  	
  	/*
  	 * Add PR layer
  	 */
  	
  	
//  	ras.getPixbuf().save("/tmp/tmpx.png", PixbufFormat.PNG);
  	df.addRasterLayer(ras);
  	
  	/*
  	 * Draw limits layer
  	 */
  	ColourMap cm = new ColourMap();
  	cm.addColour(0, colours.get(0));
  	cm.addColour(1, colours.get(1));
  	cm.addColour(2, colours.get(2));
  	
  	cm.finish();
  	H5IntRaster.Transform tm = new H5IntRaster.Transform() {
  		@Override
  		public int transform(int inVal) {
  			return inVal;
  		}
  	};
  	
  	H5IntRaster h5r = pfLimsH5.getRaster(resizedEnv, tm, cm, 1);
  	df.addRasterLayer(h5r);
  	
  	/* Get admin units (level 0)
  	 * overlapping data frame and draw them, avoiding country in question
  	 * to produce a mask
  	 */
  	MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
  	Geometry tempG = rect;
  	
  	FillStyle greyFS = new FillStyle(Palette.WHITE.get());
  	List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
  	for(AdminUnit admin0: adminPolys) {
  			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(Palette.GREY_70.get(), 0.2));
  			adminPolySym.add(ps);
		}
  	df.drawFeatures(adminPolySym);
  	
  	/*
  	 * Sea mask
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
  	Rectangle canvRect = frameConf.get("CANVAS");
  	PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", canvRect.width, canvRect.height);
  	MapCanvas mapCanvas = new MapCanvas(mapSurface, canvRect.width, canvRect.height);
  	mapCanvas.addDataFrame(df);
  	mapCanvas.drawDataFrames();
  
  	Rectangle frame = new Rectangle(20, 530, 430, 20);
  	mapCanvas.setScaleBar(frame, df.getScale(), 7);
  
  	/*
  	 * Draw the key.
  	 */
  	List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
  	legend.add(new MapKeyItem("Water", Palette.WATER.get()));
  	legend.add(new MapKeyItem("Malaria free", Palette.GREY_20.get()));
  	legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite), colours.get(1)));
  
  	Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
  	mapCanvas.drawKey(legendFrame, legend);
  
  	
   cs.draw(mapCanvas);
  	
  	/*
  	 * Title
  	 */
  	String mapTitle = String.format(mapConf.getTitle(), reg.getDisplayName());
  	mapCanvas.setTitle(mapTitle, frameConf.get("TITLE"), 10);
  	
  	/*
  	 * Text
  	 */
  	Map<String, Object> context = new HashMap<String, Object>();
  	context.put("country_name", reg.getName());
  	
  	String mapText = mapTextFactory.processTemplate(context, "prText.ftl");
  	mapCanvas.drawTextFrame(mapText, frameConf.get("LEGEND"), 6, 10);
  	
  	mapSurface.finish();
  	
		if (reg.getOrientation().equals(Orientation.LANDSCAPE)) {
			SystemUtil.addBranding("pf_pr", reg.getFilePrefix(), mapConf.getName(), "branding_final_landscape");
		} else {
			SystemUtil.addBranding("pf_pr", reg.getFilePrefix(), mapConf.getName(), "branding_final");
		}
  }

  public static void drawSurveyMap(DataFrame df, Parasite parasite, MapConf mapConf, Region region, List<AdminUnit> adminPolys, Map<String, Rectangle> frameConf) throws Exception {
      
    	/*
    	 * Get envelope as resized by dataframe 
    	 */
    	Envelope resizedEnv = df.getEnvelope();
    	
    	
    	/*
    	 * Get country then use extent to create map canvas and get appropriate admin units.
    	 */
    	
    	HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
    	colours.put(0, Palette.GREY_20.get());
    	colours.put(1, Palette.GREY_40.get());
    	colours.put(2, Palette.GREY_60.get(0));
    	
    	/*
    	 * Draw limits layer
    	 */
    	ColourMap cm = new ColourMap();
    	cm.addColour(0, colours.get(0));
    	cm.addColour(1, colours.get(1));
    	cm.addColour(2, colours.get(2));
    	
    	cm.finish();
    	H5IntRaster.Transform tm = new H5IntRaster.Transform() {
    		@Override
    		public int transform(int inVal) {
    			return inVal;
    		}
    	};
    	
    	H5IntRaster h5r = pfLimsH5.getRaster(resizedEnv, tm, cm, 1);
    	df.addRasterLayer(h5r);
    	
    	/* Get admin units (level 0)
    	 * overlapping data frame and draw them, avoiding country in question
    	 * to produce a mask
    	 */
    	MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
    	Geometry tempG = rect;
    	
    	FillStyle greyFS = new FillStyle(Palette.WHITE.get());
    	List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
    	for(AdminUnit admin0: adminPolys) {
    			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(Palette.GREY_70.get(), 0.2));
    			adminPolySym.add(ps);
  		}
    	df.drawFeatures(adminPolySym);
    	
    	/*
    	 * Sea mask
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
    	
    	
			List<PRPoint> prPoints = prService.getPointsByRegion(region, parasite);
			for (PRPoint prPoint : prPoints) {
				df.drawPoint(prPoint.getLongitude(), prPoint.getLatitude(), prPoint.getColour());
			}
    	
    	/*
    	 * Draw the rest of the canvas
    	 */
    	Rectangle canvRect = frameConf.get("CANVAS");
    	PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", canvRect.width, canvRect.height);
    	MapCanvas mapCanvas = new MapCanvas(mapSurface, canvRect.width, canvRect.height);
    	mapCanvas.addDataFrame(df);
    	mapCanvas.drawDataFrames();
    
    	Rectangle frame = new Rectangle(20, 530, 430, 20);
    	mapCanvas.setScaleBar(frame, df.getScale(), 7);
    
    	/*
    	 * Draw the key.
    	 */
    	List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
    	legend.add(new MapKeyItem("Water", Palette.WATER.get()));
    	legend.add(new MapKeyItem("Malaria free", Palette.GREY_20.get()));
    	legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite), colours.get(1)));
    
    	Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
    	mapCanvas.drawKey(legendFrame, legend);
    	
			Map<String, Object> surveyStats = prService.getSurveysByRegion(region);
    	
    	/*
    	 * Title
    	 */
    	String mapTitle = String.format(mapConf.getTitle(), region.getDisplayName());
    	mapCanvas.setTitle(mapTitle, frameConf.get("TITLE"), 10);
    	
    	/*
    	 * Text
    	 */
    	Map<String, Object> context = new HashMap<String, Object>();
    	context.put("country_name", region.getName());
    	context.putAll(surveyStats);
    	
    	String mapText = mapTextFactory.processTemplate(context, "surveyText.ftl");
    	mapCanvas.drawTextFrame(mapText, frameConf.get("LEGEND"), 6, 10);
    	
    	mapSurface.finish();
    	
  		if (region.getOrientation().equals(Orientation.LANDSCAPE)) {
  			SystemUtil.addBranding("pf_pr", region.getFilePrefix(), mapConf.getName(), "branding_final_landscape");
  		} else {
  			SystemUtil.addBranding("pf_pr", region.getFilePrefix(), mapConf.getName(), "branding_final");
  		}
    }

  public static void drawUCMap(DataFrame df, Parasite parasite, MapConf mapConf, Region region, List<AdminUnit> adminPolys, Map<String, Rectangle> frameConf) throws Exception {
    
  	/*
  	 * Get envelope as resized by dataframe 
  	 */
  	Envelope resizedEnv = df.getEnvelope();
  	
  	
  	/*
  	 * Get country then use extent to create map canvas and get appropriate admin units.
  	 */
  	
  	HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
  	colours.put(0, Palette.GREY_20.get());
  	colours.put(1, Palette.GREY_40.get());
  	colours.put(2, Palette.GREY_60.get(0));
  	
  	/*
  	 * Draw limits layer
  	 */
  	ColourMap cm = new ColourMap();
  	cm.addColour(0, colours.get(0));
  	cm.addColour(1, colours.get(1));
  	cm.addColour(2, colours.get(2));
  	
  	cm.finish();
  	H5IntRaster.Transform tm = new H5IntRaster.Transform() {
  		@Override
  		public int transform(int inVal) {
  			return inVal;
  		}
  	};
  	
  	H5IntRaster h5r = pfLimsH5.getRaster(resizedEnv, tm, cm, 1);
  	df.addRasterLayer(h5r);
  	
  	/*
  	 * Draw PR uncertainty class layer
  	 */
  	HashMap<Integer, Colour> colours1 = new HashMap<Integer, Colour>();
  	colours1.put(0, Palette.ULTRA_BLUE.get());
  	colours1.put(1, Palette.MACAW_GREEN.get());
  	colours1.put(2, Palette.AUTUNITE_YELLOW.get());
  	
  	ColourMap cm1 = new ColourMap();
  	cm1.addColour(0, colours1.get(0));
  	cm1.addColour(1, colours1.get(1));
  	cm1.addColour(2, colours1.get(2));
  	
  	cm1.finish();
  	H5IntRaster.Transform tm1 = new H5IntRaster.Transform() {
  		@Override
  		public int transform(int inVal) {
  			return inVal;
  		}
  	};
  	
  	H5IntRaster h5r1 = pfUncertaintyClass.getRaster(resizedEnv, tm1, cm1, 5);
  	df.addRasterLayer(h5r1);
  	
  	
  	/* Get admin units (level 0)
  	 * overlapping data frame and draw them, avoiding country in question
  	 * to produce a mask
  	 */
  	MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
  	Geometry tempG = rect;
  	
  	FillStyle greyFS = new FillStyle(Palette.WHITE.get());
  	List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
  	for(AdminUnit admin0: adminPolys) {
  			PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(Palette.GREY_70.get(), 0.2));
  			adminPolySym.add(ps);
  	}
  	df.drawFeatures(adminPolySym);
  	
  	/*
  	 * Sea mask
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
  	
  	
  	List<PRPoint> prPoints = prService.getPointsByRegion(region, parasite);
  	for (PRPoint prPoint : prPoints) {
  		df.drawPoint(prPoint.getLongitude(), prPoint.getLatitude(), prPoint.getColour());
  	}
  	
  	/*
  	 * Draw the rest of the canvas
  	 */
  	Rectangle canvRect = frameConf.get("CANVAS");
  	PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", canvRect.width, canvRect.height);
  	MapCanvas mapCanvas = new MapCanvas(mapSurface, canvRect.width, canvRect.height);
  	mapCanvas.addDataFrame(df);
  	mapCanvas.drawDataFrames();
  
  	Rectangle frame = new Rectangle(20, 530, 430, 20);
  	mapCanvas.setScaleBar(frame, df.getScale(), 7);
  
  	/*
  	 * Draw the key.
  	 */
  	List<MapKeyItem> legend = new ArrayList<MapKeyItem>();
  	legend.add(new MapKeyItem("Water", Palette.WATER.get()));
  	legend.add(new MapKeyItem("Malaria free", Palette.GREY_20.get()));
  	legend.add(new MapKeyItem(String.format("<i>%s</i>API &lt; 0.1‰", parasite), colours.get(1)));
  
  	Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
  	mapCanvas.drawKey(legendFrame, legend);
  	
  	Map<String, Object> surveyStats = prService.getSurveysByRegion(region);
  	
  	/*
  	 * Title
  	 */
  	String mapTitle = String.format(mapConf.getTitle(), region.getDisplayName());
  	mapCanvas.setTitle(mapTitle, frameConf.get("TITLE"), 10);
  	
  	/*
  	 * Text
  	 */
  	Map<String, Object> context = new HashMap<String, Object>();
  	context.put("country_name", region.getName());
  	context.putAll(surveyStats);
  	
  	String mapText = mapTextFactory.processTemplate(context, "surveyText.ftl");
  	mapCanvas.drawTextFrame(mapText, frameConf.get("LEGEND"), 6, 10);
  	
  	mapSurface.finish();
  	
  	if (region.getOrientation().equals(Orientation.LANDSCAPE)) {
  		SystemUtil.addBranding("pf_pr", region.getFilePrefix(), mapConf.getName(), "branding_final_landscape");
  	} else {
  		SystemUtil.addBranding("pf_pr", region.getFilePrefix(), mapConf.getName(), "branding_final");
  	}
  }
  

}
