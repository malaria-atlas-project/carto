package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.server.AdminUnit;
import uk.ac.ox.map.carto.server.AdminUnitRisk;
import uk.ac.ox.map.carto.server.AdminUnitService;
import uk.ac.ox.map.carto.server.Country;
import uk.ac.ox.map.carto.server.Exclusion;
import uk.ac.ox.map.carto.server.FeatureLayer;
import uk.ac.ox.map.carto.server.WaterBody;
import uk.ac.ox.map.carto.text.MapTextResource;
import uk.ac.ox.map.carto.util.EnvelopeFactory;
import uk.ac.ox.map.carto.util.StringUtil;
import uk.ac.ox.map.carto.util.SystemUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
        
public class CairoTest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Gtk.init(null);
		AdminUnitService adminUnitService = new AdminUnitService();
		
		String countryId = "VNM";
		Country country  = adminUnitService.getCountry(countryId);
//		drawMap(adminUnitService, country, "pv");
		drawMap(adminUnitService, country, "pf");
		
//		drawMap(adminUnitService, "pf");
//		drawMap(adminUnitService, "pv");
	}

	private static void 
	drawMap(AdminUnitService adminUnitService, String parasite) 
	throws IOException, InterruptedException {
		List<Country> pfCountries = adminUnitService.getCountries(parasite);
		for (Country country : pfCountries) {
	        System.out.println("Processing:" + country.getId());
	        System.out.println(country.getName());
	        if (country.getId().compareTo("CHN")==0)
	        	continue;
			drawMap(adminUnitService, country, parasite);
		}
	}
		
	public static void drawMap(AdminUnitService adminUnitService, Country country, String parasite) throws IOException, InterruptedException {
		
		/*
		 * Get country then use extent to: 
		 * 1. Create map canvas
		 * 2. Get appropriate admin units
		 */
		
		Polygon poly = (Polygon) country.getEnvelope();
		Envelope env = EnvelopeFactory.envelopeFromPolygon(poly, 1.05);
		
		/*
		 * Create dataframe with specified envelope
		 */
        int h, w; w=460; h = 460; 
		PdfSurface dfSurface = new PdfSurface("/tmp/tmp_dataframe.pdf", w, h);
		DataFrame df= new DataFrame(dfSurface, w, h, env);
		String waterHexColour = "#bee8ff";
		df.setBackgroundColour(waterHexColour, 1);
        df.setLineColour("#000000", (float) 0.7);
        df.setLineWidth(0.1);
        df.setFillColour("#cccccc", (float) 0.8);
        
        
		/*
		 * Get envelope as resized by dataframe
		 * Get admin units (level 0) overlapping data frame and draw them
		 */
        Envelope resizedEnv = df.getEnvelope();
        List<AdminUnit> adminUnits = adminUnitService.getAdminUnits(resizedEnv);
        for (AdminUnit admin0 : adminUnits) {
        	df.drawMultiPolygon((MultiPolygon) admin0.getGeom());
        	System.out.println(admin0.getName());
		}
        
        /*
         * Draw the risk units
         */
        HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
        colours.put(0, new Colour("#ffffff", 1));
        colours.put(1, new Colour("#ffbebe", 1));
        colours.put(2, new Colour("#cd6666", 1));
        colours.put(9, new Colour("#ffff00", 1));
        
        /*
         * TODO: possible to obtain a nice feature class, colours included, direct from the hibernate service?
         */
        
        /*
         * Get risk admin units
         * Check if we have no data
         */
    	boolean noDataPresent = false;
		FeatureLayer<MultiPolygon> pfFeats = new FeatureLayer<MultiPolygon>();
        List<AdminUnitRisk> riskUnits = adminUnitService.getRiskAdminUnits(country, parasite);
        for (AdminUnitRisk adminUnitRisk : riskUnits) {
        	pfFeats.addFeature((MultiPolygon) adminUnitRisk.getGeom(), colours.get(adminUnitRisk.getRisk()));
			if(adminUnitRisk.getRisk()==9)
        		noDataPresent = true;
		}
        df.drawFeatures(pfFeats);

        /*
         * Get excluded cities
         * TODO: get enum type for exclusions- mapped in Hibernate? This caused strange problems before.
         */
		FeatureLayer<MultiPolygon> excl = new FeatureLayer<MultiPolygon>();
		FeatureLayer<MultiPolygon> excl2 = new FeatureLayer<MultiPolygon>();
        List<Exclusion> exclusions = adminUnitService.getExclusions(country);
        Colour exclColour = new Colour("#ffffff", 1);
        List<String> exclCities = new ArrayList<String>();
        List<String> exclIslands = new ArrayList<String>();
        List<String> exclAdminUnits = new ArrayList<String>();
        List<String> exclMoh = new ArrayList<String>();
        for (Exclusion exclusion : exclusions) {
        	if (exclusion.getExclusionType().compareTo("urban area")==0)
	        	exclCities.add(exclusion.getName());
        	else if (exclusion.getExclusionType().compareTo("island")==0)
	        	exclIslands.add(exclusion.getName());
        	else if (exclusion.getExclusionType().compareTo("ithg exclusion")==0)
	        	exclAdminUnits.add(exclusion.getName());
        	else if (exclusion.getExclusionType().compareTo("moh exclusion")==0)
	        	exclMoh.add(exclusion.getName());
        	//MOH hack... damn exclusions
        	if (exclusion.getExclusionType().compareTo("moh exclusion")==0)
	        	excl2.addFeature((MultiPolygon) exclusion.getGeom(), exclColour);
        	else
	        	excl.addFeature((MultiPolygon) exclusion.getGeom(), exclColour);
		}
        /*
         * TODO: fix draw features code duplication
         */
        df.drawFeatures2(excl, "hatched");
        df.drawFeatures2(excl2, "stipple");
        
        /*
         * Draw on water bodies
         */
		FeatureLayer<MultiPolygon> waterBodies = new FeatureLayer<MultiPolygon>();
        List<WaterBody> water = adminUnitService.getWaterBodies(resizedEnv);
        Colour waterColour = new Colour(waterHexColour, 1);
        for (WaterBody waterBody : water) {
        	waterBodies.addFeature((MultiPolygon) waterBody.getGeom(), waterColour);
		}
        df.drawFeatures(waterBodies);
        
        /*
         * Draw the rest of the canvas
         */
        w=500; h = 707; 
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.drawDataFrame(df, 20, 40);
        dfSurface.finish();
		
		Rectangle frame = new Rectangle(20,530,430,20);
		mapCanvas.setScaleBar(frame, df.getScale(), 7);
		mapCanvas.drawMapBorders();
		mapCanvas.setColour("#0066CC", 1);
		mapCanvas.drawMapGrids(4.5);
		
		/*
		 * Draw the legend
		 * TODO: combine this and the styling in a layer
		 */
		List<LegendItem> legend = new ArrayList<LegendItem>();
		legend.add(new LegendItem("Water", waterColour));
		legend.add(new LegendItem("Malaria free", colours.get(0)));
		
		//TODO - hacks!
		if (parasite.compareTo("pf")==0)
			legend.add(new LegendItem("<i>Pf</i>API &lt; 0.1‰", colours.get(1)));
		else
			legend.add(new LegendItem("<i>Pv</i>API &lt; 0.1‰", colours.get(1)));
			
		if (parasite.compareTo("pf")==0)
			legend.add(new LegendItem("<i>Pf</i>API ≥ 0.1‰", colours.get(2)));
		else
			legend.add(new LegendItem("<i>Pv</i>API ≥ 0.1‰", colours.get(2)));
	
		LegendItem ithgLi = new LegendItem("ITHG exclusions", exclColour);
		LegendItem mohLi = new LegendItem("MoH exclusions", exclColour);
		
		//TODO: hatch hack
		ithgLi.hatched = true;
		mohLi.stippled = true;
		if (excl.size() > 0 )
			legend.add(ithgLi);
		if (excl2.size() > 0 )
			legend.add(mohLi);
		if (noDataPresent)
			legend.add(new LegendItem("No data", colours.get(9)));
		
		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawLegend(legendFrame, legend);
		
		
		/*
		 * Text stuff
		 * TODO: factor all this string building out
		 */
		MapTextResource mtr = new MapTextResource();
		Rectangle titleFrame = new Rectangle(0, 5, 500, 0);
		
		String mapTitle = null;
		if (parasite.compareTo("pf")==0)
			mapTitle = String.format((String) mtr.getObject("pfTitle"), country.getName());
		else if (parasite.compareTo("pv")==0)
			mapTitle = String.format((String) mtr.getObject("pvTitle"), country.getName());
		
		mapCanvas.setTitle(mapTitle, titleFrame, 11);
		
		/*
		 * Main map text
		 */
	    
	    /*
	     * TODO: hack - overrides for VEN and CHN
	     */
		List<String> mapTextItems = new ArrayList<String>();
		
		List<Integer> years = adminUnitService.getYears(country, parasite);
		List<String> apiAdminLevels = adminUnitService.getAdminLevels(country, parasite);
		
	    List<String> intelCountries = Arrays.asList(new String[] {"BWA",  "MRT", "SWZ", "DJI"});
	    Boolean isAdmin0Inferred = false;
	    if (apiAdminLevels.size() > 0) {
	    	String adminLevel = apiAdminLevels.get(0);
	    }
	    if (intelCountries.contains(country.getId())) {
			mapTextItems.add((String) mtr.getObject("apiTextNationalMedIntel"));
	    } else if (country.getId().compareTo("VEN")==0) {
	    	mapTextItems.add(
			    String.format("The health management information system data used to inform the stable, unstable and malaria free categories were available for 215 administrative units at Admin3 level and 29 at Admin1 level for the following years: %s.", StringUtil.getReadableList(years))
		    );
	    } else if (country.getId().compareTo("CHN")==0) {
	    	mapTextItems.add(
			    String.format("The health management information system data used to inform the stable, unstable and malaria free categories were available for 6 administrative units at Admin2 level and 21 at Admin1 level for the following years: %s.", StringUtil.getReadableList(years))
		    );
	    } else if (apiAdminLevels.get(0).compareTo("0")==0 && apiAdminLevels.size()<2) {
			mapTextItems.add((String) mtr.getObject("apiTextNoInfo"));
		} else {
			mapTextItems.add(StringUtil.formatAdminString((String) mtr.getObject("apiText"), apiAdminLevels, years, riskUnits.size()));
		}
	    
		
		/*
		 * Medical intelligence
		 */
		List<String> zeroed = adminUnitService.getZeroed(country, parasite);
		if (zeroed.size() > 0) {
			String medintelText = (String) mtr.getObject("medintelText");
			mapTextItems.add(String.format(medintelText, StringUtil.getReadableList(zeroed)));
		}
		
		/*
		 * Exclusions
		 */
		String formatString = "the following %s: %s";
		if (exclAdminUnits.size() > 0 || exclCities.size() > 0 || exclIslands.size() > 0) {
			String ithgText = (String) mtr.getObject("ithgText");
			List<String> txt = new ArrayList<String>();
			if (exclAdminUnits.size() > 0) {
				txt.add(
					StringUtil.formatPlaceName(formatString, "administrative unit", "administrative units", exclAdminUnits)
				);
			}
			if (exclCities.size() > 0) {
				txt.add(
					StringUtil.formatPlaceName(formatString, "city", "cities", exclCities)
				);
			}
			if (exclIslands.size() > 0) {
				txt.add(
					StringUtil.formatPlaceName(formatString, "island", "islands", exclIslands)
				);
			}
			mapTextItems.add(String.format(ithgText, StringUtil.getReadableList(txt)));
		}
		
		if (exclMoh.size() > 0) {
			String mohText = (String) mtr.getObject("mohText");
			mapTextItems.add(
				String.format(
					mohText, 
					StringUtil.formatPlaceName(formatString, "administrative unit", "administrative units", exclMoh)
				)
			);
		}
		
		mapTextItems.add((String) mtr.getObject("copyright"));
		Rectangle mapTextFrame = new Rectangle(50, 555, 320, 0);
		mapCanvas.drawTextFrame(mapTextItems, mapTextFrame, 6);
		
		/*
		 * Finally put on the logo
		
		Dataset x = gdal.Open("/home/will/map1_public/maps/map_overlay3.png");
		Band band = x.GetRasterBand(1);

		int xsize = x.getRasterXSize();
		int ysize = x.getRasterYSize();

	   ByteBuffer y = band.ReadRaster_Direct(0, 0, xsize, ysize, gdalconst.GDT_Byte);
	   byte[] bytes = new byte[y.remaining()];
	   y.get(bytes);
	   
		Pixbuf pb = new Pixbuf(bytes);
		Rectangle logoFrame = new Rectangle(300, 400, 250, 0);
		mapCanvas.setLogo(pb, logoFrame);
		gdal.AllRegister();
		
		 */
		
		/*
		 * Understand this better! Does data frame require finishing? Does the data frame even need a pdf surface?
		 */
		mapSurface.finish();
		SystemUtil.addBranding(country.getId(), parasite);
//		SystemUtil.convertToPng(country.getId()+"_pf");

	}

}
