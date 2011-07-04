package uk.ac.ox.map.carto;

import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.deps.Colour;

public class ContinuousScaleFactory {
	
	public static ContinuousScale getAmeRed(Rectangle rect) {
	  ContinuousScale cs = new ContinuousScale(rect, "Probability", null);
      cs.addColorStopRGB("0", 0, new Colour("#FFFFFF", 1), false);
      cs.addColorStopRGB("", 0.05, new Colour("#FAEFEA", 1), false);
      cs.addColorStopRGB("", 0.25, new Colour("#EDC2B1", 1), false);
      cs.addColorStopRGB("", 0.50, new Colour("#DF8F77", 1), false);
      cs.addColorStopRGB("", 0.75, new Colour("#D36049", 1), false);
      cs.addColorStopRGB("1", 1, new Colour("#C92127", 1), false);
      cs.finish();
	  return cs;
	}
	
	public static ContinuousScale getAmeGreen(Rectangle rect) {
	  ContinuousScale cs = new ContinuousScale(rect, "Probability", null);
      cs.addColorStopRGB("0", 0, new Colour("#FFFFFF", 1), false);
      cs.addColorStopRGB("", 0.05, new Colour("#F1F3EE", 1), false);
      cs.addColorStopRGB("", 0.25, new Colour("#C7D5C0", 1), false);
      cs.addColorStopRGB("", 0.50, new Colour("#97B68F", 1), false);
      cs.addColorStopRGB("", 0.75, new Colour("#699D66", 1), false);
      cs.addColorStopRGB("1", 1, new Colour("#2E8944", 1), false);
      cs.finish();
	  return cs;
	}
	
	public static ContinuousScale getAmeOrange(Rectangle rect) {
	  ContinuousScale cs = new ContinuousScale(rect, "Probability", null);
      cs.addColorStopRGB("0", 0, new Colour("#FFFFFF", 1), false);
      cs.addColorStopRGB("", 0.05, new Colour("#FDF3EC", 1), false);
      cs.addColorStopRGB("", 0.25, new Colour("#F8D3BA", 1), false);
      cs.addColorStopRGB("", 0.50, new Colour("#F2AC81", 1), false);
      cs.addColorStopRGB("", 0.75, new Colour("#EC8851", 1), false);
      cs.addColorStopRGB("1", 1, new Colour("#E76425", 1), false);
      cs.finish();
	  return cs;
	}
	
	public static ContinuousScale getAmeBrown(Rectangle rect) {
	  ContinuousScale cs = new ContinuousScale(rect, "Probability", null);
      cs.addColorStopRGB("0", 0, new Colour("#FFFFFF", 1), false);
      cs.addColorStopRGB("", 0.05, new Colour("#F3EDE9", 1), false);
      cs.addColorStopRGB("", 0.25, new Colour("#D6BDB3", 1), false);
      cs.addColorStopRGB("", 0.50, new Colour("#B88C7B", 1), false);
      cs.addColorStopRGB("", 0.75, new Colour("#9E6452", 1), false);
      cs.addColorStopRGB("1", 1, new Colour("#873E31", 1), false);
      cs.finish();
	  return cs;
	}
	
	public static ContinuousScale getAmeOld(Rectangle rect) {
	    ContinuousScale cs = new ContinuousScale(rect, "Probability", null);
	    cs.addColorStopRGB("0", 0.0, 255d / 255, 242d / 255, 0d / 255, false);
	    cs.addColorStopRGB(null, 0.5, 255d / 255, 251d / 255, 202d / 255, false);
	    cs.addColorStopRGB("0.5", 0.5, 191d / 255, 217d / 255, 178d / 255, false);
	    cs.addColorStopRGB("1", 1, 26d / 255, 152d / 255, 80d / 255, false);
	    cs.finish();
	    return cs;
	}

}
