package uk.ac.ox.map.carto.canvas;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;

import com.sun.java.swing.plaf.gtk.resources.gtk;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.Matrix;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;

public class MapCanvas {
	
	private final Context cr;
	private final Envelope env;
    private final int width;
    private final int height;
    private final double scale;
	private final AffineTransform transform = new AffineTransform();
	
	public MapCanvas(PdfSurface pdf, int width, int height, Envelope dEnv) throws FileNotFoundException {
		Gtk.init(null);
		Pixbuf pb = new Pixbuf("/home/will/map1_public/maps/map_overlay3.png");
		
		this.width = width;
		this.height = height;
		
    	Double dX = dEnv.getWidth();
    	Double dY = dEnv.getHeight();
		
		debugTransform(dEnv);
		
    	double arEnv = dX/dY;
    	double arCanvas = width/height;
    	
    	/*
    	 * Fix the scale
    	 */
    	dX = dEnv.getWidth();
    	dY = dEnv.getHeight();
		Double xScale = width / dX;
		Double yScale = height / dY;
		this.scale = (xScale < yScale) ? xScale: yScale;
	    this.env = dEnv;
	    
		/*
		 * Expand env to fit whole canvas
		 * 
		 */
    	Envelope cEnv = new Envelope();
		if (arCanvas > arEnv) {
			//canvas is fatter than data- expand env in x direction
			//ll
			double dW = (width/scale) - dX;
			cEnv.expandToInclude(new Coordinate(dEnv.getMinX() - (dW/2), dEnv.getMinY()));
			//ur
			cEnv.expandToInclude(new Coordinate(dEnv.getMaxX() + (dW/2), dEnv.getMaxY()));
		} else if (arCanvas < arEnv) {
			double dH = (height/scale) - dY;
			//canvas is thinner than data- expand env in y direction
			//ll
			cEnv.expandToInclude(new Coordinate(dEnv.getMinX(), dEnv.getMinY() - (dH/2)));
			//ur
			cEnv.expandToInclude(new Coordinate(dEnv.getMaxX(), dEnv.getMaxY() + (dH/2)));
		} else {
			//aspect ratios are the same
			cEnv = dEnv;
		}
		
		/*
		 * Set transform
		 */
		transform.scale(this.scale, -this.scale);
		transform.translate(-cEnv.getMinX(), -cEnv.getMinY());
		debugTransform(cEnv);
	    
	   /*
	    * Get context 
	    */
	    cr = new Context(pdf);
	    cr.setLineWidth(0.2);
	    cr.scale(0.2, 0.2);
	    //cr.setSource(pb, 10, 10);
	    //cr.paint();
	    cr.scale(5,5);
	    cr.setSource(1.0, 0.1, 0.0, 1.0);
	}
	
	private void debugTransform(Envelope env){
		System.out.println("X Scale: "+transform.getTranslateX());
		System.out.println("Y Scale: "+transform.getTranslateY());
	    Point2D.Double llPt = new Point2D.Double(env.getMinX(), env.getMinY());
	    Point2D.Double urPt = new Point2D.Double(env.getMaxX(), env.getMaxY());
	    
    	System.out.println("before:");
    	System.out.println(llPt);
    	System.out.println(urPt);
		transform.transform(llPt, llPt);
		transform.transform(urPt, urPt);
    	System.out.println("after:");
    	System.out.println(llPt);
    	System.out.println(urPt);
	}
	
    
	public void drawMultiPolygon(MultiPolygon mp){
        	for (int i = 0; i < mp.getNumGeometries(); i++) {
        		drawPolygon((Polygon) mp.getGeometryN(i));
			}
	}
	
	public void drawPolygon(Polygon p){
        	LineString ls = (LineString) p.getBoundary();
        	Coordinate[] coordinates = ls.getCoordinates();
        	
//        	System.out.println(p);
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
			transform.transform(pt, pt);
    		cr.lineTo(pt.getX(), pt.getY() + height);
	}
	
	private void moveTo(double x, double y){
		    Point2D.Double pt = new Point2D.Double(x,y);
			transform.transform(pt, pt);
    		cr.moveTo(pt.getX(), pt.getY() + height);
	}

}
