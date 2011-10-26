package uk.ac.ox.map.carto.canvas;

import java.util.ArrayList;
import java.util.List;

public class MapKey {
  
  private final Rectangle rectangle; 
  
  private final List<MapKeyItem> mapKeyItems = new ArrayList<MapKeyItem>(); 
  
  public double patchWidth = 25;
  public double patchHeight = 12.5;
  public double spacing = 22;
  public double textMargin = 35;
  
  final double fontSize;
  private String title;
  
  public MapKey(Rectangle rect, double fontSize, String title) {
    this.rectangle = rect;
    this.title = title;
    this.fontSize = fontSize;
  }
  
  public Rectangle getRectangle() {
    return rectangle;
  }

  public List<MapKeyItem> getKeyItems() {
    return mapKeyItems;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void addItem(MapKeyItem item) {
    mapKeyItems.add(item);
  }

}
