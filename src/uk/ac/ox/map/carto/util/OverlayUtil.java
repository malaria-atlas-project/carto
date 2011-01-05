package uk.ac.ox.map.carto.util;

import java.io.FileNotFoundException;

import org.gnome.gdk.Pixbuf;
import org.gnome.rsvg.Handle;

import uk.ac.ox.map.carto.canvas.MapCanvas;
import uk.ac.ox.map.carto.canvas.Rectangle;

public class OverlayUtil {
  
  public static void drawStaticOverlays(MapCanvas mapCanvas) throws FileNotFoundException {
    
    Handle svgH = new Handle("north_arrow.svg");
    mapCanvas.drawSVG(new Rectangle(10, 550, 20, 100), svgH);
    
    Handle svgH2 = new Handle("by-nc-sa.svg");
    mapCanvas.drawSVG(new Rectangle(25, 483, 60, 12), svgH2);
    
    Pixbuf pbLogo = new Pixbuf("map_logo.png");
    mapCanvas.drawImage(new Rectangle(295, 465, 180, 30), pbLogo);
  	
    
  }

}
