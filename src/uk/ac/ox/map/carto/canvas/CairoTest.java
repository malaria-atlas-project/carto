package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.Surface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.canvas.style.FillStyle;
import uk.ac.ox.map.carto.canvas.style.LineStyle;
import uk.ac.ox.map.carto.server.AdminUnit;
import uk.ac.ox.map.carto.server.AdminUnitService;
import uk.ac.ox.map.carto.server.Country;
import uk.ac.ox.map.carto.server.PfAdminUnit;
import uk.ac.ox.map.carto.server.PolygonCursor;
import uk.ac.ox.map.carto.util.EnvelopeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
        
public class CairoTest {
	
	public static void main(String[] args) throws IOException {
		String countryId = "PER";
		drawMap(countryId);
		
	}
	public static void drawMap(String countryId) throws IOException {
		
		/*
		 * Get country then use extent to: 
		 * 1. Create map canvas
		 * 2. Get appropriate admin units
		 */
		AdminUnitService adminUnitService = new AdminUnitService();
		
		/*
		 * Get country of interest
		 */
		Country country  = adminUnitService.getCountry(countryId);
		Polygon poly = (Polygon) country.getGeom();
		Envelope env = EnvelopeFactory.envelopeFromPolygon(poly, 1.05);
		
		/*
		 * Get admin units overlapping data frame and draw them
		 */
        ArrayList<AdminUnit> adminUnits = adminUnitService.getAdminUnit(poly);
       
        int h, w; w=460; h = 450; 
		PdfSurface dfSurface = new PdfSurface("/tmp/tmp_dataframe.pdf", w, h);
		DataFrame df= new DataFrame(dfSurface, w, h, env);
        df.setLineColour("#000000", (float) 1);
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
//		PolygonCursor<PfAdminUnit> pfFeats = new PolygonCursor<PfAdminUnit>(pfUnits, colours);
//		df.drawPolygonCursor(pfFeats);
        
        /*
         * Draw the rest of the canvas
         */
        w=500; h = 707; 
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.drawDataFrame(df, 20, 40);
        dfSurface.finish();
		Gtk.init(null);
		Pixbuf pb = new Pixbuf("/home/will/map1_public/maps/map_overlay3.png");
		mapCanvas.setLogo(pb, 20, 20);
		
		Frame frame = new Frame(70,530,400,20);
		mapCanvas.setScaleBar(frame, df.getScale());
		
		/*
		 * Understand this better! Does data frame require finishing? Does the data frame even need a pdf surface?
		 */
		mapSurface.finish();
	}

}
