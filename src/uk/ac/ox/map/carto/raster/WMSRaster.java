package uk.ac.ox.map.carto.raster;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;

import org.gnome.gdk.Pixbuf;
import org.postgis.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jndi.toolkit.url.UrlUtil;
import com.vividsolutions.jts.geom.Envelope;

import sun.net.util.URLUtil;
import uk.ac.ox.map.imageio.RasterLayer;

public class WMSRaster implements RasterLayer {
	Logger logger = LoggerFactory.getLogger(WMSRaster.class);
	private final String url;
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
		this.height =  (int) Math.ceil(env.getHeight() / cellSize);
		this.url = url;
		
	}
	
	@Override
	public double getCellSize() {
		return cellSize;
	}

	@Override
	public Point2D.Double getOrigin() {
		Point2D.Double ll = new Point2D.Double(env.getMinX(), env.getMinY());
		double x = ll.x; 
		double y = ll.y;
		y += height * cellSize;
		return new Point2D.Double(x,y);
	}

	@Override
	public Pixbuf getPixbuf() {
		
		String bbox = "&BBOX=%s,%s,%s,%s";
		bbox = String.format(bbox, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
		String url1 = url + bbox;
		String heightParam = "&HEIGHT=" + height;
		url1 = url1 + heightParam;
		String widthParam = "&WIDTH="+ width;
		url1 = url1 + widthParam;
		System.out.println(url1);
		
		try {
	        URL u = new URL(url1);
	        BufferedImage i = ImageIO.read(u);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(i, "PNG", bos);
			Pixbuf pb = new Pixbuf(bos.toByteArray());
			return pb;
			
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	

}
