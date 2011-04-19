package uk.ac.ox.map.carto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.AdminUnitRisk;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.adminunit.WaterBody;
import uk.ac.ox.map.base.model.pr.Parasite;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.base.service.CartoService;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.carto.util.GeometryUtil;
import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.h5.H5FloatRaster;
import uk.ac.ox.map.h5.H5RasterFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

public class AMEMaps {
  
  static Logger logger = LoggerFactory.getLogger(AMEMaps.class);
  
	static final CartoService cartoService = new CartoService();

  static final AdminUnitService adminUnitService = new AdminUnitService();
  static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(),
      0.2);

  public static void main(String[] args) throws OutOfMemoryError, Exception {
//    drawAMEMaps(Parasite.Pf);
//    drawAMEMaps(Parasite.Pv);
    
  	H5RasterFactory h5RFpop = new H5RasterFactory("/home/will/map1_public/mbgw2/pop10.h5");
    Country c = adminUnitService.getCountry("LKA");
    drawPopMap(c, h5RFpop);
  }
  
  public static void drawPopMap(Country country, H5RasterFactory h5RF) throws Exception {
  	
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
  	
  	logger.debug("set up colour scale");
  	
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
  	
  	logger.debug("added raster layer");
  	
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

//    toProcess.add("KGZ");
//    toProcess.add("GEO");
//    toProcess.add("AZE");
    
//    toProcess.add("MEX");
//    toProcess.add("TUR");
//    toProcess.add("ARG");
    
//    toProcess.add("CHN");

    // List to append all risk units to
//    List<AdminUnitRisk> riskUnits = adminUnitService.getYunnanRiskUnit(p);
    List<AdminUnitRisk> riskUnits = new ArrayList<AdminUnitRisk>();

    for (Country country : megCountries) {
      if (!toProcess.contains(country.getId())) {
//        continue;
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
    Envelope env = new Envelope();
    for (AdminUnitRisk adminUnitRisk : riskUnits) {
      env.expandToInclude(adminUnitRisk.getGeom().getEnvelopeInternal());
    }
    */

    // drawAMEMap(riskUnits, "GMSR", env, p);

  }

  public static void drawAMEMap(List<AdminUnitRisk> riskUnits, String areaName, Envelope env, Parasite parasite) throws OutOfMemoryError, Exception {

    env.expandBy(env.getWidth() / 20);

//    String dfOutputLoc =  String.format("/home/will/c/Temp/maps/AME_svg/%s_%s.svg", areaName,  parasite.toString());
    String dfOutputLoc = String.format("/home/will/c/Temp/maps/AME/%s_%s.pdf", areaName, parasite.toString());
    /*
     * Create dataframe with specified envelope
     */
    DataFrame df = new DataFrame.Builder(env, new Rectangle(0, 0, 460, 460),
        dfOutputLoc).build();
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
      
      
      //Intersect. If no overlap, get geomcollection with 0 geometries
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
        PolygonSymbolizer ps = new PolygonSymbolizer( (MultiPolygon) adminUnitRisk.getGeom(), fs, defaultLineStyle);
        pfFeats.add(ps);
      }
    }
    df.drawFeatures(pfFeats);
    df.finish();
  }

}
