package uk.ac.ox.map.carto.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.vividsolutions.jts.geom.MultiPolygon;

import uk.ac.ox.map.carto.canvas.style.Colour;

public class PolygonCursor<T extends RiskMap> {
	private final HashMap<Integer, Colour> colours;
	private final ArrayList<T> features;
	public PolygonCursor(ArrayList<T> features, HashMap<Integer, Colour> colours) {
		this.features = features;
		this.colours = colours;
	}

	public Feature getFeature(int i) {
		Feature feat = new Feature();
		T au = features.get(i);
		feat.c = colours.get(au.getRisk());
		feat.mp = (MultiPolygon) au.getGeom();
		return feat;
	}
	
	public Integer size(){
		return features.size();
	}
	
}
