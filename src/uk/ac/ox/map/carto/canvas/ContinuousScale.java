package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.LinearPattern;

import uk.ac.ox.map.carto.canvas.Rectangle.Anchor;
/**
 * 
 * @author will
 * 
 * Creates a linear gradient scale.
 * 
 * Caches the colour stops to enable construction of LinearPattern when it is drawn on the map
 * done because we don't know until draw time where the gradient should be. 
 *
 */

public class ContinuousScale implements DrawSurround {

	private LinearPattern lp;
	private final List<ColourStop> colourStops;
	private final Rectangle rect;
	
	private final Orientation orientation;
	
	private enum Orientation {NS, EW};	

	private class ColourStop {
		final String annotation;
		final double[] colourStop;
		public ColourStop(String annotation, double[] cs) {
			this.colourStop = cs;
			this.annotation = annotation;
		}	
	}
	
	public ContinuousScale(Rectangle rect) {
		colourStops = new ArrayList<ColourStop>();
		this.rect = rect;
		orientation = (rect.height >= rect.width)? Orientation.NS: Orientation.EW;
	}

	/**
	 * Adds a colour stop.
	 */
	public void addColorStopRGB(String annotation, double d, double e, double f, double g) {
		colourStops.add(new ColourStop(annotation, new double[] {d, e, f, g}));
	}
	
	/**
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
    	mapCanvas.annotateMap("Probability", rect.x, rect.y-10, Anchor.LB);
	}
}
