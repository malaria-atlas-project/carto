package uk.ac.ox.map.carto.layer;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.map.base.model.adminunit.HasGeometry;
import uk.ac.ox.map.carto.style.FillStyle;
import uk.ac.ox.map.carto.style.LineStyle;
import uk.ac.ox.map.carto.style.PolygonSymbolizer;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * 
 * @author will
 * Currently takes a list of objects that have geometries, the colours and builds a list of PolygonSymbolizers from these.
 *
 */
public class LayerFactory {
	
		public <T extends HasGeometry> List<PolygonSymbolizer> getPolygonLayer(List<T> polyList, FillStyle fs, LineStyle ls) {
			
			List<PolygonSymbolizer> newLayer = new ArrayList<PolygonSymbolizer>();
			
			for (T hasGeom : polyList) {
				PolygonSymbolizer ps = new PolygonSymbolizer((MultiPolygon) hasGeom.getGeom(), fs, ls);
				newLayer.add(ps);
			}
			return newLayer;
		}
	
}
