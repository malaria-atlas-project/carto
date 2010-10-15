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
	private final List<ScaleAnnotation> scaleAnnotations = new ArrayList<ContinuousScale.ScaleAnnotation>();
	private final Rectangle rect;
	
	private final Orientation orientation;
	private final String title;
	private final String description;
	//FIXME: hack with getting last number and normalizing scale
	private double normalization;
	
	private enum Orientation {NS, EW}	

	private class ColourStop {
		final String annotation;
		final double[] colourStop;
		public ColourStop(String annotation, double[] cs) {
			this.colourStop = cs;
			this.annotation = annotation;
		}	
	}
	
	private class ScaleAnnotation {
		public ScaleAnnotation(String annotation, double ratio) {
			this.ratio = ratio;
			this.annotation = annotation;
		}
		final double ratio;
		final String annotation;
	}
	
	public ContinuousScale(Rectangle rect, String title, String description) {
		colourStops = new ArrayList<ColourStop>();
		this.rect = rect;
		orientation = (rect.height >= rect.width)? Orientation.NS: Orientation.EW;
		this.title = title;
		this.description = description;
	}

	/**
	 * Adds a colour stop.
	 * @param ignoreRGB TODO
	 */
	public void addColorStopRGB(String annotation, double d, double e, double f, double g, boolean ignoreRGB) {
		if (!ignoreRGB)	{
			colourStops.add(new ColourStop(annotation, new double[] {d, e, f, g}));
		}
		scaleAnnotations.add(new ScaleAnnotation(annotation, d));
	}
	
	/**
	 * TODO: Violates encapsulation.
	 * Takes a cairo context and draws itself.
	 */
	public void draw(MapCanvas mapCanvas) {
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
			lp.addColorStopRGB(cs[0]*normalization, cs[1]*normalization, cs[2]*normalization, cs[3]*normalization);
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
		
		
		for (ScaleAnnotation colourStop : scaleAnnotations) {
			if (colourStop.annotation == null)
				continue;
			if (orientation.equals(Orientation.NS)) {
		    	mapCanvas.annotateMap(
		    			colourStop.annotation, 
		    			rect.x+rect.width+5, 
						(rect.y + rect.height) - (rect.height * (colourStop.ratio * normalization)), 
						Anchor.LC
				);
			} else {
		    	mapCanvas.annotateMap(
		    			colourStop.annotation, 
		    			(rect.x + (rect.width * (colourStop.ratio * normalization))),
						(rect.y + rect.height),
						Anchor.CT
				);
			}
		}
		
    	
//    	mapCanvas.annotateMap("Incidence (‰)", rect.x, rect.y-10, AnchorX.L, AnchorY.B);
		//TODO: hackery with description
		if (description == null) {
	    	mapCanvas.annotateMap(title, rect.x, rect.y-5, Anchor.LB);
		} else {
	    	mapCanvas.annotateMap(title, rect.x, rect.y-18, Anchor.LB);
	    	mapCanvas.annotateMap(description, rect.x, rect.y-5, Anchor.LB);
		}
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
		double max = colourStops.get(colourStops.size()-1).colourStop[0];
		normalization = 1/max;
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
	public void printHex() {
		for (ColourStop colourStop : colourStops) {
			String s = "";
			for (int i = 1; i < colourStop.colourStop.length; i++) {
				String hex = Integer.toString((int) Math.floor(colourStop.colourStop[i] * 255), 16);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				s = s + hex;
			}
			System.out.println(s);
		}
	}
}
