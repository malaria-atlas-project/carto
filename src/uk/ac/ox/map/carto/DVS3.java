package uk.ac.ox.map.carto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gdk.PixbufFormat;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.CountryDVS;
import uk.ac.ox.map.base.model.adminunit.MAPRegion;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.vector.Anopheline;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
import uk.ac.ox.map.base.service.VectorService;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.MapKeyItem;
import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.canvas.Rectangle.Anchor;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.text.MapTextFactory;
import uk.ac.ox.map.carto.util.SystemUtil;
import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.imageio.FltReader;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

public class DVS3 {

  static final Colour waterColour = Palette.GREY_10.get();
  static final AdminUnitService adminUnitService = new AdminUnitService();
  static final CartoService cartoService = new CartoService();
  static final MapTextFactory mapTextFactory = new MapTextFactory();
  static final VectorService vectorService = new VectorService();
  final static String baseDir = "/home/will/map1_public/data/vector/species_mapping_outputs/model_results/Anopheles.%s_results/probability-map";

  static String basicFilePaths = "";

  public static void main(String[] args) throws Exception {
    Gtk.init(args);
    
    Map<String, Rectangle> frameConfP = new HashMap<String, Rectangle>();
    frameConfP.put("dataFrame", new Rectangle(20, 40, 460, 460));
    frameConfP.put("scalebar", new Rectangle(120, 487, 100, 10));
    frameConfP.put("continuousScale", new Rectangle(390, 675, 100, 15));
    frameConfP.put("titleFrame", new Rectangle(0, 3, 500, 0));
    frameConfP.put("mapTextFrame", new Rectangle(20, 520, 340, 0));
    
    frameConfP.put("keyFrame", new Rectangle(410, 520, 170, 200));
    frameConfP.put("cubeFrame", new Rectangle(400, 560, 110, 110));
    
    Country country = adminUnitService.getCountry("KEN");
    drawCountryDVSMap(country, frameConfP);
    country = adminUnitService.getCountry("BRA");
    drawCountryDVSMap(country, frameConfP);
    country = adminUnitService.getCountry("UGA");
    drawCountryDVSMap(country, frameConfP);
    country = adminUnitService.getCountry("COD");
    drawCountryDVSMap(country, frameConfP);
    
    drawRegionDVSMap("Africa+", frameConfP);
  }

  private static void drawRegionDVSMap(String regionName, Map<String, Rectangle> frameConfP) throws IOException, Exception {
    
    Country country = adminUnitService.getCountry("UGA");
    List<CountryDVS> countryDVS = adminUnitService.getCountryDVS(country);
  
    /*
     * Get the DVS
     */
    List<Anopheline> anos = new ArrayList<Anopheline>();
    for (CountryDVS cdvs : countryDVS) {
      anos.add(vectorService.getAnophelineById(cdvs.getAnoId()));
    }
  
		MAPRegion region = adminUnitService.getRegion(regionName);
		Envelope env = new Envelope(region.getMinX(), region.getMaxX(), region.getMinY(), region.getMaxY());
    /*
     * expand by 5% (1/20 * width)
     */
    env.expandBy(env.getWidth() / 20);
    
    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, frameConfP.get("dataFrame"), null) .build();
    df.setBackgroundColour(waterColour);
  
    /*
     * Get envelope as resized by dataframe
     */
    Envelope resizedEnv = df.getEnvelope();
    
