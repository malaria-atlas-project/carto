package uk.ac.ox.map.carto;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.PixbufFormat;
import org.gnome.gtk.Gtk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.AdminUnitRisk;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.adminunit.World;
import uk.ac.ox.map.base.model.carto.WorldPoly;
import uk.ac.ox.map.base.model.pr.Parasite;
import uk.ac.ox.map.base.model.site.Site;
import uk.ac.ox.map.base.model.vector.Anopheline;
import uk.ac.ox.map.base.model.vector.SitePresenceAbsence;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
import uk.ac.ox.map.base.service.VectorService;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.util.GeometryUtil;
import uk.ac.ox.map.carto.util.SystemUtil;
import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.h5.H5FloatRaster;
import uk.ac.ox.map.h5.H5RasterFactory;
import uk.ac.ox.map.imageio.FltReader;
import uk.ac.ox.map.imageio.FltReader2;
import uk.ac.ox.map.imageio.RasterLayer;

import com.sun.java.swing.plaf.gtk.GTKColorType;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

public class AMEMaps {

  static Logger logger = LoggerFactory.getLogger(AMEMaps.class);

  static VectorService vectorService = new VectorService();
  static final CartoService cartoService = new CartoService();

  static final AdminUnitService adminUnitService = new AdminUnitService();
  static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(), 0.2);

  public static void main(String[] args) throws OutOfMemoryError, Exception {
    Gtk.init(null);
    // drawAMEMaps(Parasite.Pf);
    // drawAMEMaps(Parasite.Pv);

//    drawScaleGraphic();
//    if (1 == 1)
//      return;

    H5RasterFactory h5RFpop = new H5RasterFactory("/home/will/map1_public/mbgw2/pop10.h5");
    Country c = adminUnitService.getCountry("LKA");
    drawPopMap(c, h5RFpop);

    List<String> abbr = new ArrayList<String>();
    abbr.add("culicifacies");
    abbr.add("annularis");
    abbr.add("aconitus");
    abbr.add("subpictus");

    Map<String, Rectangle> frameConfLS = new HashMap<String, Rectangle>();
    frameConfLS.put("dataFrame", new Rectangle(0, 0, 400, 400));
    frameConfLS.put("continuousScale", new Rectangle(200, 200, 110, 12));

    for (String a : abbr) {
      Anopheline ano = vectorService.getAnophelineByAbbreviation(a);
      // String.format("/home/will/map1_public/data/vector/species_mapping_outputs/model_results/Anopheles.%s_results",
      // ano.getAbbreviation());
      final String anoDir = String.format("/home/will/map1_public/data/vector/species_mapping_outputs/Asia.eo_occurrence/Anopheles.%s_results", ano.getAbbreviation());
      File anoFolder = new File(anoDir);

      // Country c = adminUnitService.getCountry("LKA");

      Envelope env = new Envelope(c.getMinX(), c.getMaxX(), c.getMinY(), c.getMaxY());
      env.expandBy(env.getWidth() / 20);
      createAnoMap(ano, anoFolder, env, frameConfLS, c.getId());

    }

  }

  public static void drawScaleGraphic() throws Exception {

    int w = 500;
    int h = 707;

    PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
    MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
    mapCanvas.setFontSize(8);

    {
      Rectangle rect = new Rectangle(10, 40, 15, 90);
      ContinuousScale cs = new ContinuousScale(rect, "Population", null);

      double incr = 1d / 6;
      DecimalFormat decF = new DecimalFormat("#,###,###");

      cs.addColorStopRGB("&lt;1", incr * 0, 26d / 255, 152d / 255, 80d / 255, false);
      cs.addColorStopRGB(decF.format(Math.pow(10, 1)), incr * 1, 0, 0, 0, true);
      cs.addColorStopRGB(decF.format(Math.pow(10, 2)), incr * 2, 0, 0, 0, true);
      cs.addColorStopRGB(decF.format(Math.pow(10, 3)), incr * 3, 234d / 255, 230d / 255, 138d / 255, false);
      cs.addColorStopRGB(decF.format(Math.pow(10, 4)), incr * 4, 0, 0, 0, true);
      cs.addColorStopRGB(decF.format(Math.pow(10, 5)), incr * 5, 0, 0, 0, true);
      cs.addColorStopRGB(decF.format(Math.pow(10, 6)), incr * 6, 234d / 255, 4d / 255, 3d / 255, false);
      cs.finish();
      cs.draw(mapCanvas);
    }
    {
      Rectangle rect = new Rectangle(10, 200, 110, 12);
      ContinuousScale cs = new ContinuousScale(rect, "Probability", null);
      cs.addColorStopRGB("0", 0.0, 0.0, 0.0, 0.0, false);
      cs.addColorStopRGB(null, 0.5, 0.5, 0.5, 0.5, false);
      cs.addColorStopRGB("0.5", 0.5, 1, 0.8, 0.8, false);
      cs.addColorStopRGB("1", 1, 0.870588235, 0.188235294, 0.152941176, false);
      cs.finish();
      cs.draw(mapCanvas);
    }

    mapSurface.finish();

  }

  public static void drawPopMap(Country country, H5RasterFactory h5RF) throws Exception {

    /*
     * Get country then use extent to create map canvas and get appropriate
     * admin units.
     */
    Envelope env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());

    /*
     * expand by 5% (1/20 * width)
     */
    env.expandBy(env.getWidth() / 20);
    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, new Rectangle(20, 40, 460, 460), "/tmp/lka.pdf").build();
    df.setBackgroundColour(Palette.WATER.get());

    /*
     * Get envelope as resized by dataframe
     */
    Envelope resizedEnv = df.getEnvelope();
    List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);

    logger.debug("got admin units");

    /*
     * Add population layer
     */
    ContinuousScale cs = new ContinuousScale(new Rectangle(390, 600, 15, 90), "Population", null);

    double incr = 1d / 6;
    DecimalFormat decF = new DecimalFormat("#,###,###");

    cs.addColorStopRGB("&lt;1", incr * 0, 26d / 255, 152d / 255, 80d / 255, false);
    cs.addColorStopRGB(decF.format(Math.pow(10, 1)), incr * 1, 0, 0, 0, true);
    cs.addColorStopRGB(decF.format(Math.pow(10, 2)), incr * 2, 0, 0, 0, true);
    cs.addColorStopRGB(decF.format(Math.pow(10, 3)), incr * 3, 234d / 255, 230d / 255, 138d / 255, false);
    cs.addColorStopRGB(decF.format(Math.pow(10, 4)), incr * 4, 0, 0, 0, true);
    cs.addColorStopRGB(decF.format(Math.pow(10, 5)), incr * 5, 0, 0, 0, true);
    cs.addColorStopRGB(decF.format(Math.pow(10, 6)), incr * 6, 234d / 255, 4d / 255, 3d / 255, false);
    cs.finish();

    logger.debug("set up colour scale");

    H5FloatRaster ras = h5RF.getRaster(resizedEnv, new H5FloatRaster.Transform() {
      @Override
      public float transform(float fV) {
        if (fV < 1) {
          fV = 1;
        }
        fV = (float) (Math.log10(fV)) / 6;
        if (fV < 0) {
          fV = 0;
        }
        return fV;
      }
    }, cs, 1);

    df.addRasterLayer(ras);

    logger.debug("added raster layer");

    /*
     * Get admin units (level 0) overlapping data frame and draw them, avoiding
     * country in question to produce a mask
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

    logger.debug("sea mask done");

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

    logger.debug("waterbodies done");

    df.finish();

  }

  public static void drawAMEMaps(Parasite p) throws OutOfMemoryError, Exception {
    List<String> toProcess = new ArrayList<String>();
    List<Country> megCountries = adminUnitService.getEliminationCountries();
    // toProcess.add("LKA");
    // toProcess.add("THA");
    // toProcess.add("VNM");
    // toProcess.add("MMR");
    // toProcess.add("KHM");
    // toProcess.add("LAO");
    // toProcess.add("CHN");

    // toProcess.add("KGZ");
    // toProcess.add("GEO");
    // toProcess.add("AZE");

    // toProcess.add("MEX");
    // toProcess.add("TUR");
    // toProcess.add("ARG");

    // toProcess.add("CHN");

    // List to append all risk units to
    // List<AdminUnitRisk> riskUnits = adminUnitService.getYunnanRiskUnit(p);
    List<AdminUnitRisk> riskUnits = new ArrayList<AdminUnitRisk>();

    for (Country country : megCountries) {
      if (!toProcess.contains(country.getId())) {
        // continue;
      }
      List<AdminUnitRisk> countryRiskUnits = adminUnitService.getRiskAdminUnits(country, p);
      riskUnits.addAll(countryRiskUnits);
      Envelope env;
      // if (country.getId().equals("CHN")) {
      // env = new Envelope(97.5291, 106.191, 21.143, 29.227);
      // } else
      if (country.getId().equals("ARG")) {
        env = new Envelope(-73.5829, -53.5918, -55.0613, -21.7812);
      } else {
        env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
      }

      if (p.equals(Parasite.Pf) && country.getPfEndemic()) {
        drawAMEMap(countryRiskUnits, country.getId(), env, p);
      }
      if (p.equals(Parasite.Pv) && country.getPvEndemic()) {
        drawAMEMap(countryRiskUnits, country.getId(), env, p);
      }
    }
    /*
     * Envelope env = new Envelope(); for (AdminUnitRisk adminUnitRisk :
     * riskUnits) {
     * env.expandToInclude(adminUnitRisk.getGeom().getEnvelopeInternal()); }
     */

    // drawAMEMap(riskUnits, "GMSR", env, p);

  }

  public static void drawAMEMap(List<AdminUnitRisk> riskUnits, String areaName, Envelope env, Parasite parasite) throws OutOfMemoryError, Exception {

    env.expandBy(env.getWidth() / 20);

    // String dfOutputLoc =
    // String.format("/home/will/c/Temp/maps/AME_svg/%s_%s.svg", areaName,
    // parasite.toString());
    String dfOutputLoc = String.format("/home/will/c/Temp/maps/AME/%s_%s.pdf", areaName, parasite.toString());
    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, new Rectangle(0, 0, 460, 460), dfOutputLoc).build();
    df.setBackgroundColour(Palette.WATER.get());

    /*
     * Get envelope as resized by dataframe Get admin units (level 0)
     * overlapping data frame and draw them
     */
    Envelope resizedEnv = df.getEnvelope();
    GeometryFactory gf = new GeometryFactory();
    Geometry envGeom = gf.toGeometry(resizedEnv);
    List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);

    FillStyle greyFS = new FillStyle(Palette.GREY_20.get());
    FillStyle whiteFS = new FillStyle(Palette.WHITE.get());
    List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();
    for (AdminUnit admin0 : adminUnits) {

      // Intersect. If no overlap, get geomcollection with 0 geometries
      OverlayOp ol = new OverlayOp(envGeom, admin0.getGeom());
      Geometry tempG = ol.getResultGeometry(OverlayOp.INTERSECTION);

      FillStyle fs = greyFS;
      if (areaName.equals(admin0.getCountryId())) {
        fs = whiteFS;
      }

      if (tempG.getNumGeometries() > 0) {
        PolygonSymbolizer ps = new PolygonSymbolizer(GeometryUtil.toMultiPolygon(gf, tempG), fs, defaultLineStyle);
        adminPolySym.add(ps);
      }
    }
    df.drawFeatures(adminPolySym);

    /*
     * Draw the risk units
     */
    HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
    colours.put(0, Palette.WHITE.get());
    colours.put(1, Palette.ROSE.get());
    colours.put(2, Palette.CORAL.get());

    List<PolygonSymbolizer> pfFeats = new ArrayList<PolygonSymbolizer>();

    for (AdminUnitRisk adminUnitRisk : riskUnits) {
      if (adminUnitRisk.getRisk() > 0) {
        FillStyle fs = new FillStyle(colours.get(adminUnitRisk.getRisk()), FillType.SOLID);
        PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) adminUnitRisk.getGeom(), fs, defaultLineStyle);
        pfFeats.add(ps);
      }
    }
    df.drawFeatures(pfFeats);
    df.finish();
  }

  public static void createAnoMap(Anopheline ano, File anoFolder, Envelope env, Map<String, Rectangle> frameConfLS, String countryId) throws Exception {

    String fn = String.format("/tmp/%s_%s.pdf", countryId, ano.getAbbreviation());

    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, frameConfLS.get("dataFrame"), fn).backgroundColour(Palette.WATER.get()).gridColour(Palette.GREY_70.get()).build();

    /*
     * Get colour scale for map gradient and for the image transform.
     */

    // TODO: XML config file?? AMERICAS VERSION
    // ContinuousScale cs = new ContinuousScale(new Rectangle(400, 70, 15, 100),
    // "Probability", null);
    ContinuousScale cs = new ContinuousScale(frameConfLS.get("continuousScale"), "Probability", null);
    cs.addColorStopRGB("0", 0.0, 0.0, 0.0, 0.0, false);
    cs.addColorStopRGB(null, 0.5, 0.5, 0.5, 0.5, false);
    cs.addColorStopRGB("0.5", 0.5, 1, 0.8, 0.8, false);
    cs.addColorStopRGB("1", 1, 0.870588235, 0.188235294, 0.152941176, false);
    cs.finish();

    // Styles for masks
    Colour blueColour = new Colour(0.270588, 0.462745, 0.694117, 1);
    FillStyle maskFillStyle = new FillStyle(blueColour);
    LineStyle maskLineStyle = new LineStyle(blueColour, 0.1);

    /*
     * World world = adminUnitService.getWorld(); PolygonSymbolizer wps = new
     * PolygonSymbolizer((MultiPolygon) world.getGeom(), maskFillStyle,
     * maskLineStyle); worldPolySymBlue.add(wps);
     * df.drawFeatures(worldPolySymBlue);
     */

    RasterLayer ras = FltReader2.openFloatFile(anoFolder, "probability-map", cs);
    df.addRasterLayer(ras);

    // Mask
    MultiPolygon eoMask = (MultiPolygon) ano.getExpertOpinion().getMaskGeom();
    if (eoMask != null) {
      PolygonSymbolizer ps = new PolygonSymbolizer(eoMask, maskFillStyle, maskLineStyle);
      List<PolygonSymbolizer> msk = new ArrayList<PolygonSymbolizer>();
      msk.add(ps);
      df.drawFeatures(msk);
    }

    /*
     * Get envelope as resized by dataframe Get admin units (level 0)
     * overlapping data frame and draw them
     */

    Envelope resizedEnv = df.getEnvelope();
    GeometryFactory gf = new GeometryFactory();
    final Geometry envGeom = gf.toGeometry(resizedEnv);
    Geometry tempSea = envGeom;

    List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);

    FillStyle greyFS = new FillStyle(Palette.GREY_20.get());
    FillStyle whiteFS = new FillStyle(Palette.WHITE.get());
    List<PolygonSymbolizer> adminPolySym = new ArrayList<PolygonSymbolizer>();

    /*
     * Overlay admin units
     */
    for (AdminUnit admin0 : adminUnits) {

      // Intersect. If no overlap, get geomcollection with 0 geometries
      OverlayOp ol = new OverlayOp(envGeom, admin0.getGeom());
      Geometry tempG = ol.getResultGeometry(OverlayOp.INTERSECTION);

      OverlayOp seaDiff = new OverlayOp(tempSea, admin0.getGeom());
      tempSea = seaDiff.getResultGeometry(OverlayOp.DIFFERENCE);

      FillStyle fs = greyFS;
      if (!countryId.equals(admin0.getCountryId())) {

        if (tempG.getNumGeometries() > 0) {
          PolygonSymbolizer ps = new PolygonSymbolizer(GeometryUtil.toMultiPolygon(gf, tempG), fs, defaultLineStyle);
          adminPolySym.add(ps);
        }
      }
    }
    df.drawFeatures(adminPolySym);

    /*
     * Draw sea mask
     */
    for (AdminUnit admin0 : adminUnits) {
      OverlayOp seaDiff = new OverlayOp(tempSea, admin0.getGeom());
      tempSea = seaDiff.getResultGeometry(OverlayOp.DIFFERENCE);
    }

    MultiPolygon mp = GeometryUtil.toMultiPolygon(new GeometryFactory(), tempSea);
    PolygonSymbolizer aps = new PolygonSymbolizer(mp, new FillStyle(Palette.WATER.get()), new LineStyle(Palette.WATER.get(), 0.2));
    df.drawMultiPolygon(aps);

    /*
     * Presence absence points
     */
    int debugCount = 0;
    List<SitePresenceAbsence> x = ano.getPresenceAbsences();
    for (SitePresenceAbsence spa : x) {
      if (spa.isPresent()) {
        Site site = spa.getSite();
        if (site.getCountryId().equals(countryId)) {
          df.drawPoint(site.getX(), site.getY(), spa.isPresent());
        }
        debugCount++;
      }
    }
    System.out.println(ano.getAbbreviation() + " " + debugCount);

    df.finish();

  }

}
