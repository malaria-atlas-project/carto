package uk.ac.ox.map.carto.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.util.List;

import org.freedesktop.cairo.Filter;
import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.Surface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.IsFillLayer;
import uk.ac.ox.map.carto.style.Palette;
import uk.ac.ox.map.domain.carto.Colour;
import uk.ac.ox.map.imageio.RasterLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author will
 * 
 *         Adds polygon and raster drawing facilities to BaseCanvas.
 * 
 */
public class DataFrame extends BaseCanvas {

  private final Logger logger = LoggerFactory.getLogger(DataFrame.class);
  private Envelope env;
  private double scale;
  private final boolean hasGrid;
  private final AffineTransform transform = new AffineTransform();
  private final Point2D.Double origin;
  private final boolean hasBorder;
  private final Colour borderColour;
  private final Colour gridColour;

  public static class Builder {
    // required params
    private Envelope env;
    private Rectangle rect;
    private String fileName;

    // optional params
    private boolean hasGrid = true;
    private boolean hasBorder = true;
    private Colour backgroundColour = Palette.WHITE.get();
    private Colour borderColour = Palette.BLACK.get();
    private Colour gridColour = Palette.GRID.get();

    public Builder(Envelope dataEnv, Rectangle rect, String fileName) {
      this.env = dataEnv;
      this.rect = rect;
      this.fileName = fileName;
    }

    public Builder hasGrid(boolean hasGrid) {
      this.hasGrid = hasGrid;
      return this;
    }

    public Builder hasBorder(boolean hasBorder) {
      this.hasBorder = hasBorder;
      return this;
    }

    public Builder backgroundColour(Colour colour) {
      this.backgroundColour = colour;
      return this;
    }

    public DataFrame build() throws IOException {
      return new DataFrame(this);
    }

    public Builder borderColour(Colour borderColour) {
      this.borderColour = borderColour;
      return this;
    }

    public Builder gridColour(Colour gridColour) {
      this.gridColour = gridColour;
      return this;
    }
  }

  public DataFrame(Builder builder) throws IOException {

    super(new PdfSurface(builder.fileName, builder.rect.width,
        builder.rect.height), builder.rect.width, builder.rect.height);

    setEnvelope(builder.rect.width, builder.rect.height, builder.env);
    this.origin = builder.rect.getUpperLeft();

    setBackgroundColour(builder.backgroundColour);

    cr.setLineWidth(0.2);
    cr.setSource(0.0, 1.0, 0.0, 1.0);
    this.hasGrid = builder.hasGrid;
    this.hasBorder = builder.hasBorder;
    this.borderColour = builder.borderColour;
    this.gridColour = builder.gridColour;
  }

  /*
   * TODO: remove and put in pointsymbolizer
   */
  public void drawPoint(double x, double y, FillStyle fs) {

    Point2D.Double pt = new Point2D.Double(x, y);
    transform.transform(pt, pt);

    drawPoint(fs, pt);
  }

  public void addRasterLayer(RasterLayer ras) throws IOException {
    {
      cr.save();

      Pixbuf pb;
      pb = new Pixbuf(ras.getImageData());

      Point2D.Double pt = ras.getOrigin();

      logger.debug("Pixbuf width: {}", pb.getWidth());
      logger.debug("Pixbuf height: {}", pb.getHeight());
      logger.debug("Orig x: {}", pt.x);
      logger.debug("Orig y: {}", pt.y);

      transform.transform(pt, pt);
      double cellSize = ras.getCellSize();
      logger.debug("Cellsize: {}", cellSize);

      /*
       * Determine the factor by which to scale the canvas to draw the image.
       * NB: cellSize and scale are the inverse of one another cellSize is dd /
       * pix scale is pix / dd
       */
      double newScale = scale * cellSize;

      cr.scale(newScale, newScale);
      /*
       * The scaling will move the origin, therefore this needs to be corrected.
       */
      cr.setSource(pb, pt.x / newScale, pt.y / newScale);
      cr.getSource().setFilter(Filter.NEAREST);
      cr.paint();

      cr.restore();
    }
    
    while (Gtk.eventsPending()) {
      Gtk.mainIterationDo(false);
    }
  }

