package uk.ac.ox.map.carto.style;


import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * 
 */
public class PolygonSymbolizer {
  private LineStyle lineStyle;
  private FillStyle fillStyle;
  private MultiPolygon mp;

  public PolygonSymbolizer(MultiPolygon mp, FillStyle fs, LineStyle ls) {
    this.mp = mp;
    this.lineStyle = ls;
    this.fillStyle = fs;
  }

  public void setLineStyle(LineStyle lineStyle) {
    this.lineStyle = lineStyle;
  }

  public LineStyle getLineStyle() {
    return lineStyle;
  }

  public void setFillStyle(FillStyle fillStyle) {
    this.fillStyle = fillStyle;
  }

  public FillStyle getFillStyle() {
    return fillStyle;
  }

  public void setMp(MultiPolygon mp) {
    this.mp = mp;
  }

  public MultiPolygon getMp() {
    return mp;
  }

}
