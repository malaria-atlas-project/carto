package uk.ac.ox.map.carto.canvas;

import java.io.IOException;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.Surface;

import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.deps.Colour;

/**
 * Encapsulates a cairo context, embracing the fact a cairo context is a state machine.
 * This means that if e.g. the fillColour is set, it will be used for multiple calls. 
 * 
 */

public abstract class BaseCanvas {
	protected final Context cr;
	protected final Surface surface;
	private Colour fillColour;
	private Colour lineColour;
	protected final double width;
	protected final double height;

	public void setBackgroundColour(Colour backgroundColour) {
		setColour(backgroundColour);
		cr.rectangle(0, 0, width, height);
		cr.fill();
	}

	public void rectangle(double x, double y, double width, double height) {
		cr.rectangle(x, y, width, height);
		cr.fill();
	}
	
	public void drawTestRectangle(Rectangle r) {
		setFillColour(new Colour("#000000", 1));
		cr.setLineWidth(1);
		cr.rectangle(r.x, r.y, r.width, r.height);
		cr.stroke();
	}
	
	public void setColour(Colour c) {
		cr.setSource(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	protected void setLineColour() {
		cr.setSource(lineColour.getRed(), lineColour.getGreen(), lineColour.getBlue(), lineColour.getAlpha());
	}

	protected void setFillColour() {
		cr.setSource(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue(), fillColour.getAlpha());
	}

	public BaseCanvas(Surface surface, double width2, double height2) {
		/*
		 * Get context
		 */
		cr = new Context(surface);
		/*
		 * Initialise styles to sensible defaults.
		 */
		this.lineColour = Palette.BLACK.get();
		this.fillColour = Palette.GREY_20.get();
		this.width = width2;
		this.height = height2;
		this.surface = surface;
	}

	public void setFillColour(Colour fillColour) {
		this.fillColour = fillColour;
		setFillColour();
	}

	public Colour getFillColour() {
		return fillColour;
	}

	public void setLineColour(Colour lineColour) {
		this.lineColour = lineColour;
		setLineColour();
	}

	public Colour getLineColour() {
		return lineColour;
	}

	public PdfSurface getSurfacePattern() {
		PdfSurface tempPdf = null;
		double w = width, h = height;
		try {
	        tempPdf = new PdfSurface(null, w, h);
        } catch (IOException e) {
	        e.printStackTrace();
        }
        Context tempCr = new Context(tempPdf);
        tempCr.setLineWidth(0.2);
        tempCr.setSource(1, 1, 1);
        tempCr.rectangle(0, 0, w, h);
        tempCr.fill();
        tempCr.setSource(0,0,0);
        
		int spacing = 3;
		double offset = (h > w) ? h : w;
		double x1 = -offset;
		int y1 = 0;
		for (int i = 0; i < (offset * 2); i += spacing) {
			tempCr.moveTo(x1, y1);
			tempCr.lineTo(x1 + offset, y1 + offset);
			tempCr.stroke();
			x1 += spacing;
		}
		tempPdf.flush();
//		tempPdf.finish();
		return tempPdf;
	}
	void paintCrossHatch() {
		/*
		 * Very simple and unoptimised. Just draws a parallelogram of hatchings.
		 * The size is determined by the largest dimension of the map canvas to
		 * always be inclusive.
		 */
		int spacing = 3;
		double offset = (height > width) ? height : width;
		double x1 = -offset;
		int y1 = 0;
		for (int i = 0; i < (offset * 2); i += spacing) {
			cr.moveTo(x1, y1);
			cr.lineTo(x1 + offset, y1 + offset);
			cr.stroke();
			x1 += spacing;
		}
	}
	
	void paintDuffy() {
		int spacing = 9;
		setLineColour(Palette.ORANGE_DARK.get());
		cr.setLineWidth(1.5);
		double offset = (height > width) ? height : width;
		double x1 = -offset;
		int y1 = 0;
		for (int i = 0; i < (offset * 2); i += spacing) {
			cr.moveTo(x1, y1 + offset);
			cr.lineTo(x1 + offset, y1);
			cr.stroke();
			x1 += spacing;
		}
	}

	void paintStipple() {
		/*
         * 
         */

		int spacing = 3;
		cr.setDash(new double[] { 1, 3 });
		for (int j = 0; j < height; j += spacing) {
			cr.moveTo(0, j);
			cr.lineTo(width, j);
			cr.stroke();
		}
		cr.setDash(new double[] { 1, 2 });
		spacing += 1;
		for (int i = 0; i < height; i += spacing) {
			cr.moveTo(i + 0.5, -0.5);
			cr.lineTo(i + 0.5, height - 0.5);
			cr.stroke();
		}
	}
}
