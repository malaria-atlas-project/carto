package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.LinearPattern;

import uk.ac.ox.map.carto.canvas.MapCanvas.AnchorX;
import uk.ac.ox.map.carto.canvas.MapCanvas.AnchorY;
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
	private final List<double[]> colourStops;
	private final Rectangle rect;

	public ContinuousScale(Rectangle rect) {
		colourStops = new ArrayList<double[]>();
		this.rect = rect;
	}

	/**
	 * Adds a colour stop.
	 */
	public void addColorStopRGB(double d, double e, double f, double g) {
		colourStops.add(new double[] {d, e, f, g});
	}
	
	/**
	 * Takes a cairo context and draws itself.
	 */
	public void draw(MapCanvas mapCanvas) {
		Point2D.Double ul = rect.getUpperLeft();
		Double startPoint; 
		Double endPoint; 
		
		startPoint = new Point2D.Double(rect.x, rect.y);
		if (rect.height >= rect.width) { //N-S
			endPoint = new Point2D.Double(rect.x, rect.y + rect.height);
		} else { //E-W
			endPoint = new Point2D.Double(rect.x + rect.width, rect.y);
		}
		
		lp = new LinearPattern(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		for (double[] cs : colourStops) {
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
    	mapCanvas.annotateMap("0", rect.x+rect.width+5, rect.y, AnchorX.L, AnchorY.C);
    	mapCanvas.annotateMap("2.5", rect.x+rect.width+5, rect.y+rect.height, AnchorX.L, AnchorY.C);
    	mapCanvas.annotateMap("Incidence (‰)", rect.x, rect.y-10, AnchorX.L, AnchorY.B);
	}
}
