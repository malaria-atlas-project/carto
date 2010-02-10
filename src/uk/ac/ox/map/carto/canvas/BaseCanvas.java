package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.canvas.style.LineStyle;
import uk.ac.ox.map.carto.canvas.style.PolygonStyle;

public abstract class BaseCanvas {
	protected final Context cr;
	private PolygonStyle polygonStyle;
	private LineStyle lineStyle;
	
	public void setLineStyle(LineStyle ls) {
		lineStyle = ls;
		setLineStyle();
	}

	public void setLineStyle() {
		/*
		 * Convenience method invoked by multiple draw methods
		 */
		Colour c = lineStyle.getLineColour();
		setColour(c);
		cr.setLineWidth(lineStyle.getLineWidth());
	}
	
	public void setPolygonStyle(PolygonStyle ps){
		polygonStyle = ps;
		setPolygonStyle();
	}
	
	public void setPolygonStyle() {
		/*
		 * Convenience method
		 */
		Colour c = polygonStyle.getLineColour();
		setColour(c);
		cr.setLineWidth(polygonStyle.getLineWidth());
	}
	
	private void setColour(Colour c){
		cr.setSource(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
	
	public BaseCanvas(PdfSurface pdf) {
	   /*
	    * Get context 
	    */
		cr = new Context(pdf);
	}

}
