package uk.ac.ox.map.carto.feature;

import uk.ac.ox.map.carto.canvas.style.Colour;

public class Feature<T> {
		private Colour colour;
		private T geometry;

		public Feature(T g, Colour c) {
			geometry = g;
			colour = c;
		}

		public void setColour(Colour colour) {
			this.colour = colour;
		}

		public Colour getColour() {
			return colour;
		}

		public void setGeometry(T geometry) {
			this.geometry = geometry;
		}

		public T getGeometry() {
			return (T) geometry;
		}
}
