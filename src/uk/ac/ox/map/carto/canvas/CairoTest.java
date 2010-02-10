package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.Surface;

import uk.ac.ox.map.carto.canvas.style.LineStyle;
import uk.ac.ox.map.carto.server.AdminUnit;
import uk.ac.ox.map.carto.server.AdminUnitService;
import uk.ac.ox.map.carto.server.Country;
import uk.ac.ox.map.carto.util.EnvelopeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
        
public class CairoTest {
	
	public static void main(String[] args) throws IOException {
		
		/*
		 * Get country then use extent to: 
		 * 1. Create map canvas
		 * 2. Get appropriate admin units
		 */
		AdminUnitService adminUnitService = new AdminUnitService();
		
		/*
		 * Get country of interest
		 */
		Country country  = adminUnitService.getCountry("BRA");
		Polygon poly = (Polygon) country.getGeom();
		Envelope env = EnvelopeFactory.envelopeFromPolygon(poly);
		
		
		/*
		 * Get admin units overlapping data frame and draw them
		 */
        ArrayList<AdminUnit> adminUnits = adminUnitService.getAdminUnit(poly);
        LineStyle ls = new LineStyle();
        ls.setLineColour("#9999CC", (float) .2);
        ls.setLineWidth(0.4);
       
        int h, w; w=400; h = 450; 
		PdfSurface dfSurface = new PdfSurface("/tmp/tmp_dataframe.pdf", w, h);
		DataFrame df= new DataFrame(dfSurface, w, h, env);
		df.setLineStyle(ls);
		
        for (AdminUnit admin0 : adminUnits) {
        	df.drawMultiPolygon((MultiPolygon) admin0.getGeom());
		}
		
        /*
         * Draw the rest of the canvas
         */
        w=500; h = 707; 
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.drawDataFrame(df, 10, 10);
		
		/*
		 * Understand this better! Does data frame require finishing? Does the data frame even need a pdf surface?
		 */
        dfSurface.finish();
		mapSurface.finish();
	}

}
