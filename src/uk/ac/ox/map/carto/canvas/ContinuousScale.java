package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import org.freedesktop.cairo.LinearPattern;

import uk.ac.ox.map.carto.canvas.Rectangle.Anchor;
import uk.ac.ox.map.imageio.RenderScale;

/**
 * 
 * @author will
 * 
 * Creates a linear gradient scale.
 * 
 * Caches the colour stops to enable construction of LinearPattern when it is drawn on the map
 * done because we don't know until draw time where the gradient should be. 
 *
 * Also provides a 
 *
 */

public class ContinuousScale implements DrawSurround, RenderScale {
	
	double[] r = new double[256];
	double[] g = new double[256];
	double[] b = new double[256];

	private LinearPattern lp;
	private final List<ColourStop> colourStops;
	private final Rectangle rect;
	
	private final Orientation orientation;
	private String title;
	
	private enum Orientation {NS, EW};	

	private class ColourStop {
		final String annotation;
		final double[] colourStop;
		public ColourStop(String annotation, double[] cs) {
			this.colourStop = cs;
			this.annotation = annotation;
		}	
	}
	
	public ContinuousScale(Rectangle rect, String title) {
		colourStops = new ArrayList<ColourStop>();
		this.rect = rect;
		orientation = (rect.height >= rect.width)? Orientation.NS: Orientation.EW;
		this.title = title;
	}

	/**
	 * Adds a colour stop.
	 */
	public void addColorStopRGB(String annotation, double d, double e, double f, double g) {
		colourStops.add(new ColourStop(annotation, new double[] {d, e, f, g}));
	}
	
	/**
	 * TODO: Violates encapsulation.
	 * Takes a cairo context and draws itself.
	 */
	public void draw(MapCanvas mapCanvas) {
		Point2D.Double ul = rect.getUpperLeft();
		Double startPoint; 
		Double endPoint; 
		
		
		if (orientation.equals(Orientation.NS)) {
			startPoint = new Point2D.Double(rect.x, rect.y + rect.height);
			endPoint = new Point2D.Double(rect.x, rect.y);
		} else {
			startPoint = new Point2D.Double(rect.x, rect.y);
			endPoint = new Point2D.Double(rect.x + rect.width, rect.y);
		}
		
		lp = new LinearPattern(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		
		double[] cs;
		for (ColourStop colourStop : colourStops) {
			cs = colourStop.colourStop;
			lp.addColorStopRGB(cs[0], cs[1], cs[2], cs[3]);
		}
		
		mapCanvas.cr.save();
		mapCanvas.cr.setSource(lp);
		mapCanvas.cr.rectangle(rect.x, rect.y, rect.width, rect.height);
		mapCanvas.cr.fillPreserve();
		
		//TODO: hardcoded source colour
		mapCanvas.cr.setSource(0, 0, 0);
		//TODO: hardcoded linewidth
		mapCanvas.cr.setLineWidth(0.2);
		mapCanvas.cr.stroke();
		mapCanvas.cr.restore();
		
		for (ColourStop colourStop : colourStops) {
			if (colourStop.annotation == null)
				continue;
	    	mapCanvas.annotateMap(
	    			colourStop.annotation, 
	    			rect.x+rect.width+5, 
					(rect.y + rect.height) - (rect.height * colourStop.colourStop[0]), 
					Anchor.LC
			);
		}
		
    	
//    	mapCanvas.annotateMap("Incidence (‰)", rect.x, rect.y-10, AnchorX.L, AnchorY.B);
    	mapCanvas.annotateMap(title, rect.x, rect.y-10, Anchor.LB);
	}

	/**
	 * Takes a pair of colourstops and a value to interpolate. Returns an rgb triplet.
	 * 
	 * @param x
	 * @param cs0
	 * @param cs1
	 * @return [r,g,b]
	 */
	private double[] interpolate(double x, double[] cs0, double[] cs1) {
		assert (x <= 1);
		
		double y, y0, x0, x1, y1;
		double[] ret = new double[3];
	
		/*
		 * X axis;
		 */
		x0 = cs0[0];
		x1 = cs1[0];
	
		for (int i = 0; i < ret.length; i++) {
			y0 = cs0[i + 1];
			y1 = cs1[i + 1];
	
			y = y0 + ((x - x0) * ((y1 - y0) / (x1 - x0)));
	
			ret[i] = y;
		}
	
		return ret;
	}

	public void finish() {
		
		ColourStop prevCS = colourStops.subList(0, 1).get(0);
		ColourStop thisCS;
		
		for (int i = 1; i < colourStops.size(); i++) {
			
			thisCS = colourStops.get(i);
			
			/*
			 * Get upper and lower bounds, flooring for consistency. 
			 */
			int b0 = (int) Math.floor(prevCS.colourStop[0] * 255);
			int b1 = (int) Math.floor(thisCS.colourStop[0] * 255);
			
			for (int j = b0; j < b1; j++) {
				double x = ((double) j) / 255;
				double[] interp = interpolate(x, prevCS.colourStop, thisCS.colourStop);
				r[j] = interp[0];
				g[j] = interp[1];
				b[j] = interp[2];
			}
			prevCS = colourStops.get(i);
		}
	}
	
	public double getRed(float f) {
		int i = (int) (f * 255);
		return this.r[i];
	}

	public double getGreen(float f) {
		int i = (int) (f * 255);
		return this.g[i];
	}

	public double getBlue(float f) {
		int i = (int) (f * 255);
		return this.b[i];
	}
	public double getAlpha(float f) {
		//FIXME: quick hack
		return 1;
	}
}
