package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;

import uk.ac.ox.map.carto.canvas.style.Colour;

public abstract class BaseCanvas {
	protected final Context cr;
	protected final PdfSurface pdf;
	private Colour fillColour;
	private Colour lineColour;
	private double lineWidth;
    protected final int width;
    protected final int height;

	
    public void setBackgroundColour(String hex, float a){
    	setColour(hex, a);
		cr.rectangle(0, 0, width, height);
		cr.fill();
    }
    
    protected void setColour(String hex, float a) {
    	Colour c = new Colour(hex, a);
		cr.setSource(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
	
	protected void setLineColour(String hex, float a) {
		lineColour = new Colour(hex, a);
		setLineColour();
	}
	protected void setLineColour() {
		cr.setSource(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue(), lineColour.getAlpha());
	}
	
	protected void setFillColour(String hex, float a) {
		fillColour = new Colour(hex, a);
		setFillColour();
	}
	
	protected void setFillColour() {
		cr.setSource(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue(), fillColour.getAlpha());
	}
	
	public BaseCanvas(PdfSurface pdf, int width, int height) {
	   /*
	    * Get context 
	    */
		cr = new Context(pdf);
		/*
		 * Initialise styles
		 */
		setLineColour("#000000", 1);
		setLineWidth(0.2);
		setFillColour("#CCCCCC", (float) 0.8);
		this.width = width;
		this.height = height;
		this.pdf = pdf;
	}

	public void setFillColour(Colour fillColour) {
		this.fillColour = fillColour;
	}

	public Colour getFillColour() {
		return fillColour;
	}

	public void setLineColour(Colour lineColour) {
		this.lineColour = lineColour;
	}

	public Colour getLineColour() {
		return lineColour;
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	public double getLineWidth() {
		return lineWidth;
	}

}
