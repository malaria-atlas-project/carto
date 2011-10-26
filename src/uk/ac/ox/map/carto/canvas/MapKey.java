package uk.ac.ox.map.carto.canvas;

import java.util.List;

public class MapKey {
  
  private final Rectangle rectangle; 
  
  private final List<MapKeyItem> mapKeyItems; 
  
  public double patchWidth = 25;
  public double patchHeight = 12.5;
  public double spacing = 22;
  public double textMargin = 35;
  
  double fontSize;
  
  public MapKey(Rectangle rect, List<MapKeyItem> mapKeyItems, double fontSize) {
    this.rectangle = rect;
    this.mapKeyItems = mapKeyItems;
  }
  
  public Rectangle getRectangle() {
    return rectangle;
  }

}
