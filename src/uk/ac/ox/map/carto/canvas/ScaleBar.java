package uk.ac.ox.map.carto.canvas;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScaleBar {
  
  private static final double KM_PER_DEGREE = 111.320;
  private NumberFormat formatter = new DecimalFormat("###,###,###");
  
  private final Logger logger = LoggerFactory.getLogger(ScaleBar.class);
  private final List<String> annotations = new ArrayList<String>();
  private double divisionWidth = 0;
  

  public ScaleBar(double width, double scale) {

    /*
     * Find the max space available
     */
    double maxDist = (width / scale) * KM_PER_DEGREE;
    int intervals[] = { 10000, 5000, 2500, 2000, 1000, 500, 250, 100, 50, 25, 10 };

    double nIntervals = 0;
    int interval = 0;
    
    /*
     * Iterate possible intervals which are in descending order.
     * Choose the first interval width that allows more than one segment on the scale bar.
     * This yields the scale bar with the maximum size we can fit in.
     */
    for (int i = 0; i < intervals.length; i++) {
      interval = intervals[i];
      nIntervals = maxDist / interval;
      if (nIntervals > 2) {
        this.divisionWidth = (interval / KM_PER_DEGREE) * scale;
        break;
      }
    }
    logger.debug("division width: {}", divisionWidth);
    logger.debug("intervals: {}", nIntervals);

    if (!(nIntervals >= 2)) {
      //TODO: deal with errors better
      logger.error("Not enough intervals");
    }
    
    /*
     * To ensure the interval count * width doesn't exceed available space, need to round down.
     */
    int n = (int) Math.floor(nIntervals);
    
    for (int i = 0; i < n; i++) {
      String anno = formatter.format(interval * (i + 1));
      annotations.add(anno);
    }
  }
  
  /**
   * @return the width in points of each division
   */
  public double getIntervalWidth() {
    return divisionWidth;
  }
  
  
  /**
   * Together with the annotations, the division width, the units and the frame, the client can construct a scalebar.
   * 
   * @return the scalebar division annotations
   */
  public List<String> getAnnotations() {
    return annotations;
  }
  
}