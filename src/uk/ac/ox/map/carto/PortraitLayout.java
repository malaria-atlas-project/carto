package uk.ac.ox.map.carto;

import uk.ac.ox.map.carto.canvas.Rectangle;

public enum PortraitLayout {
  
		CANVAS (new Rectangle(0, 0, 500, 707)),
		DATAFRAME (new Rectangle(20, 40, 460, 460)),
		SCALEBAR (new Rectangle(20, 525, 430, 20)),
		LEGEND (new Rectangle(50, 545, 300, 0)),
		KEY (new Rectangle(365, 545, 170, 200)),
		CONTINUOUSSCALE (new Rectangle(390, 675, 100, 15)),
		TITLE (new Rectangle(0, 3, 500, 0));
	
  private Rectangle rect;

  PortraitLayout(Rectangle rect) {
    this.rect = rect;
  }
  public Rectangle get(){
    return rect;
    
  }

}
