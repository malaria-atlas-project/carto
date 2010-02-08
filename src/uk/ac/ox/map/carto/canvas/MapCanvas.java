package uk.ac.ox.map.carto.canvas;

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
	    setTransform(env);
	    
	    /*
	     * Cairo context is +x-y by default. 
	     * Transformation parameters translate to +x+y coordinate system.
	     * 
	     */
	    
	}
	
	private void setTransform(Envelope env){
    	Matrix m = new Matrix();
    	Double dX = env.getWidth();
    	Double dY = env.getHeight();
    	/*
    	 * Scale is ratio of map env to canvas env 
    	 * Have to preseve aspect ratio
    	 * TODO: hardcoding canvas size!!
    	 */
    	
    	/*
    	 * Must fit
    	 */
    	int canvasWidth = 500;
    	int canvasHeight = 700;
    	
		Double xScale = canvasWidth / dX;
		Double yScale = canvasHeight / dY;
    	
    	double d = -env.getMinX() * xScale;
		double e = -env.getMinY() * xScale;
    	
		m.translate(d, e);
    	m.scale(xScale, xScale);
		
        cr.transform(m);
        
    	System.out.println(xScale);
    	System.out.println(yScale);
    	System.out.println(d);
    	System.out.println(e);
		
        /*
        assert (xScale * env.getMaxX()) <= canvasWidth;
        assert (yScale * env.getMaxY()) <= canvasHeight;
    	System.out.println(dX);
    	System.out.println(dY);
    	System.out.println(xScale);
    	System.out.println(yScale);
    	System.out.println(xScale* env.getMinX());
    	System.out.println(yScale* env.getMinY());
    	System.out.println(xScale* env.getMaxX());
    	System.out.println(yScale* env.getMaxY());
    	*/
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
        	
    		/*
	        cr.moveTo(0,0);
    		cr.lineTo(-40,-40);
    		*/
        	
        	for (int i = 0; i < coordinates.length; i++) {
				Coordinate c = coordinates[i];
        		if (i==0){
			        //cr.moveTo(c.x, c.y);
	        		cr.lineTo(c.x, c.y);
        		}
        		cr.lineTo(c.x, c.y);
//	        		System.out.println("X: " + c.x);
			}
	        cr.stroke();
	}

}
