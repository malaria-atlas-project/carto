package uk.ac.ox.map.carto.canvas;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;

import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.Surface;
import org.gnome.gdk.Pixbuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.canvas.style.Palette;
import uk.ac.ox.map.carto.feature.Feature;
import uk.ac.ox.map.carto.feature.FeatureLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author will
 * 
 * Adds polygon and raster drawing facilities to BaseCanvas.
 *  
 */
public class DataFrame extends BaseCanvas {

	private final Logger logger = LoggerFactory.getLogger(DataFrame.class);
	private Envelope env;
	private double scale;
	private final boolean hasGrid;
	private final AffineTransform transform = new AffineTransform();
	private final Point2D.Double origin;

	public static class Builder {
		//required params
		private Envelope env;
		private Rectangle rect;
		private String fileName;
		
		//optional params
		private boolean hasGrid = true;
		private Colour backgroundColour = Palette.WHITE.get();

		public Builder(Envelope dataEnv, Rectangle rect, String fileName) {
			this.env = dataEnv;
			this.rect = rect;
			this.fileName = fileName;
	    }	
		public Builder hasGrid(boolean hasGrid) {
			this.hasGrid = hasGrid;
			return this;
		}
		public Builder backgroundColour(Colour colour) {
			this.backgroundColour = colour;
			return this;
		}
		public DataFrame build() throws IOException {
			return new DataFrame(this);
        }
	}
	
	public DataFrame(Builder builder) throws IOException {
		
		super((Surface) new PdfSurface(builder.fileName, builder.rect.width, builder.rect.height), builder.rect.width, builder.rect.height);

		setEnvelope(builder.rect.width, builder.rect.height, builder.env);
		this.origin = builder.rect.getUpperLeft();
		
		setBackgroundColour(builder.backgroundColour);

		cr.setLineWidth(0.2);
		cr.setSource(0.0, 1.0, 0.0, 1.0);
		this.hasGrid = builder.hasGrid;
    }
	
	public void addRasterLayer(Raster ras) {
		cr.save();
		
		Pixbuf pb = ras.getPixbuf();
		Point2D.Double pt = ras.getOrigin();
		
		logger.debug("Pixbuf width: {}", pb.getWidth());
		logger.debug("Pixbuf height: {}", pb.getHeight());
		logger.debug("Orig x: {}", pt.x);
		logger.debug("Orig y: {}", pt.y);
		
		transform.transform(pt, pt);
		double cellSize = ras.getCellSize();
		
		/*
		 * Determine the factor by which to scale the canvas to draw the image.
		 * TODO: Differing scales. 
		 * cellSize is dd / pix 
		 * scale is pix / dd 
		 */
		double newScale = scale * cellSize;
		
		cr.scale(newScale, newScale);
		/*
		 * The scaling will move the origin, 
		 * therefore this needs to be corrected.
		 */
		cr.setSource(pb, pt.x/newScale, pt.y/newScale);
		
		cr.paint();
		cr.restore();
		
	}

