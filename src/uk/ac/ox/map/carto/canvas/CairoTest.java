package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.server.AdminUnit;
import uk.ac.ox.map.carto.server.AdminUnitService;
import uk.ac.ox.map.carto.server.Country;
import uk.ac.ox.map.carto.server.PfAdminUnit;
import uk.ac.ox.map.carto.server.PfCountry;
import uk.ac.ox.map.carto.server.PolygonCursor;
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
//		String countryId = "LKA";
		AdminUnitService adminUnitService = new AdminUnitService();
		
		/*
		drawMap(adminUnitService, countryId);
		*/
		ArrayList<PfCountry> pfCountries = adminUnitService.getCountries();
		for (PfCountry pfCountry : pfCountries) {
//			if (pfCountry.getId().compareTo(countryId) <=0)
//				continue;
	        System.out.println("Processing:" + pfCountry.getId());
			drawMap(adminUnitService, pfCountry.getId());
		}
	}
		
	public static void drawMap(AdminUnitService adminUnitService, String countryId) throws IOException, InterruptedException {
		
		/*
		 * Get country then use extent to: 
		 * 1. Create map canvas
		 * 2. Get appropriate admin units
		 */
		
		/*
		 * Get country of interest
		 */
		PfCountry country  = adminUnitService.getCountry(countryId);
		Polygon poly = (Polygon) country.getGeom();
		Envelope env = EnvelopeFactory.envelopeFromPolygon(poly, 1.05);
		
		/*
		 * Get admin units overlapping data frame and draw them
		 */
        ArrayList<AdminUnit> adminUnits = adminUnitService.getAdminUnit(poly);
       
        int h, w; w=460; h = 460; 
		PdfSurface dfSurface = new PdfSurface("/tmp/tmp_dataframe.pdf", w, h);
		DataFrame df= new DataFrame(dfSurface, w, h, env);
		df.setBackgroundColour("#bee8ff", 1);
        df.setLineColour("#000000", (float) 0.7);
        df.setLineWidth(0.1);
        df.setFillColour("#cccccc", (float) 0.8);
        
        for (AdminUnit admin0 : adminUnits) {
        	df.drawMultiPolygon((MultiPolygon) admin0.getGeom());
		}
        
        /*
         * Draw the risk units
         */
        ArrayList<PfAdminUnit> pfUnits = adminUnitService.getPfAdminUnits(countryId);
        HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
        colours.put(0, new Colour("#ffffff", 1));
        colours.put(1, new Colour("#ffbebe", 1));
        colours.put(2, new Colour("#cd6666", 1));
		PolygonCursor<PfAdminUnit> pfFeats = new PolygonCursor<PfAdminUnit>(pfUnits, colours);
		df.drawPolygonCursor(pfFeats);
        
        
        /*
         * Draw the rest of the canvas
         */
        w=500; h = 707; 
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.drawDataFrame(df, 20, 40);
        dfSurface.finish();
		
		Frame frame = new Frame(20,530,430,20);
		mapCanvas.setScaleBar(frame, df.getScale(), 7);
		mapCanvas.drawMapBorders();
		mapCanvas.setColour("#0066CC", 1);
		mapCanvas.drawMapGrids(4.5);
		
		/*
		 * Text stuff
		 */
		MapTextResource mtr = new MapTextResource();
		Frame titleFrame = new Frame(0, 5, 500, 0);
		mapCanvas.setTextFrame(
			String.format((String) mtr.getObject("pfTitle"), country.getName()),
			titleFrame,
			11);
		
		List<String> mapTextItems = new ArrayList<String>();
		
		String yearsText = StringUtil.getReadableList(country.getYears());
		mapTextItems.add(String.format(
				(String) mtr.getObject("apiText"), 
				country.getCount(),
				country.getAdminLevel(),
				yearsText
		));
		
		if (country.getZeroed().size() > 0) {
			String zeroedText = StringUtil.getReadableList(country.getZeroed());
			mapTextItems.add(String.format((String) mtr.getObject("ithgText"), zeroedText));
		}
		
		Frame mapTextFrame = new Frame(70, 550, 300, 0);
		mapCanvas.setTextFrame(mapTextItems, mapTextFrame, 8);
		
		/*
		 * Finally put on the logo
		Pixbuf pb = new Pixbuf("/home/will/map1_public/maps/map_overlay3.png");
		Frame logoFrame = new Frame(300, 400, 250, 0);
		mapCanvas.setLogo(pb, logoFrame);
		
		 */
		/*
		 * Understand this better! Does data frame require finishing? Does the data frame even need a pdf surface?
		 */
		mapSurface.finish();
		SystemUtil.addBranding(countryId+"_pf");
		SystemUtil.convertToPng(countryId+"_pf");

	}

}
