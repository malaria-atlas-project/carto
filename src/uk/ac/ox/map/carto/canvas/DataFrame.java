package uk.ac.ox.map.carto.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;

import javax.print.attribute.standard.PDLOverrideSupported;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;

public class DataFrame extends BaseCanvas {
	
	private final PdfSurface pdf;
	private final Envelope env;
    private final int width;
    private final int height;
    private double scale;
	private final AffineTransform transform = new AffineTransform();
	
	public DataFrame(PdfSurface pdf, int width, int height, Envelope dataEnv) {
		
		super(pdf);
		
		this.pdf = pdf;
		this.width = width;
		this.height = height;
		this.env = dataEnv;
    	setEnvelope(width, height, dataEnv);
	    
	    cr.setLineWidth(0.2);
	    cr.setSource(0.0, 1.0, 0.0, 1.0);
	}

	private void setEnvelope(int width, int height, Envelope dataEnv) {
		Double dX = dataEnv.getWidth();
    	Double dY = dataEnv.getHeight();
		
    	double arEnv = dX/dY;
    	double arCanvas = width/height;
    	
    	/*
    	 * Fix the scale
    	 */
    	dX = dataEnv.getWidth();
    	dY = dataEnv.getHeight();
		Double xScale = width / dX;
		Double yScale = height / dY;
		this.scale = (xScale < yScale) ? xScale: yScale;
	    
		/*
		 * Expand env to fit whole canvas
		 * 
		 */
    	Envelope canvasEnv = new Envelope();
		if (arCanvas > arEnv) {
			//canvas is fatter than data- expand env in x direction
			//ll
			double dW = (width/scale) - dX;
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMinX() - (dW/2), dataEnv.getMinY()));
			//ur
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMaxX() + (dW/2), dataEnv.getMaxY()));
		} else if (arCanvas < arEnv) {
			double dH = (height/scale) - dY;
			//canvas is thinner than data- expand env in y direction
			//ll
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMinX(), dataEnv.getMinY() - (dH/2)));
			//ur
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMaxX(), dataEnv.getMaxY() + (dH/2)));
		} else {
			//aspect ratios are the same
			canvasEnv = dataEnv;
		}
		
		/*
		 * Set transform
		 */
		transform.scale(this.scale, -this.scale);
		transform.translate(-canvasEnv.getMinX(), -canvasEnv.getMinY()-(height/scale));
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
        	Geometry ls =  p.getBoundary();
        	Coordinate[] coordinates = ls.getCoordinates();
        	
        	for (int i = 0; i < coordinates.length; i++) {
				Coordinate c = coordinates[i];
        		if (i==0){
	        		moveTo(c.x, c.y);
        		} else {
	        		lineTo(c.x, c.y);
        		} 
			}
//	        cr.stroke();
	        cr.fill();
	}
	
	private void lineTo(double x, double y){
		    Point2D.Double pt = new Point2D.Double(x,y);
			transform.transform(pt, pt);
    		cr.lineTo(pt.getX(), pt.getY());
	}
	
	private void moveTo(double x, double y){
		    Point2D.Double pt = new Point2D.Double(x,y);
			transform.transform(pt, pt);
    		cr.moveTo(pt.getX(), pt.getY());
	}

	public PdfSurface getSurface() {
		return pdf;
	}
	

}
