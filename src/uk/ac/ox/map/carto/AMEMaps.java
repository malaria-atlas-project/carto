package uk.ac.ox.map.carto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

import uk.ac.ox.map.base.model.adminunit.AdminUnit;
import uk.ac.ox.map.base.model.adminunit.AdminUnitRisk;
import uk.ac.ox.map.base.model.adminunit.Country;
import uk.ac.ox.map.base.model.pr.Parasite;
import uk.ac.ox.map.base.service.AdminUnitService;
import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.FillStyle.FillType;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;
import uk.ac.ox.map.deps.Colour;

public class AMEMaps {
  
	static final AdminUnitService adminUnitService = new AdminUnitService();
	static final LineStyle defaultLineStyle = new LineStyle(Palette.BLACK.get(), 0.2);
	
  public static void main(String[] args) throws OutOfMemoryError, Exception {
    drawAMEMaps(Parasite.Pf);
    drawAMEMaps(Parasite.Pv);
  }
  
  public static void drawAMEMaps(Parasite p) throws OutOfMemoryError, Exception {
		List<String> toProcess = new ArrayList<String>();
//		toProcess.add("LKA");
		toProcess.add("THA");
		toProcess.add("VNM");
		toProcess.add("MMR");
		toProcess.add("KHM");
		toProcess.add("LAO");
//		toProcess.add("CHN");
		
		//List to append all risk units to
    List<AdminUnitRisk> riskUnits = adminUnitService.getYunnanRiskUnit(p);
		
		for (String string : toProcess) {
    	Country country = adminUnitService.getCountry(string);
    	List<AdminUnitRisk> countryRiskUnits = adminUnitService.getRiskAdminUnits(country, p);
    	riskUnits.addAll(countryRiskUnits);
    	Envelope env;
    	if (country.getId().equals("CHN")) {
         env = new Envelope(97.5291, 106.191, 21.143, 29.227);
    	} else {
    	   env = new Envelope(country.getMinX(), country.getMaxX(), country.getMinY(), country.getMaxY());
    	}
    	drawAMEMap(countryRiskUnits, country.getId(), env, p);
    }
		Envelope env = new Envelope();
		for (AdminUnitRisk adminUnitRisk : riskUnits) {
		  env.expandToInclude(adminUnitRisk.getGeom().getEnvelopeInternal());
    }
		
    drawAMEMap(riskUnits, "GMSR", env, p);
		
  }

  
  public static void drawAMEMap(List<AdminUnitRisk> riskUnits, String areaName, Envelope env, Parasite parasite) throws OutOfMemoryError, Exception {
    
    env.expandBy(env.getWidth()/20);
  
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
  
  	List<PolygonSymbolizer> pfFeats = new ArrayList<PolygonSymbolizer>();
  
  	for (AdminUnitRisk adminUnitRisk : riskUnits) {
  		FillStyle fs = new FillStyle(colours.get(adminUnitRisk.getRisk()), FillType.SOLID);
  		PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) adminUnitRisk.getGeom(), fs, defaultLineStyle);
  		pfFeats.add(ps);
  	}
  	df.drawFeatures(pfFeats);
  	df.finish();
  }

}
