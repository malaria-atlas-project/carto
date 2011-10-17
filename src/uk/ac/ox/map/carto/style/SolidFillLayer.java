package uk.ac.ox.map.carto.style;

import uk.ac.ox.map.domain.carto.Colour;

public class SolidFillLayer implements IsFillLayer {
  
  public SolidFillLayer(Colour colour) {
    this.colour = colour;
  }
  
  public final Colour colour;

}
