package uk.ac.ox.map.carto.canvas.style;

public enum Palette {
	WATER ("#bee8ff"),
	BLACK ("#000000"),
	WHITE ("#ffffff");
	
	private String hexColour;
	Palette(String hexColour){
		this.hexColour = hexColour;
	}
	
	public String get() {
		return hexColour;
    }
	
}
