package uk.ac.ox.map.carto.style;


/**
 * Represents an arbitrary line pattern that can be used as a fill
 * 
 * @author will
 * 
 */
public class LineFillLayer implements IsFillLayer {

  /**
   * TODO too many doubles with different effects in constructor
   * 
   * @param angle
   * @param spacing
   *          how much to space the lines
   * @param offset
   *          where to start drawing - allows multiple overlays 
   * @param lineStyle
   *          the colour and weight of the line
   */
  public LineFillLayer(double angle, double spacing, double offset,
      LineStyle lineStyle) {
    this.angle = angle;
    this.offset = offset;
    this.spacing = spacing;
    this.lineStyle = lineStyle;
  }

  public final double angle;

  public final double offset;

  public final double spacing;

  public final LineStyle lineStyle;

}
