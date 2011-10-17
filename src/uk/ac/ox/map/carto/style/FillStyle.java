package uk.ac.ox.map.carto.style;

import java.util.ArrayList;
import java.util.List;


/**
 * Can represent a solid fill, a line fill, a picture fill or SVG fill
 * Includes outline
 */
public class FillStyle {
  
  public FillStyle(LineStyle outline) {
    this.outline = outline;
  }
  
  public final LineStyle outline;
  
  public final List<IsFillLayer> layers = new ArrayList<IsFillLayer>();
  
}
