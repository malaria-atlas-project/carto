package uk.ac.ox.map.carto.canvas;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

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
    private final int width;
    private final int height;
	private final AffineTransform transform = new AffineTransform();
	
	
	public MapCanvas(PdfSurface pdf, int width, int height, Envelope env) {
		this.width = width;
		this.height = height;
		
		
	    cr = new Context(pdf);
	    //cr.scale(1, -1);
	    cr.setSource(1.0, 0.1, 0.0, 1.0);
	    cr.setLineWidth(0.2);
	    
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
    	
		Double xScale = width / dX;
		Double yScale = height / dY;
    	
    	double d = -env.getMinX() * xScale;
		double e = -env.getMinY() * yScale;
    	
		
		transform.scale(xScale, xScale);
		transform.translate(-env.getMinX(), -env.getMinY());
        
        /*
    	System.out.println(xScale);
    	System.out.println(yScale);
		
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
        	
        	System.out.println(p);
        	for (int i = 0; i < coordinates.length; i++) {
				Coordinate c = coordinates[i];
        		if (i==0){
	        		moveTo(c.x, c.y);
        		} else {
	        		lineTo(c.x, c.y);
        		} 
        		
			}
	        cr.stroke();
	}
	private void lineTo(double x, double y){
		    Point2D.Double pt = new Point2D.Double(x,y);
		    pt.setLocation(x, y);
			transform.transform(pt, pt);
			System.out.println(pt);
    		cr.lineTo(pt.getX(), pt.getY());
	}
	private void moveTo(double x, double y){
    		cr.moveTo(x, y);
	}

}
