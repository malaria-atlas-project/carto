package uk.ac.ox.map.carto.style;


public enum Palette {
	WATER ("#bee8ff"),
	BLACK ("#000000"),
	WHITE ("#ffffff"), 
	GREY ("#cccccc"), 
	GRID ("#0066cc");
	
	private String hexColour;
	Palette(String hexColour){
		this.hexColour = hexColour;
	}
	
	public Colour get() {
		return new Colour(hexColour, 1);
    }
	
}
