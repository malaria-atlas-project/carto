package uk.ac.ox.map.carto.server;

import java.util.ArrayList;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.feature.Feature;
import uk.ac.ox.map.carto.util.ForwardingList;

/*
 * Wrapper class. Made generic for future extension to linestring. 
 */
public class FeatureLayer<T> extends ForwardingList<Feature<T>> {
	
	public FeatureLayer() {
		super(new ArrayList<Feature<T>>());
	}

	public void addFeature(T g, Colour c){
		Feature<T> feat = new Feature<T>(g,c);
		super.add(feat);
	}
	
}
