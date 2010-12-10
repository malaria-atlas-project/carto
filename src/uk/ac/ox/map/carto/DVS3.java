package uk.ac.ox.map.carto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.CountryDVS;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.vector.Anopheline;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
import uk.ac.ox.map.base.service.VectorService;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.LegendItem;
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
import uk.ac.ox.map.imageio.FltReader;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

public class DVS3 {

  static final AdminUnitService adminUnitService = new AdminUnitService();
  static final CartoService cartoService = new CartoService();
  static final MapTextFactory mapTextFactory = new MapTextFactory();
  static final VectorService vectorService = new VectorService();
  final static String baseDir = "/home/will/map1_public/data/vector/species_mapping_outputs/model_results/Anopheles.%s_results/probability-map";

  static String basicFilePaths = "";

  public static void main(String[] args) throws Exception {
    Gtk.init(args);

    Country country = adminUnitService.getCountry("UGA");
    List<CountryDVS> countryDVS = adminUnitService.getCountryDVS(country);

    /*
     * Get the DVS
     */
    List<Anopheline> anos = new ArrayList<Anopheline>();
    for (CountryDVS cdvs : countryDVS) {
      anos.add(vectorService.getAnophelineById(cdvs.getAnoId()));
    }

    /*
     * Build the list of file paths forming the composite and build the raster
     */
    List<String> floatFilePaths = new ArrayList<String>();
    for (Anopheline anopheline : anos) {
      floatFilePaths.add(String.format(baseDir, anopheline.getAbbreviation()));
    }
    RasterLayer ras = FltReader.getCompositeRaster(floatFilePaths);

    Map<String, Rectangle> frameConfP = new HashMap<String, Rectangle>();
    frameConfP.put("dataFrame", new Rectangle(20, 40, 460, 460));
    frameConfP.put("scalebar", new Rectangle(20, 525, 430, 20));
    frameConfP.put("legendFrame", new Rectangle(365, 545, 170, 200));
    frameConfP.put("continuousScale", new Rectangle(390, 675, 100, 15));
    frameConfP.put("titleFrame", new Rectangle(0, 3, 500, 0));
    frameConfP.put("mapTextFrame", new Rectangle(50, 545, 300, 0));

    drawDVSMap(country, anos, ras, frameConfP, 500, 707);
  }

  public static void drawDVSMap(Country country, List<Anopheline> anos,
      RasterLayer ras, Map<String, Rectangle> frameConf, int w, int h)
      throws Exception {

    /*
     * Get country then use extent to create map canvas and get appropriate
     * admin units.
     */
    Envelope env = new Envelope(country.getMinX(), country.getMaxX(),
        country.getMinY(), country.getMaxY());
    if (country.getId().equals("ARG")) {
      env = new Envelope(-73.5829, -53.5918, -55.0613, -21.7812);
    }

    /*
     * expand by 5% (1/20 * width)
     */
    env.expandBy(env.getWidth() / 20);
    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, frameConf.get("dataFrame"), null)
        .build();
    df.setBackgroundColour(Palette.WATER.get());

    /*
     * Get envelope as resized by dataframe
     */
    Envelope resizedEnv = df.getEnvelope();
    List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);

    /*
     * Get admin units (level 0) overlapping data frame and draw them, avoiding
     * country in question to produce a mask
     */
    MultiPolygon rect = (MultiPolygon) cartoService.getWorldRect().getGeom();
    Geometry tempG = rect;

    FillStyle greyFS = new FillStyle(Palette.GREY_30.get());
    List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
    for (AdminUnit admin0 : adminUnits) {
      if (admin0.getCountryId().equals(country.getId())) {
        /*
         * get difference for sea_mask
         */
        OverlayOp ol = new OverlayOp(tempG, admin0.getGeom());
        tempG = ol.getResultGeometry(OverlayOp.DIFFERENCE);
      } else {
        PolygonSymbolizer ps = new PolygonSymbolizer(
            (MultiPolygon) admin0.getGeom(), greyFS, new LineStyle(
                Palette.GREY_70.get(), 0.2));
        adminPolySym.add(ps);
      }
    }

    // Add the externally generated raster layer
    df.addRasterLayer(ras);

    /*
     * Sea mask
     */
    MultiPolygon mp;
    if (tempG.getNumGeometries() > 1) {
      mp = (MultiPolygon) tempG;
    } else {
      Polygon p = (Polygon) tempG;
      mp = new MultiPolygon(new Polygon[] { p }, new GeometryFactory());
    }
    PolygonSymbolizer aps = new PolygonSymbolizer(mp, new FillStyle(
        Palette.WATER.get()), new LineStyle(Palette.WATER.get(), 0.2));
    df.drawMultiPolygon(aps);
    df.drawFeatures(adminPolySym);

    /*
     * Draw on water bodies
     */
    List<PolygonSymbolizer> waterBodies = new ArrayList<PolygonSymbolizer>();
    List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
    FillStyle fs = new FillStyle(Palette.WATER.get(), FillType.SOLID);
    for (WaterBody waterBody : water) {
      PolygonSymbolizer ps = new PolygonSymbolizer(
          (MultiPolygon) waterBody.getGeom(), fs, new LineStyle(
              Palette.WATER.get(), 0.2));
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
     * Draw the legend.
     */
    List<LegendItem> legend = new ArrayList<LegendItem>();
    legend.add(new LegendItem("Water", Palette.WATER.get()));

    Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
    mapCanvas.drawLegend(legendFrame, legend);

    /*
     * Title
     */
    String mapTitle = String.format("The dominant vector species in %s",
        country.getName());
    mapCanvas.setTitle(mapTitle, frameConf.get("titleFrame"), 10);

    /*
     * Text Map<String, Object> context = new HashMap<String, Object>();
     * context.put("country_name", country.getName());
     * 
     * String mapText = mapTextFactory.processTemplate(context, "prText.ftl");
     * mapCanvas.drawTextFrame(mapText, frameConf.get("mapTextFrame"), 6, 10);
     */
    StringBuilder sb = new StringBuilder();
    for (Anopheline ano : anos) {
      sb.append(ano.getScientificName());
      sb.append("<br>");
    }
    mapCanvas
        .drawTextFrame(sb.toString(), frameConf.get("mapTextFrame"), 6, 10);

    /*
     * Colour triangle
     */
    Pixbuf pbTri = new Pixbuf("/home/will/workspace/Carto/rgbcube.png");
    int ctX = 365;
    int ctY = 600;
    int ctW = 80;
    int ctH = 80;
    mapCanvas.drawImage(ctX, ctY, ctW, ctH, pbTri);
    mapCanvas.annotateMap("<i>An. " + anos.get(0).getSpecies() + "</i>", ctX
        + (ctW / 2), ctY, Anchor.CB);
    mapCanvas.annotateMap("<i>An. " + anos.get(1).getSpecies() + "</i>", ctX
        + ctW, ctY + ctH, Anchor.CT);
    mapCanvas.annotateMap("<i>An. " + anos.get(2).getSpecies() + "</i>", ctX,
        ctY + ctH, Anchor.CT);

    mapSurface.finish();

    SystemUtil.addBranding("dvs123", country.getId(), "dvs123",
        "branding_final");

  }
}