    /*
     * Get admin units (level 0) overlapping data frame and draw them, avoiding
     * country in question to produce a mask
     */
    List<AdminUnit> regionAdminUnits = adminUnitService.getAdminUnitsByRegion(region);
    List<AdminUnit> maskAdminUnits = adminUnitService.getMaskByRegion(resizedEnv, region);
    
  
    drawDVSMap(regionName, df, regionAdminUnits, maskAdminUnits, anos, frameConfP, 750, 707, regionName);
  }

  private static void drawCountryDVSMap(Country country, Map<String, Rectangle> frameConfP) throws IOException, Exception {
    
    
    List<CountryDVS> countryDVS = adminUnitService.getCountryDVS(country);

    /*
     * Get the DVS
     */
    List<Anopheline> anos = new ArrayList<Anopheline>();
    for (CountryDVS cdvs : countryDVS) {
      anos.add(vectorService.getAnophelineById(cdvs.getAnoId()));
    }

    Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
    /*
     * expand by 5% (1/20 * width)
     */
    env.expandBy(env.getWidth() / 20);
    
    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, frameConfP.get("dataFrame"), null) .build();
    df.setBackgroundColour(waterColour);
    
    /*
     * Get envelope as resized by dataframe
     */
    Envelope resizedEnv = df.getEnvelope();
    List<AdminUnit> maskAdminUnits = adminUnitService.getMaskByCountry(resizedEnv, country.getId());
    List<AdminUnit> regionAdminUnit = adminUnitService.getAdminUnitsByCountryId(country.getId());
    
    drawDVSMap(country.getName(), df, regionAdminUnit, maskAdminUnits, anos, frameConfP, 750, 707, country.getId());
  }
  
  public static DataFrame buildOverviewFrame(String floatFilePath, Envelope env, ContinuousScale cs, Rectangle rectangle, List<PolygonSymbolizer> adminPolySym) throws IOException {
    
    RasterLayer ras = FltReader.openFloatFile(floatFilePath, cs, env);
//    ras.getPixbuf().save("/tmp/pixbuftest.tif", PixbufFormat.TIFF);
    DataFrame df2 = new DataFrame.Builder(env, rectangle, null).hasGrid(false).build();
    df2.setBackgroundColour(waterColour);
    df2.addRasterLayer(ras);
    df2.drawFeatures(adminPolySym);
    return df2;
  }

  public static void drawDVSMap(String areaName, DataFrame df, List<AdminUnit> regionAdminUnits, List<AdminUnit> maskAdminUnits, List<Anopheline> anos, Map<String, Rectangle> frameConf, int w, int h, String fileName) throws Exception {
    
    Envelope env = df.getEnvelope();
    
    /*
     * Build the list of file paths forming the composite and build the raster
     */
    List<String> floatFilePaths = new ArrayList<String>();
    for (Anopheline anopheline : anos) {
      floatFilePaths.add(String.format(baseDir, anopheline.getAbbreviation()));
    }
    RasterLayer ras = FltReader.getCompositeRaster(floatFilePaths);
    
    // Add the externally generated raster layer
    df.addRasterLayer(ras);
    
    /*
     * Get admin units (level 0) overlapping data frame and draw them, avoiding
     * country in question to produce a mask
     */
    MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
    Geometry tempG = rect;
    for (AdminUnit adminUnit : regionAdminUnits) {
      OverlayOp ol = new OverlayOp(tempG, adminUnit.getGeom());
      tempG = ol.getResultGeometry(OverlayOp.DIFFERENCE);
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
  	PolygonSymbolizer aps = new PolygonSymbolizer(mp, new FillStyle(waterColour), new LineStyle(waterColour, 0.2));
  	df.drawMultiPolygon(aps);

    FillStyle greyFS = new FillStyle(Palette.GREY_40.get());
    List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
    for (AdminUnit admin0 : maskAdminUnits) {
      PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) admin0.getGeom(), greyFS, new LineStyle( Palette.GREY_70.get(), 0.2));
      adminPolySym.add(ps);
    }
    df.drawFeatures(adminPolySym);
    
    /**
     * build list of overview frames 
     */
    List<DataFrame> overviewFrames = new ArrayList<DataFrame>(); 
    List<ContinuousScale> continuousScales = new ArrayList<ContinuousScale>(); 
    double leftEdge = 550;
    double ovfW = 150;
    double ovfH = 150;
    double ovfSpace = 60;
    double ovfTopOrig = 40;
    double csOffsetY = 20 + ovfTopOrig;
    /*
     *  Overview 1
     */
    csOffsetY += ovfH;
    ContinuousScale cs1 = new ContinuousScale(new Rectangle(leftEdge, csOffsetY, 100, 15), anos.get(0).getScientificAbbreviation(), null); 
    cs1.addColorStopRGB("0", 0, 0, 0, 0, false);
    cs1.addColorStopRGB("", 0.5, 0, 0, 0, false);
    cs1.addColorStopRGB("0.5", 0.5, 0.5, 0, 0, false);
    cs1.addColorStopRGB("1", 1, 1, 0, 0, false);
    cs1.finish();
    overviewFrames.add(buildOverviewFrame(floatFilePaths.get(0), env, cs1, new Rectangle(leftEdge, ovfTopOrig, ovfW, ovfH), adminPolySym));
    continuousScales.add(cs1);
    
    /*
     *  Overview 2
     */
    ovfTopOrig += (ovfH+ovfSpace);
    csOffsetY += ovfH + ovfSpace;
    ContinuousScale cs2 = new ContinuousScale(new Rectangle(leftEdge, csOffsetY, 100, 15), anos.get(1).getScientificAbbreviation(), null); 
    cs2.addColorStopRGB("0", 0, 0, 0, 0, false);
    cs2.addColorStopRGB("", 0.5, 0, 0, 0, false);
    cs2.addColorStopRGB("0.5", 0.5, 0, 0, 0.5, false);
    cs2.addColorStopRGB("1", 1, 0, 0, 1, false);
    cs2.finish();
    overviewFrames.add(buildOverviewFrame(floatFilePaths.get(1), env, cs2, new Rectangle(leftEdge, ovfTopOrig, ovfW, ovfH), adminPolySym));
    continuousScales.add(cs2);
    
    /*
     *  Overview 3
     */
    ovfTopOrig += (ovfH+ovfSpace);
    csOffsetY += ovfH + ovfSpace;
    ContinuousScale cs3 = new ContinuousScale(new Rectangle(leftEdge, csOffsetY, 100, 15), anos.get(2).getScientificAbbreviation(), null); 
    cs3.addColorStopRGB("0", 0, 0, 0, 0, false);
    cs3.addColorStopRGB("", 0.5, 0, 0, 0, false);
    cs3.addColorStopRGB("0.5", 0.5, 0, 0.5, 0, false);
    cs3.addColorStopRGB("1", 1, 0, 1, 0, false);
    cs3.finish();
    overviewFrames.add(buildOverviewFrame(floatFilePaths.get(2), env, cs3, new Rectangle(leftEdge, ovfTopOrig, ovfW, ovfH), adminPolySym));
    continuousScales.add(cs3);
    /*
     * Draw on water bodies
     */
    List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
    List<WaterBody> water = adminUnitService.getWaterBodies(df.getEnvelope());
    FillStyle fs = new FillStyle(waterColour, FillType.SOLID);
    for (WaterBody waterBody : water) {
      PolygonSymbolizer ps = new PolygonSymbolizer(
          (MultiPolygon) waterBody.getGeom(), fs, new LineStyle(waterColour, 0.2));
      waterBodies.add(ps);
    }
    df.drawFeatures(waterBodies);

    /*
     * Draw the rest of the canvas
     */
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
    MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
    mapCanvas.addDataFrame(df);
    for (DataFrame overviewFrame: overviewFrames) {
      mapCanvas.addDataFrame(overviewFrame);
    }
    mapCanvas.drawDataFrames();

    mapCanvas.setScaleBar(frameConf.get("scalebar"), df.getScale(), 5, "km");

    /*
     * Draw the legend.
     */
    List<MapKeyItem> key = new ArrayList<MapKeyItem>();
    key.add(new MapKeyItem("Water", waterColour));

    mapCanvas.drawKey(frameConf.get("keyFrame"), key, 6);
    
    /*
     * Draw scales
     */
    for (ContinuousScale cs : continuousScales) {
      cs.draw(mapCanvas);
    }

    /*
     * Title
     */
    String mapTitle = String.format("The top three dominant vector species in %s", areaName);
    mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);

    /*
     * Text
     */
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("anos", anos);
    context.put("areaName", areaName);

    String mapText = mapTextFactory.processTemplate(context, "dvsText.ftl");
    mapCanvas.drawTextFrame(mapText, frameConf.get("mapTextFrame"), 5.5f, 10);

    /*
     * Colour triangle
     */
    Pixbuf pbTri = new Pixbuf("/home/will/workspace/Carto/rgbcube.png");
    Rectangle cf = frameConf.get("cubeFrame");
    mapCanvas.drawImage(cf, pbTri);
    mapCanvas.annotateMap(anos.get(0).getScientificAbbreviation(), cf.x + (cf.width / 2), cf.y, Anchor.CB);
    mapCanvas.annotateMap(anos.get(1).getScientificAbbreviation(), cf.x + cf.width, cf.y + cf.height, Anchor.CT);
    mapCanvas.annotateMap(anos.get(2).getScientificAbbreviation(), cf.x, cf.y + cf.height, Anchor.CT);

    mapSurface.finish();

    SystemUtil.addBranding("dvs123", fileName, "dvs123", "branding_dvs123");

  }
}
