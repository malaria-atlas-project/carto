package uk.ac.ox.map.carto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.PixbufFormat;
import org.gnome.gtk.Gtk;
import org.gnome.rsvg.Rsvg;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.pr.Parasite;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
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
import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.h5.H5FloatRaster;
import uk.ac.ox.map.h5.H5IntRaster;
import uk.ac.ox.map.h5.H5RasterFactory;
import uk.ac.ox.map.imageio.ColourMap;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

public class CountryPRMaps {
  
	static final AdminUnitService adminUnitService = new AdminUnitService();
	static final CartoService cartoService = new CartoService();
	private static final MapTextFactory mapTextFactory = new MapTextFactory();
	static  H5RasterFactory h5RFpfLims;
	static  H5RasterFactory h5RFPfPR;
	static  H5RasterFactory h5RFPfStd;
  
	public static void main(String[] args) throws Exception {
	  Gtk.init(args);
	  Rsvg.init();
  	h5RFpfLims = new H5RasterFactory("/home/will/workspace/ImageBase/pf_limits.h5");
  	h5RFPfPR = new H5RasterFactory("/home/will/workspace/ImageBase/pf_pr.h5");
  	h5RFPfStd = new H5RasterFactory("/home/will/workspace/ImageBase/pf_std.h5");
  	
	  Parasite para = Parasite.Pf;
	  
  	ContinuousScale cs = new ContinuousScale(PortraitLayout.CONTINUOUSSCALE.get(), "Probability", "(in units of <i>" + Parasite.Pf.toString() + "</i>PR<span rise='-3000' size='x-small'>2-10</span>, 0-100%)");
  	cs.addColorStopRGB("0",   0.0, 1.0, 1.0, 0, false);
  	cs.addColorStopRGB("100", 1.0, 1.0, 0.0, 0, false);
  	cs.finish();
	  
	  List<Country> countries = adminUnitService.getMECountries();
	  for (Country country : countries) {
	    if(country.getPfEndemic()) {
        drawCountryPRMap(para, country, cs);
	    }
    }
	  
	}
	
	public static void drawRegionPRMaps(Parasite para){
	  
	}
	
	public static void drawRegionPRMap(Parasite para){
	  
	}
	
	public static void drawCountryPRMaps(Parasite para, ContinuousScale cs) {
	  
	}
	
	public static void drawCountryPRMap(Parasite para, Country country, ContinuousScale cs) throws Exception{
	  
		Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY()); 
  	env.expandBy(env.getWidth()/20);
  	
	  String regionName = country.getName();
	  
  	String mapTitle = String.format("The spatial distribution of <i>%s</i> malaria endemicity in %s", para.getSpecies(), regionName);
  	
		
  	/*
  	 * Create dataframe with specified envelope
  	 */
  	DataFrame df = new DataFrame.Builder(env, PortraitLayout.DATAFRAME.get(), null).build();
  	df.setBackgroundColour(Palette.WATER.get());
  	Envelope resizedEnv = df.getEnvelope();
  	
  	List<AdminUnit> maskPolys = adminUnitService.getMaskByCountry(resizedEnv, country.getId());
  	drawPRMap(df, para, mapTitle, regionName, h5RFPfPR, cs, h5RFpfLims, maskPolys);
	}
	
  

  public static void drawPRMap(DataFrame df, Parasite parasite, String title, String regionName, H5RasterFactory h5RFPr, ContinuousScale cs, H5RasterFactory h5RFPfLims, List<AdminUnit> adminPolys) throws Exception {
  	
  	/*
  	 * Get country then use extent to create map canvas and get appropriate admin units.
  	 */
  	
  	HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
  	colours.put(0, Palette.GREY_20.get());
  	colours.put(1, Palette.GREY_40.get());
  	colours.put(2, Palette.GREY_60.get(0));
  	
  	
  	/*
  	 * Get envelope as resized by dataframe 
  	 */
  	Envelope resizedEnv = df.getEnvelope();
  	
  	/*
  	 * Add PR layer
  	 */
  	
  	H5FloatRaster ras = h5RFPr.getRaster(resizedEnv, new H5FloatRaster.Transform() {
  		@Override
  		public float transform(float fV) {
  			return fV;
  		}
  	}, cs);
  	
  	ras.getPixbuf().save("/tmp/tmpx.png", PixbufFormat.PNG);
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
  	
  	H5IntRaster h5r = h5RFPfLims.getRaster(resizedEnv, tm, cm);
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
  	Rectangle canvRect = PortraitLayout.CANVAS.get();
  	PdfSurface mapSurface = new PdfSurface(OutputUtil.getOutputLocation("pf_pr", regionName+"_Pf_PR"), canvRect.width, canvRect.height);
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
  	mapCanvas.setTitle(title, PortraitLayout.TITLE.get(), 10);
  	
  	/*
  	 * Text
  	 */
  	Map<String, Object> context = new HashMap<String, Object>();
  	context.put("country_name", regionName);
  	
  	String mapText = mapTextFactory.processTemplate(context, "prText.ftl");
  	mapCanvas.drawTextFrame(mapText, PortraitLayout.LEGEND.get(), 6, 10);
  	
  	OverlayUtil.drawStaticOverlays(mapCanvas);
  	
  	mapSurface.finish();
  }

}
