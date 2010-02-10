package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;

import org.freedesktop.cairo.PdfSurface;

import uk.ac.ox.map.carto.server.Admin0;
import uk.ac.ox.map.carto.server.Admin0Service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
        
public class CairoTest {
	
	public static void main(String[] args) throws IOException {
		
		Admin0Service a0 = new Admin0Service();
        ArrayList<Admin0> a = a0.getAdminUnit();
        
		Envelope env = new Envelope();
        for (Admin0 admin0 : a) {
        	System.out.println(admin0.getName());
        	/*
        	 * Why doesn't this return an envelope? This envelope creation shouldn't be necessary
        	 */
        	Polygon boundingGeom = (Polygon) admin0.getGeometry().getEnvelope();
        	for (Coordinate c: boundingGeom.getCoordinates()) {
        		env.expandToInclude(c);
			}
        }
        
		PdfSurface pdf = new PdfSurface("/tmp/javacairo.pdf", 700, 500);
		MapCanvas mc= new MapCanvas(pdf, 700, 500, env);
		
		//System.out.println(env.getMinX());
		//System.out.println(env.getMinY());
		//System.out.println(env.getMaxX());
		//System.out.println(env.getMaxX());
        
        for (Admin0 admin0 : a) {
        	mc.drawMultiPolygon((MultiPolygon) admin0.getGeometry());
		}
        
        pdf.finish();
	}

}
