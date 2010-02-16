package uk.ac.ox.map.carto.canvas;


import java.util.List;

import uk.ac.ox.map.carto.canvas.style.Colour;

/*
 * The legend is responsible for drawing
 *
 */
    
public class LegendItem {
        public final String description;
        public final Colour colour;
        public LegendItem(String description, Colour colour) {
            this.description = description;
            this.colour = colour;
        }
    
}