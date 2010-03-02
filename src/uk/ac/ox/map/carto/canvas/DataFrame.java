package uk.ac.ox.map.carto.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.freedesktop.cairo.PdfSurface;

import uk.ac.ox.map.carto.feature.Feature;
import uk.ac.ox.map.carto.server.FeatureLayer;

import com.google.gwt.junit.client.WithProperties;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/*
 * Adds polygon drawing facilities to BaseCanvas.
 * TODO: add raster functionality 
 *  
 */
public class DataFrame extends BaseCanvas {
	
	private Envelope env;
    private double scale;
	private final AffineTransform transform = new AffineTransform();
	
	public DataFrame(PdfSurface pdf, int width, int height, Envelope dataEnv) {
		
		super(pdf, width, height);
		
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
		this.env = canvasEnv;
		transform.scale(this.scale, -this.scale);
		transform.translate(-canvasEnv.getMinX(), -canvasEnv.getMinY()-(height/scale));
	}
	public Envelope getEnvelope() {
		return this.env;
	}
	
	/*
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
	*/
	public void drawFeatures(FeatureLayer<MultiPolygon> features) {
		for (Feature<MultiPolygon> feature : features) {
			setFillColour(feature.getColour());
			drawMultiPolygon(feature.getGeometry());
		}
	}
    
	public void drawMultiPolygon(MultiPolygon mp){
        	for (int i = 0; i < mp.getNumGeometries(); i++) {
        		drawPolygon((Polygon) mp.getGeometryN(i));
			}
	}
	
	private void drawPolygon(Polygon p){
        	LineString exteriorRing =  p.getExteriorRing();
        	drawLineString(exteriorRing);
        	
        	for (int i = 0; i < p.getNumInteriorRing(); i++) {
	        	drawLineString(p.getInteriorRingN(i));
			}
        	setFillColour();
	        cr.fillPreserve();
	        setLineColour();
    		cr.stroke();
	}
	
	private void drawLineString(LineString ls) {
		
        	Coordinate[] coordinates = ls.getCoordinates();
        	for (int i = 0; i < coordinates.length; i++) {
				Coordinate c = coordinates[i];
        		if (i==0){
	        		moveTo(c.x, c.y);
        		} else {
	        		lineTo(c.x, c.y);
        		} 
			}
		
	}
	
	public void drawFeatures2(FeatureLayer<MultiPolygon> features) {
		for (Feature<MultiPolygon> feature : features) {
			setFillColour(feature.getColour());
			drawMultiPolygon2(feature.getGeometry());
		}
	}
	
	public void drawMultiPolygon2(MultiPolygon mp){
        	for (int i = 0; i < mp.getNumGeometries(); i++) {
        		drawPolygon2((Polygon) mp.getGeometryN(i));
			}
	}
	
	private void drawPolygon2(Polygon p){
        	LineString exteriorRing =  p.getExteriorRing();
        	drawLineString(exteriorRing);
        	
        	for (int i = 0; i < p.getNumInteriorRing(); i++) {
	        	drawLineString(p.getInteriorRingN(i));
			}
        	setFillColour();
	        cr.fillPreserve();
	        cr.save();
		        cr.clipPreserve();
		        setLineColour();
		        paintCrossHatch();
	        cr.restore();
    		cr.stroke();
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
	
	public Point2D.Double transform(double x, double y) {
		    Point2D.Double pt = new Point2D.Double(x,y);
			transform.transform(pt, pt);
			return pt;
	}

	public PdfSurface getSurface() {
		return pdf;
	}
	
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}

	public double getScale() {
		return scale;
	}


}
