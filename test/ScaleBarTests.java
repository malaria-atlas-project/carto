import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gnome.gdk.PixbufFormat;
import org.gnome.gtk.Gtk;
import org.junit.Before;
import org.junit.Test;


import uk.ac.ox.map.carto.AMEMaps;
import uk.ac.ox.map.carto.canvas.ContinuousScale;
import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.imageio.FltReader2;
import uk.ac.ox.map.imageio.RasterLayer;

public class ScaleBarTests {

  private Map<String, Rectangle> frameConfLS;
  private File anoFolder;

  @Before
  public void init() {
    Gtk.init(null);

    this.frameConfLS = new HashMap<String, Rectangle>();
    frameConfLS.put("dataFrame", new Rectangle(0, 0, 400, 400));
    frameConfLS.put("continuousScale", new Rectangle(200, 200, 110, 12));

    final String anoDir = String.format("/home/will/map1_public/data/vector/species_mapping_outputs/Asia.eo_occurrence/Anopheles.%s_results", "subpictus");
    anoFolder = new File(anoDir);
  }
  
  @Test
  public void scaleGraphics() throws Exception {
    AMEMaps.drawScaleGraphic();
  }

  @Test
  public void anoRedGreen() throws IOException {

    ContinuousScale cs = new ContinuousScale(frameConfLS.get("continuousScale"), "Probability", null);
    cs.addColorStopRGB("0", 0.0, 26d / 255, 152d / 255, 80d / 255, false);
    cs.addColorStopRGB(null, 0.5, 191d / 255, 217d / 255, 178d / 255, false);
    cs.addColorStopRGB("0.5", 0.5, 1, 0.8, 0.8, false);
    cs.addColorStopRGB("1", 1, 0.870588235, 0.188235294, 0.152941176, false);
    cs.finish();

    RasterLayer ras = FltReader2.openFloatFile(anoFolder, "probability-map", cs);
    ras.getPixbuf().save("/tmp/subpictus-rg.png", PixbufFormat.PNG);
  }
  
  @Test
  public void anoYellowGreen() throws IOException {

    ContinuousScale cs = new ContinuousScale(frameConfLS.get("continuousScale"), "Probability", null);
    cs.addColorStopRGB("0", 0.0, 255d / 255, 242d / 255, 0d / 255, false);
    cs.addColorStopRGB(null, 0.5, 255d / 255, 251d / 255, 202d / 255, false);
    cs.addColorStopRGB("0.5", 0.5, 191d / 255, 217d / 255, 178d / 255, false);
    cs.addColorStopRGB("1", 1, 26d / 255, 152d / 255, 80d / 255, false);
    cs.finish();

    RasterLayer ras = FltReader2.openFloatFile(anoFolder, "probability-map", cs);
    ras.getPixbuf().save("/tmp/subpictus-yg.png", PixbufFormat.PNG);
  }
  
  @Test
  public void anoBlueRed() throws IOException {
    ContinuousScale cs = new ContinuousScale(frameConfLS.get("continuousScale"), "Probability", null);
    cs.addColorStopRGB("0", 0.0, 0.0, 0.0, 0.0, false);
    cs.addColorStopRGB(null, 0.5, 0.5, 0.5, 0.5, false);
    cs.addColorStopRGB("0.5", 0.5, 1, 0.8, 0.8, false);
    cs.addColorStopRGB("1", 1, 0.870588235, 0.188235294, 0.152941176, false);
    cs.finish();
    RasterLayer ras = FltReader2.openFloatFile(anoFolder, "probability-map", cs);
    ras.getPixbuf().save("/tmp/subpictus-br.png", PixbufFormat.PNG);
  }
}
