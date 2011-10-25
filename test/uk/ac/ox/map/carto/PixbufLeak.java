package uk.ac.ox.map.carto;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import javax.imageio.ImageIO;

import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;
import org.junit.Test;

public class PixbufLeak {

  @Test
  public void itLeaks() throws IOException, InterruptedException {
    Gtk.init(null);
    
    long memFreeBefore = Runtime.getRuntime().freeMemory();

    for (int i = 0; i < 5000; i++) {
      System.out.println(i);
//      MemoryUsage memUse = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
//      System.out.println(memUse.getUsed());

      BufferedImage b = ImageIO.read(new File("/tmp/x.png"));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ImageIO.write(b, "PNG", bos);
      byte[] arr = bos.toByteArray();

      Pixbuf pb = new Pixbuf(arr);

    }
    
    while (Gtk.eventsPending()) {
      Gtk.mainIterationDo(false);
    }
    
    System.gc();
    Thread.sleep(10000);

    long memFreeAfter = Runtime.getRuntime().freeMemory();
    System.out.println(
    String.format("Free JVM memory: %,d", memFreeAfter)
        );
    System.out.println("Used: " + (memFreeBefore - memFreeAfter));

  }
}
