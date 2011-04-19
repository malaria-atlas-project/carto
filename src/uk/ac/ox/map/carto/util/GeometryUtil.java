package uk.ac.ox.map.carto.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryUtil {
  
  public static MultiPolygon toMultiPolygon(GeometryFactory gf, Geometry geom) {
  	
  	if (geom.getNumGeometries() > 1) {
  		return (MultiPolygon) geom;
  	} else {
  		Polygon p = (Polygon) geom;
  		return new MultiPolygon(new Polygon[]{p}, gf);
  	}
    
  }
}
