package uk.ac.ox.map.carto.canvas;

import java.io.IOException;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.Surface;

import uk.ac.ox.map.carto.style.IsFillLayer;
import uk.ac.ox.map.carto.style.LineFillLayer;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.SolidFillLayer;
import uk.ac.ox.map.carto.style.StippleFillLayer;
import uk.ac.ox.map.domain.carto.Colour;

/**
 * Encapsulates a cairo context
 * Currently if the fillColour is set, it will be used for multiple calls. 
 * 
 */

public abstract class BaseCanvas {
	
	protected final Context cr;
	protected final Surface surface;
	private Colour fillColour;
	private Colour lineColour;
	protected final double width;
	protected final double height;

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

	public void setBackgroundColour(uk.ac.ox.map.domain.carto.Colour backgroundColour) {
		setColour(backgroundColour);
		cr.rectangle(0, 0, width, height);
		cr.fill();
	}

	public void rectangle(double x, double y, double width, double height) {
		cr.rectangle(x, y, width, height);
		cr.fill();
	}
	
	public void drawTestRectangle(Rectangle r) {
		setFillColour(new uk.ac.ox.map.domain.carto.Colour("#000000", 1));
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
	
	public void finish() {
    surface.finish();
  }

  /**
   * Paints an arbitrary pattern.
   * returns true if the current strokes have been consumed which is necessary for
   * 
   * @param layer
   *     the pattern layer to draw
   * @return
   */
  protected boolean paintFill(IsFillLayer layer) {
    if (layer instanceof SolidFillLayer) {
      SolidFillLayer lyr = (SolidFillLayer) layer;
      setFillColour(lyr.colour);
      cr.fillPreserve();
      return false;
    } else if (layer instanceof LineFillLayer) {
      cr.save();
      cr.clip();
      paintLineLayer((LineFillLayer) layer);
  
      cr.restore();
      return true;
    } else if (layer instanceof StippleFillLayer) {
      cr.save();
      cr.clip();
      paintStippleFill((StippleFillLayer) layer);
      cr.restore();
      return true;
      
    } else {
      throw new RuntimeException("Unknown FillLayer type.");
    }
  }

  protected void paintLineLayer(LineFillLayer lyr) {
  		setLineColour(lyr.lineStyle.getLineColour());
  		cr.setLineWidth(lyr.lineStyle.getLineWidth());
  		
  		/*
  		 * Angle is analogous to compass bearings
  		 */
  //		double angle = Math.toRadians(lyr.angle);
  //		double offset = Math.tan(angle) * height;
  		
  		/*
  		 * Offset is the largest dimension of the canvas to ensure complete coverage.
  		 */
  		double offset = (height > width) ? height : width;
  		
  		double x1 = -offset;
  		System.out.println(offset);
  		x1 += lyr.offset;
  		int y1 = 0;
  		for (int i = 0; i < (offset * 2); i += lyr.spacing) {
  			cr.moveTo(x1, y1);
  			cr.lineTo(x1 + offset, y1 + offset);
  			cr.stroke();
  			x1 += lyr.spacing;
  		}
  	}

  void paintStippleFill(StippleFillLayer layer) {
    
  	double spacing = layer.spacing;
  	
  	cr.setDash(new double[] { 1, spacing });
  	for (int j = 0; j < height; j += spacing) {
  		cr.moveTo(0, j);
  		cr.lineTo(width, j);
  		cr.stroke();
  	}
  	cr.setDash(new double[] { 1, spacing });
  	for (int i = 0; i < height; i += spacing) {
  		cr.moveTo(i + 0.5, -0.5);
  		cr.lineTo(i + 0.5, height - 0.5);
  		cr.stroke();
  	}
  }
	
}
