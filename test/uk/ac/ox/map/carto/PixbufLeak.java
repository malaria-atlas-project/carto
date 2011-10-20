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

  class DestroyablePixbuf extends Pixbuf {
    public DestroyablePixbuf(byte[] data) throws IOException {
      super(data);
    }

    @Override
    protected void finalize() {
      System.out.println("finalizing Pixbuf");
      super.finalize();
    }

    public void destroy() {
      System.out.println("destroying");
      finalize();
    }

  }

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

      //comment out and memory usage remains steady
      DestroyablePixbuf dpb = new DestroyablePixbuf(arr);

      dpb.destroy();

    }
    
    System.gc();
    Thread.sleep(10000);

    long memFreeAfter = Runtime.getRuntime().freeMemory();
    System.out.println("Used: " + (memFreeBefore - memFreeAfter));

  }
}
