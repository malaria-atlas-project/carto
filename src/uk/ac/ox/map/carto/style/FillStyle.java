package uk.ac.ox.map.carto.style;



/**
 * @author will
 * 
 *         Incomplete
 */
public class FillStyle {
	
	private final Colour fillColor;
	private final FillType fillType;
	
	public enum FillType {
		SOLID, HATCHED, STIPPLED;
	}

	public FillStyle(Colour colour, FillType fillType) {
		this.fillColor = colour;
		this.fillType = fillType;
    }

	/**
	 * Fill type defaults to solid 
	 * @param colour
	 */
	public FillStyle(Colour colour) {
		this.fillColor = colour;
		this.fillType = FillType.SOLID;
	}

	public Colour getFillColor() {
		return fillColor;
	}


	public FillType getFillType() {
	    return fillType;
    }

}