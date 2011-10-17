package uk.ac.ox.map.carto;

import java.io.IOException;

import org.gnome.gtk.Gtk;
import org.junit.Test;

import uk.ac.ox.map.carto.canvas.DataFrame;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.LineFillLayer;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.carto.style.SolidFillLayer;
import uk.ac.ox.map.carto.util.GeometryUtil;
import uk.ac.ox.map.domain.carto.Colour;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class StyleTests {
  
  public StyleTests() {
    Gtk.init(null);
  }
  
  public FillStyle getfillStyle() {
    
    LineStyle outline = new LineStyle(Palette.GREY_20.get(), 0.2);
    FillStyle fs = new FillStyle(outline);
//    fs.layers.add(new SolidFillLayer(Palette.FERN.get()));
    fs.layers.add(new LineFillLayer(45, 5, 0, new LineStyle(Palette.CORAL.get(), 2)));
    
    return fs;
  }
  
	public FillStyle duffy() {
    LineStyle outline = new LineStyle(Palette.BLACK.get(0), 0);
    FillStyle fs = new FillStyle(outline);
    
    // Grey blue
    fs.layers.add(new LineFillLayer(25, 12, 0, new LineStyle(new Colour("#6B7EAE", 1), 2)));
    fs.layers.add(new LineFillLayer(45, 12, 4, new LineStyle(new Colour("#D2D2D2", 1), 4)));
    
    return fs;
		
	}

  
  @Test
  public void lineFillLayers() throws IOException {
    
//    MapCanvas mc = new MapCanvas(new PdfSurface("/tmp/lines.pdf", 100, 100), 100, 100);
    
    Envelope dataEnv = new Envelope(10, 20, 10, 20);
    MultiPolygon poly = GeometryUtil.getPolygonFromEnvelope(dataEnv);
//    dataEnv.expandBy(5);
    
    Rectangle rect = new Rectangle(0, 0, 100, 100);
    DataFrame df = new DataFrame.Builder(dataEnv, rect, "/tmp/lines.pdf").build();
    
    df.drawMultiPolygon(poly, duffy());
    
    df.finish();
    
  }

}