	private void setEnvelope(double width, double height, Envelope dataEnv) {
		double dX = dataEnv.getWidth();
		double dY = dataEnv.getHeight();

		double arEnv = dX / dY;
		double arCanvas = width / height;

		/*
		 * Fix the scale
		 */
		dX = dataEnv.getWidth();
		dY = dataEnv.getHeight();
		double xScale = width / dX;
		double yScale = height / dY;
		this.scale = (xScale < yScale) ? xScale : yScale;

		/*
		 * Expand env to fit whole canvas
		 */
		Envelope canvasEnv = new Envelope();
		if (arCanvas > arEnv) {
			// canvas is fatter than data- expand env in x direction
			// ll
			double dW = (width / scale) - dX;
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMinX() - (dW / 2), dataEnv.getMinY()));
			// ur
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMaxX() + (dW / 2), dataEnv.getMaxY()));
		} else if (arCanvas < arEnv) {
			double dH = (height / scale) - dY;
			// canvas is thinner than data- expand env in y direction
			// ll
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMinX(), dataEnv.getMinY() - (dH / 2)));
			// ur
			canvasEnv.expandToInclude(new Coordinate(dataEnv.getMaxX(), dataEnv.getMaxY() + (dH / 2)));
		} else {
			// aspect ratios are the same
			canvasEnv = dataEnv;
		}

		/*
		 * Set transform
		 */
		this.env = canvasEnv;
		transform.scale(this.scale, -this.scale);
		transform.translate(-canvasEnv.getMinX(), -canvasEnv.getMinY() - (height / scale));
	}

	public Envelope getEnvelope() {
		return this.env;
	}

	/*
	 * private void debugTransform(Envelope env){
	 * System.out.println("X Scale: "+transform.getTranslateX());
	 * System.out.println("Y Scale: "+transform.getTranslateY()); Point2D.Double
	 * llPt = new Point2D.Double(env.getMinX(), env.getMinY()); Point2D.Double
	 * urPt = new Point2D.Double(env.getMaxX(), env.getMaxY());
	 * 
	 * System.out.println("before:"); System.out.println(llPt);
	 * System.out.println(urPt); transform.transform(llPt, llPt);
	 * transform.transform(urPt, urPt); System.out.println("after:");
	 * System.out.println(llPt); System.out.println(urPt); }
	 */
	public void drawFeatures(FeatureLayer<MultiPolygon> features) {
		for (Feature<MultiPolygon> feature : features) {
			setFillColour(feature.getColour());
			drawMultiPolygon(feature.getGeometry());
		}
	}

	public void drawMultiPolygon(MultiPolygon mp) {
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			drawPolygon((Polygon) mp.getGeometryN(i));
		}
	}

	private void drawPolygon(Polygon p) {
		LineString exteriorRing = p.getExteriorRing();
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
			if (i == 0) {
				moveTo(c.x, c.y);
			} else {
				lineTo(c.x, c.y);
			}
		}

	}

	public void drawFeatures2(FeatureLayer<MultiPolygon> features, String styleType) {
		for (Feature<MultiPolygon> feature : features) {
			setFillColour(feature.getColour());
			drawMultiPolygon2(feature.getGeometry(), styleType);
		}
	}

	public void drawMultiPolygon2(MultiPolygon mp, String styleType) {
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			drawPolygon2((Polygon) mp.getGeometryN(i), styleType);
		}
	}

	private void drawPolygon2(Polygon p, String styleType) {
		LineString exteriorRing = p.getExteriorRing();
		drawLineString(exteriorRing);

		for (int i = 0; i < p.getNumInteriorRing(); i++) {
			drawLineString(p.getInteriorRingN(i));
		}
		setFillColour();
		cr.fillPreserve();
		cr.save();
		cr.clipPreserve();
		setLineColour();
		if (styleType.compareTo("hatched") == 0)
			paintCrossHatch();
		else if (styleType.compareTo("stipple") == 0)
			paintStipple();
		cr.restore();
		cr.stroke();
	}

	/**
	 * Draws a line from the current position to the position specified in x and
	 * y. TODO: transform the whole set of coordinates in a polygon instead of
	 * multiple calls to transform?
	 * 
	 * @param x
	 *            the x Coordinate in Geographic coordinates
	 * @param y
	 *            the y Coordinate in Geographic coordinates
	 * @return void
	 */
	private void lineTo(double x, double y) {
		Point2D.Double pt = new Point2D.Double(x, y);
		transform.transform(pt, pt);
		cr.lineTo(pt.getX(), pt.getY());
	}

	private void moveTo(double x, double y) {
		Point2D.Double pt = new Point2D.Double(x, y);
		transform.transform(pt, pt);
		cr.moveTo(pt.getX(), pt.getY());
	}

	public Point2D.Double transform(double x, double y) {
		Point2D.Double pt = new Point2D.Double(x, y);
		transform.transform(pt, pt);
		return pt;
	}

	public Surface getSurface() {
		return surface;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getScale() {
		return scale;
	}

	public boolean hasGrid() {
		return hasGrid;
	}

	public Point2D.Double getOrigin() {
		return origin;
    }

	public void finish() {
	   surface.finish(); 
    }

}
