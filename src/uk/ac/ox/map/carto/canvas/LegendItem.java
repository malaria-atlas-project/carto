package uk.ac.ox.map.carto.canvas;


import uk.ac.ox.map.carto.canvas.style.Colour;

/*
 * 
 *
 */
    
public class LegendItem {
        public final String description;
        public final Colour colour;
        public boolean hatched = false;
		public boolean stippled = false;
        public LegendItem(String description, Colour colour) {
            this.description = description;
            this.colour = colour;
        }
}