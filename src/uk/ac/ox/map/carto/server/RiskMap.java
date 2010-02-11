package uk.ac.ox.map.carto.server;

import com.vividsolutions.jts.geom.Geometry;

public interface RiskMap {
	Integer getRisk();
	Geometry getGeom();
}
