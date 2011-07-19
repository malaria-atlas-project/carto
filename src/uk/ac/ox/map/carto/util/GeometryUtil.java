package uk.ac.ox.map.carto.util;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryUtil {

  /**
   * Passing either a MultiPolygon or a Polygon returns a MultiPolygon It is
   * convenient to deal with MultiPolygons.
   * 
   * @param gf
   * @param geom
   * @return
   */
  public static MultiPolygon toMultiPolygon(GeometryFactory gf, Geometry geom) {

    if (geom.getNumGeometries() > 1) {
      return (MultiPolygon) geom;
    } else {
      Polygon p = (Polygon) geom;
      return new MultiPolygon(new Polygon[] { p }, gf);
    }
  }

}
