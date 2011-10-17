package uk.ac.ox.map.carto.style;

import uk.ac.ox.map.domain.carto.Colour;

public class StippleFillLayer implements IsFillLayer {
  
  public StippleFillLayer(Colour colour, double spacing) {
    this.colour = colour;
    this.spacing = spacing;
  }
  
  public final Colour colour;
  
  public final double spacing;

}
