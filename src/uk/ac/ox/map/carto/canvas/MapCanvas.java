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
	
	public MapCanvas(PdfSurface pdf, int width, int height, Envelope env) throws FileNotFoundException {
		Gtk.init(null);
		Pixbuf pb = new Pixbuf("/home/will/map1_public/maps/map_overlay3.png");
		
		this.width = width;
		this.height = height;
		
    	Double dX = env.getWidth();
    	Double dY = env.getHeight();
		
		debugTransform(env);
		
    	double arEnv = dX/dY;
    	double arCanvas = width/height;
    	
		/*
		 * Expand env to fit whole canvas
		 * 
		 */
    	Envelope cEnv = new Envelope();
		if (arCanvas > arEnv) {
			//canvas is fatter- expand env in x direction
			//ll
			cEnv.expandToInclude(new Coordinate(env.getMinX() - (dX/2), env.getMinY()));
			//ur
			cEnv.expandToInclude(new Coordinate(env.getMaxX() + (dX/2), env.getMaxY()));
		} else if (arCanvas < arEnv) {
			//canvas is thinner - expand env in y direction
			//ll
			cEnv.expandToInclude(new Coordinate(env.getMinX(), env.getMinY() - (dY/2)));
			//ur
			cEnv.expandToInclude(new Coordinate(env.getMaxX(), env.getMaxY() + (dY/2)));
		}
		
		/*
		 * Set transform
		 */
    	dX = env.getWidth();
    	dY = env.getHeight();
    	
    	
    	System.out.println("centre:");
    	System.out.println(env.centre());
	
		Double xScale = width / dX;
		Double yScale = height / dY;
		this.scale = (xScale < yScale) ? xScale: yScale;
	    this.env = env;
		transform.scale(this.scale, -this.scale);
		transform.translate(-env.getMinX(), -env.getMinY());
		debugTransform(env);
	    
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
