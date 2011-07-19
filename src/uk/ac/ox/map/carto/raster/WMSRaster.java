package uk.ac.ox.map.carto.raster;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.imageio.RasterLayer;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import com.vividsolutions.jts.geom.Envelope;

public class WMSRaster implements RasterLayer {
  Logger logger = LoggerFactory.getLogger(WMSRaster.class);
  private final String wmsUrl;
  private Envelope env;
  private double cellSize;
  private int width;
  private int height;

  public WMSRaster(Envelope env, double cellSize, String url) {
    /*
     * Envelope resized already.
     */
    this.env = env;
    this.cellSize = cellSize;
    this.width = (int) Math.ceil(env.getWidth() / cellSize);
    this.height = (int) Math.ceil(env.getHeight() / cellSize);
    this.wmsUrl = url;
  }

  @Override
  public double getCellSize() {
    return cellSize;
  }

  @Override
  public Point2D.Double getOrigin() {
    return new Point2D.Double(env.getMinX(), env.getMaxY());
  }

  @Override
  public ByteOutputStream getImageOutputStream() {

    String bbox = "&BBOX=%s,%s,%s,%s";
    bbox = String.format(bbox, env.getMinX(), env.getMinY(), env.getMaxX(), env .getMaxY());
    
    StringBuilder url = new StringBuilder();
    url.append(wmsUrl);
    url.append(bbox);
    url.append("&HEIGHT=").append(height);
    url.append("&WIDTH=").append(width);

    try {
      URL u = new URL(url.toString());
      BufferedImage i = ImageIO.read(u);
      ByteOutputStream bos = new ByteOutputStream();
      ImageIO.write(i, "PNG", bos);
      return bos;

    } catch (IOException e) {
      logger.error(e.getMessage());
      return null;
    }
  }

}
