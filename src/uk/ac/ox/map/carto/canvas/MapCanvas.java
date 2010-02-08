package uk.ac.ox.map.carto.canvas;
import java.io.IOException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.Matrix;
import org.freedesktop.cairo.PdfSurface;

public class MapCanvas {
	
	private final Context cr;
	private final Envelope env;
	
	public MapCanvas(PdfSurface pdf, Envelope env) {
	    cr = new Context(pdf);
	    cr.setSource(1.0, 0.1, 0.0, 1.0);
	    cr.setLineWidth(0.1);
        
	    this.env = env;
	    
	    /*
	     * Cairo context is +x-y by default. 
	     * Transformation parameters translate to +x+y coordinate system.
	     * 
	     */
	    
    	Matrix m = new Matrix();
    	m.scale(1, -1);
    	m.translate(0, -180);
        cr.transform(m);
	}
    
	public void drawMultiPolygon(MultiPolygon mp){
        	Polygon p = (Polygon) mp.getGeometryN(0);
        	for (int i = 1; i == mp.getNumGeometries(); i++) {
        		drawPolygon(p);
			}
	}
	
	public void drawPolygon(Polygon p){
        	LineString ls = (LineString) p.getBoundary();
        	Coordinate[] coordinates = ls.getCoordinates();
        	
        	for (int i = 0; i < coordinates.length; i++) {
				Coordinate c = coordinates[i];
        		if (i==0){
			        cr.moveTo(c.x, c.y);
        		}
        		cr.lineTo(c.x, c.y);
			}
	        cr.stroke();
	}

}