  private void setEnvelope(double width, double height, Envelope dataEnv) {
    double dX = dataEnv.getWidth();
    double dY = dataEnv.getHeight();

    double arEnv = dX / dY;
    double arCanvas = width / height;

    /*
     * Fix the scale
     */
    dX = dataEnv.getWidth();
    dY = dataEnv.getHeight();
    double xScale = width / dX;
    double yScale = height / dY;
    this.scale = (xScale < yScale) ? xScale : yScale;

    /*
     * Expand env to fit whole canvas
     */
    Envelope canvasEnv = new Envelope();
    if (arCanvas > arEnv) {
      // canvas is fatter than data- expand env in x direction
      // ll
      double dW = (width / scale) - dX;
      canvasEnv.expandToInclude(new Coordinate(dataEnv.getMinX() - (dW / 2),
          dataEnv.getMinY()));
      // ur
      canvasEnv.expandToInclude(new Coordinate(dataEnv.getMaxX() + (dW / 2),
          dataEnv.getMaxY()));
    } else if (arCanvas < arEnv) {
      double dH = (height / scale) - dY;
      // canvas is thinner than data- expand env in y direction
      // ll
      canvasEnv.expandToInclude(new Coordinate(dataEnv.getMinX(), dataEnv
          .getMinY()
          - (dH / 2)));
      // ur
      canvasEnv.expandToInclude(new Coordinate(dataEnv.getMaxX(), dataEnv
          .getMaxY()
          + (dH / 2)));
    } else {
      // aspect ratios are the same
      canvasEnv = dataEnv;
    }

    /*
     * Build transform. Negative Y scale translates between +y geographical and
     * -y cairo coordinates.
     */
    this.env = canvasEnv;
    transform.scale(this.scale, -this.scale);
    transform.translate(-canvasEnv.getMinX(), -canvasEnv.getMaxY());

    /*
     * Print debugging info
     */
    logger.debug("X Scale: {}", transform.getTranslateX());
    logger.debug("Y Scale: {}", transform.getTranslateY());

    Double urPt = new Point2D.Double(env.getMaxX(), env.getMaxY());
    logger.debug("UR env before transform: {}", urPt);
    transform.transform(urPt, urPt);
    logger.debug("UR env after transform: {}", urPt);

    Double llPt = new Point2D.Double(env.getMinX(), env.getMinY());
    transform.transform(llPt, llPt);
    logger.debug("LL env before transform: {}", llPt);
    transform.transform(urPt, urPt);
    logger.debug("LL env after transform: {}", llPt);

  }

  /**
   * @return the envelope represented by the dataframe, calculated from the
   *         envelope passed in.
   */
  public Envelope getEnvelope() {
    return this.env;
  }

  public void drawFeatures(List<MultiPolygon> ls, FillStyle fs) {
    for (MultiPolygon ps : ls) {
      drawMultiPolygon(ps, fs);
    }
  }

  public void drawMultiPolygon(MultiPolygon mp, FillStyle fs) {

    for (int i = 0; i < mp.getNumGeometries(); i++) {
      drawPolygon((Polygon) mp.getGeometryN(i), fs);
    }

  }

  private void drawPolygon(Polygon p, FillStyle fs) {

    /*
     * Draw line strings
     */
    drawLineStrings(p);

    /*
     * To draw hatches, line strings are sometimes consumed.
     */

    /*
     * Filling
     */
    for (IsFillLayer layer : fs.layers) {
      boolean lineStringsConsumed = paintFill(layer);
      if (lineStringsConsumed) {
        drawLineStrings(p);
      }
    }

    /*
     * Stroking
     */
    setLineColour(fs.outline.getLineColour());
    cr.stroke();
  }

  private void drawLineStrings(Polygon p) {
    LineString exteriorRing = p.getExteriorRing();
    drawLineString(exteriorRing);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      drawLineString(p.getInteriorRingN(i));
    }
  }

  /**
   * Draws a line string onto the canvas. Stroking and filling operations happen
   * afterwards.
   * 
   * @param ls
   */
  private void drawLineString(LineString ls) {

    Coordinate[] coordinates = ls.getCoordinates();
    if (coordinates.length < 2) {
      return;
    }
    Coordinate first = coordinates[0];
    moveTo(first.x, first.y);
    for (int i = 1; i < (coordinates.length - 1); i++) {
      Coordinate c = coordinates[i];
      lineTo(c.x, c.y);
    }
    cr.closePath();
  }

  /**
   * Draws a line from the current position to the position specified in x and
   * y. TODO: transform the whole set of coordinates in a polygon instead of
   * multiple calls to transform?
   * 
   * @param x
   *          the x Coordinate in Geographic coordinates
   * @param y
   *          the y Coordinate in Geographic coordinates
   * @return void
   */
  private void lineTo(double x, double y) {
    Point2D.Double pt = new Point2D.Double(x, y);
    transform.transform(pt, pt);
    cr.lineTo(pt.getX(), pt.getY());
  }

  private void moveTo(double x, double y) {
    Point2D.Double pt = new Point2D.Double(x, y);
    transform.transform(pt, pt);
    cr.moveTo(pt.getX(), pt.getY());
  }

  public Point2D.Double transform(double x, double y) {
    Point2D.Double pt = new Point2D.Double(x, y);
    transform.transform(pt, pt);
    return pt;
  }

  public Surface getSurface() {
    return surface;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public double getScale() {
    return scale;
  }

  public boolean hasGrid() {
    return hasGrid;
  }

  public boolean hasBorder() {
    return hasBorder;
  }

  public Point2D.Double getOrigin() {
    return origin;
  }

  public Colour getBorderColour() {
    return borderColour;
  }

  public Colour getGridColour() {
    return gridColour;
  }
}
