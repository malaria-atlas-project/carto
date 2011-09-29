package uk.ac.ox.map.carto.canvas;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.map.domain.carto.Colour;
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
 */
public class ContinuousScale implements RenderScale {
	
	double[] r = new double[256];
	double[] g = new double[256];
	double[] b = new double[256];

	private final List<ColourStop> colourStops = new ArrayList<ColourStop>();
	private final List<ScaleAnnotation> scaleAnnotations = new ArrayList<ContinuousScale.ScaleAnnotation>();
	
	//FIXME: hack with getting last number and normalizing scale
	private double normalization;

	public class ColourStop {
		final double[] colourStop;
		public ColourStop(String annotation, double[] cs) {
			this.colourStop = cs;
		}	
	}
	
	public class ScaleAnnotation {
		public ScaleAnnotation(String annotation, double ratio) {
			this.ratio = ratio;
			this.annotation = annotation;
		}
		final double ratio;
		final String annotation;
	}
	
	
	public void addColorStopRGB(String annotation, double d, Colour colour, boolean ignoreRGB) {
		if (!ignoreRGB)	{
			colourStops.add(new ColourStop(annotation, new double[] {d, colour.getRed(), colour.getGreen(), colour.getBlue()}));
		}
		scaleAnnotations.add(new ScaleAnnotation(annotation, d));
	}
	
	/**
	 * Takes a pair of colourstops and an x axis value to determine. Returns an rgb triplet.
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
		int b0 = (int) Math.floor(prevCS.colourStop[0] * 255);
		
		ColourStop thisCS;
		
		for (int i = 1; i < colourStops.size(); i++) {
			
			thisCS = colourStops.get(i);
			
			/*
			 * Get upper bound of colourstop
			 */
			int b1 = (int) Math.ceil(thisCS.colourStop[0] * 255);
			if (thisCS.colourStop[0] == 1.0) {
			  b1 = 256;
			}
			
			for (int j = b0; j < b1; j++) {
				double x = ((double) j) / 255;
				double[] interp = interpolate(x, prevCS.colourStop, thisCS.colourStop);
				r[j] = interp[0];
				g[j] = interp[1];
				b[j] = interp[2];
			}
			prevCS = colourStops.get(i);
			b0 = b1;
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
	
	/**
	 * Not implemented, always returns one.
	 */
	public double getAlpha(float f) {
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

  public List<ColourStop> getColourStops() {
    return colourStops;
  }

  public List<ScaleAnnotation> getScaleAnnotations() {
    return scaleAnnotations;
  }

  /**
   * @return the fraction to multiply colourstops by to normalize to a 0..1 scale.
   */
  public double getNormalization() {
    return normalization;
  }
}
