package uk.ac.ox.map.carto.style;


public enum Palette {
	WATER ("#bee8ff"),
	WHITE ("#ffffff"), 
	GREY_20 ("#cccccc"), 
	GREY_30 ("#b2b2b2"), 
	GREY_40 ("#9c9c9c"), 
	GREY_50 ("#828282"), 
	GREY_60 ("#6e6e6e"), 
	GREY_70 ("#4e4e4e"), 
	BLACK ("#000000"),
	ROSE ("#FFBEBE"),
	LIME_MED ("#CDF57A"),
	FERN ("#89CD66"),
	YELLOW ("#ffff00"),
	GRID ("#0066cc");
	
	private String hexColour;
	Palette(String hexColour){
		this.hexColour = hexColour;
	}
	
	public Colour get() {
		return new Colour(hexColour, 1);
    }
	
}
