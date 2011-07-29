package uk.ac.ox.map.carto.util;

import java.util.List;

import uk.ac.ox.map.domain.AdminUnit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.overlay.OverlayOp;

public class GeometryUtil {
  
  private static GeometryFactory geomFact = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue), 4326);

  /**
   * Passing either a MultiPolygon or a Polygon returns a MultiPolygon It is
   * convenient to deal with MultiPolygons.
   * 
   * @param gf
   * @param geom
   * @return
   */
  public static MultiPolygon toMultiPolygon(Geometry geom) {

    if (geom.getNumGeometries() > 1) {
      return (MultiPolygon) geom;
    } else {
      Polygon p = (Polygon) geom;
      return new MultiPolygon(new Polygon[] { p }, geomFact);
    }
  }
  
  public static MultiPolygon createMask(Envelope env, List<AdminUnit> adminUnits) {
    
    Geometry mask = getPolygonFromEnvelope(env);
    
    for (AdminUnit adminUnit : adminUnits) {
	    OverlayOp ol = new OverlayOp(mask, adminUnit.getAdminGeom().getGeom());
	    mask = ol.getResultGeometry(OverlayOp.DIFFERENCE);
    }

    return GeometryUtil.toMultiPolygon(mask);
  }
  
  public static MultiPolygon clip(Envelope env, MultiPolygon mp) {
    Geometry mask = getPolygonFromEnvelope(env);
    OverlayOp ol = new OverlayOp(mask, mp);
	  Geometry geom = ol.getResultGeometry(OverlayOp.INTERSECTION);
	  if (geom.isEmpty()) {
	    return null;
	  }
	  return toMultiPolygon(geom);
  }

  public static MultiPolygon getPolygonFromEnvelope(Envelope env) {
    Coordinate[] coords = new Coordinate[5];
    coords[0] = new Coordinate(env.getMinX(), env.getMinY());
    coords[1] = new Coordinate(env.getMinX(), env.getMaxY());
    coords[2] = new Coordinate(env.getMaxX(), env.getMaxY());
    coords[3] = new Coordinate(env.getMaxX(), env.getMinY());
    coords[4] = new Coordinate(env.getMinX(), env.getMinY());
    CoordinateArraySequence cas = new CoordinateArraySequence(coords);
    
    LinearRing lr = new LinearRing(cas, geomFact);
    MultiPolygon mask = toMultiPolygon(new Polygon(lr, null, geomFact));
    return mask;
  }

}
