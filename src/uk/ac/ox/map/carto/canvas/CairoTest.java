package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.server.AdminUnit;
import uk.ac.ox.map.carto.server.AdminUnitRisk;
import uk.ac.ox.map.carto.server.AdminUnitService;
import uk.ac.ox.map.carto.server.Country;
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
		AdminUnitService adminUnitService = new AdminUnitService();
		
		String countryId = "VEN";
		Country country  = adminUnitService.getCountry(countryId);
		drawMap(adminUnitService, country, "pv");
		
		/*
		String parasite = "pv";
		ArrayList<Country> pfCountries = adminUnitService.getCountries(parasite);
		for (Country country : pfCountries) {
	        System.out.println("Processing:" + country.getId());
			drawMap(adminUnitService, country, parasite);
		}
		*/
	}
		
	public static void drawMap(AdminUnitService adminUnitService, Country country, String parasite) throws IOException, InterruptedException {
		
		/*
		 * Get country then use extent to: 
		 * 1. Create map canvas
		 * 2. Get appropriate admin units
		 */
		
		/*
		 * Get country envelope
		 * Get admin units (level 0) overlapping data frame and draw them
		 */
		Polygon poly = (Polygon) country.getGeom();
		Envelope env = EnvelopeFactory.envelopeFromPolygon(poly, 1.05);
		
        ArrayList<AdminUnit> adminUnits = adminUnitService.getAdminUnits(poly);
       
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
        ArrayList<AdminUnitRisk> pfUnits = adminUnitService.getRiskAdminUnits(country, parasite);
        HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
        colours.put(0, new Colour("#ffffff", 1));
        colours.put(1, new Colour("#ffbebe", 1));
        colours.put(2, new Colour("#cd6666", 1));
        colours.put(9, new Colour("#ffff00", 1));
		PolygonCursor<AdminUnitRisk> pfFeats = new PolygonCursor<AdminUnitRisk>(pfUnits, colours);
		df.drawPolygonCursor(pfFeats);

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
		legend.add(new LegendItem("Malaria free", colours.get(0)));
		legend.add(new LegendItem("<i>Pf</i>API &lt; 0.1‰", colours.get(1)));
		legend.add(new LegendItem("<i>Pf</i>API &gt;= 0.1‰", colours.get(2)));
		legend.add(new LegendItem("No data", colours.get(9)));
		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawLegend(legendFrame, legend);
		
		
		/*
		 * Text stuff
		 * TODO: factor all this string building out?
		 */
		MapTextResource mtr = new MapTextResource();
		Rectangle titleFrame = new Rectangle(0, 5, 500, 0);
		
		String mapTitle = null;
		if (parasite.compareTo("pf")==0)
			mapTitle = String.format((String) mtr.getObject("pfTitle"), country.getName());
		else if (parasite.compareTo("pv")==0)
			mapTitle = String.format((String) mtr.getObject("pvTitle"), country.getName());
		
		mapCanvas.setTextFrame(
			mapTitle,
			titleFrame,
			11);
		
		List<String> mapTextItems = new ArrayList<String>();
		
		List<Integer> years = adminUnitService.getYears(country, parasite);
		System.out.println(years);
		String yearsText = StringUtil.getReadableList(years);
		mapTextItems.add(String.format(
				(String) mtr.getObject("apiText"), 
				adminUnitService.getAdminUnitCount(country, parasite),
				adminUnitService.getAdminLevel(country, parasite),
				yearsText 
		));
		
		List<String> zeroed = adminUnitService.getZeroed(country, parasite);
		
		if (zeroed.size() > 0) {
			String zeroedText = StringUtil.getReadableList(zeroed);
			mapTextItems.add(String.format((String) mtr.getObject("ithgText"), zeroedText));
		}
		
		Rectangle mapTextFrame = new Rectangle(70, 555, 300, 0);
		mapCanvas.drawTextFrame(mapTextItems, mapTextFrame, 6);
		
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
		SystemUtil.addBranding(country.getId(), parasite);
//		SystemUtil.convertToPng(country.getId()+"_pf");

	}

}
