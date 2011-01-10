package uk.ac.ox.map.carto.style;

import uk.ac.ox.map.deps.Colour;


public enum Palette {
	WATER ("#bee8ff"),
	WHITE ("#ffffff"), 
	GREY_10 ("#e1e1e1"), 
	GREY_20 ("#cccccc"), 
	GREY_30 ("#b2b2b2"), 
	GREY_40 ("#9c9c9c"), 
	GREY_50 ("#828282"), 
	GREY_60 ("#6e6e6e"), 
	GREY_70 ("#4e4e4e"), 
	BLACK ("#000000"),
	ROSE ("#FFBEBE"),
	CORAL ("#cd6666"),
	LIME_MED ("#CDF57A"),
	FERN ("#89CD66"),
	YELLOW ("#ffff00"),
	ORANGE_DARK ("#FF8C00"),
	GRID ("#0066cc"), 
	ULTRA_BLUE ("#004DA8"),
	MACAW_GREEN ("#98E600"),
	AUTUNITE_YELLOW ("#FFFF73")
	; 
	
	
  private String hexColour;

  Palette(String hexColour) {
    this.hexColour = hexColour;
  }

  public Colour get() {
    return new Colour(hexColour, 1);
  }

  public Colour get(double a) {
    return new Colour(hexColour, a);
  }
	
}
