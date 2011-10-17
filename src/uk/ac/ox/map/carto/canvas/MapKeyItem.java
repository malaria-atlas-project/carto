package uk.ac.ox.map.carto.canvas;

import uk.ac.ox.map.carto.style.FillStyle;

public class MapKeyItem {

	public final String description;
	public boolean point = false;
  public final FillStyle fillStyle;

	public MapKeyItem(String description, FillStyle fs) {
		this.description = description;
		this.fillStyle = fs;
	}
}