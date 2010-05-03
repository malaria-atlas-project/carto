/**
 * 
 */
package uk.ac.ox.map.carto.canvas;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.freedesktop.cairo.Context;

import uk.ac.ox.map.carto.canvas.Rectangle.Anchor;

public class ScaleBar {
	/**
     * 
     */
    private final MapCanvas mapCanvas;
	private static final double KM_PER_DEGREE = 111.320;
	private final int barHeight;
	private double offset;
	private Context cr;
	private Rectangle frame;
	private NumberFormat formatter = new DecimalFormat("###,###,###");

	private enum SegmentType {
		HOLLOW, FILLED
	}
	
	public ScaleBar(MapCanvas mapCanvas, Rectangle frame, double scale, int fontSize) {
		this.mapCanvas = mapCanvas;
		this.cr = this.mapCanvas.outer().cr;

		// TODO: hard code
		this.barHeight = 5;
		this.frame = frame;
		this.offset = frame.x;

		// Set the font size
		this.mapCanvas.outer().fontDesc.setSize(fontSize);
		cr.setLineWidth(0.2);
		cr.setSource(0, 0, 0, 1);
		/*
		 * Find the max space available
		 */
		double maxDist = (frame.width / scale) * KM_PER_DEGREE;
		int intervals[] = { 10000, 5000, 2500, 2000, 1000, 500, 250, 100, 50, 25, 10 };

		double divisionWidth = 0;
		double nIntervals = 0;
		int interval = 0;
		for (int i = 0; i < intervals.length; i++) {
			interval = intervals[i];
			nIntervals = maxDist / interval;
			if (nIntervals > 2) {
				divisionWidth = (interval / KM_PER_DEGREE) * scale;
				break;
			}
		}
		MapCanvas.logger.debug("division width: {}", divisionWidth);
		MapCanvas.logger.debug("intervals: {}", nIntervals);

		if (!(nIntervals >= 2)) {
			System.err.println("not enough intervals");
		}

		int n = (int) nIntervals;
		SegmentType st;

		this.mapCanvas.outer().annotateMap("0", offset, frame.y, Anchor.CB);

		for (int i = 0; i < n; i++) {
			if ((i % 2) == 0)
				st = SegmentType.HOLLOW;
			else
				st = SegmentType.FILLED;

			drawSegment((int) divisionWidth, st, formatter.format(interval * (i + 1)));
		}

		this.mapCanvas.outer().annotateMap("Kilometres", offset + 10, frame.y, Anchor.LT);
	}

	private void drawSegment(int width, SegmentType st, String text) {
		cr.rectangle(offset, frame.y, width, barHeight);
		if (st == SegmentType.FILLED) {
			cr.fillPreserve();
		}
		cr.stroke();
		offset += width;
		this.mapCanvas.outer().annotateMap(text, offset, frame.y, Anchor.CB);
	}
}