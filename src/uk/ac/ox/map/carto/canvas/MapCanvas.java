package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.pango.Alignment;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.map.carto.util.AnnotationFactory;

import com.vividsolutions.jts.geom.Envelope;

public class MapCanvas extends BaseCanvas {

	private final List<DataFrame> dataFrames = new ArrayList<DataFrame>();
	final FontDescription fontDesc;
	static final Logger logger = LoggerFactory.getLogger(MapCanvas.class);

	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf, width, height);

		/*
		 * Sensible defaults for font options.
		 */
		fontDesc = new FontDescription();
		fontDesc.setFamily("Helvetica");
		fontDesc.setSize(12);
	}

	MapCanvas outer() {
		return this;
	}

	public void setTitle(String text, Rectangle frame, Integer fontSize) {
		/*
		 * TODO look at Reportlab api to think of good ways of abstracting
		 */
		Layout layout = new Layout(cr);
		fontDesc.setSize(fontSize);
		layout.setFontDescription(fontDesc);
		layout.setWidth(frame.width);
		layout.setAlignment(Alignment.CENTER);
		layout.setMarkup(text);

		// Always default to black. Good idea?
		cr.setSource(0.0, 0.0, 0.0);
		cr.moveTo(frame.x, frame.y);
		cr.showLayout(layout);
	}

	public void drawTextFrame(List<String> text, Rectangle frame, float fontSize) {
		Layout layout = new Layout(cr);
		fontDesc.setSize(fontSize);
		layout.setFontDescription(fontDesc);
		layout.setWidth(frame.width);
		layout.setJustify(true);

		cr.setSource(0.0, 0.0, 0.0);

		double y = frame.y;

		for (String string : text) {
			cr.moveTo(frame.x, y);
			layout.setMarkup(string);
			cr.showLayout(layout);
			y += layout.getPixelHeight() + 10;
		}
	}

	public void annotateMap(List<String> anno, double x, double y, AnchorX ax, AnchorY ay) {
		for (String string : anno) {
			annotateMap(string, x, y, ax, ay);
		}
	}

	public void annotateMap(String text, double x, double y, AnchorX ax, AnchorY ay) {

		Layout layout = new Layout(cr);
		layout.setFontDescription(fontDesc);
		layout.setMarkup(text);

		cr.moveTo(ax.eval(layout.getPixelWidth(), x), ay.eval(layout.getPixelHeight(), y));
		cr.showLayout(layout);
	}

	/**
	 * Draws already stored dataframes, grids and associated stuff.
	 */
	public void drawDataFrames() {
		for (DataFrame df : dataFrames) {
			Point2D.Double origin = df.getOrigin();
			cr.setSource(df.getSurface(), origin.x, origin.y);
			cr.paint();
			
			setColour("#000000", 1);
			cr.setLineWidth(0.2);
			cr.rectangle(origin.x, origin.y, df.getWidth(), df.getHeight());
			cr.stroke();
			df.finish();
			if (df.hasGrid())
				drawMapGrid(4.5, df);
		}
	}

	public void drawLegend(Rectangle rect, List<LegendItem> legend) {
		double y = rect.y;
		double x = rect.x;

		/*
		 * Could be configuration options?
		 */
		double patchWidth = 25;
		double patchHeight = 12.5;
		double spacing = 22;
		double textMargin = 40;

		fontDesc.setSize(6);
		for (LegendItem li : legend) {
			setFillColour(li.colour);
			cr.rectangle(x, y, patchWidth, patchHeight);
			cr.fillPreserve();
			setLineColour();
			if (li.hatched) {
				// don't clip preserve
				cr.save();
				cr.clipPreserve();
				paintCrossHatch();
				cr.restore();
			} else if (li.stippled) {
				// don't clip preserve
				cr.save();
				cr.clipPreserve();
				paintStipple();
				cr.restore();
			}
			setLineColour();
			cr.setDash(new double[] { 1, 0 });
			cr.stroke();

			annotateMap(li.description, x + textMargin, y + (patchHeight / 2), AnchorX.L, AnchorY.C);
			y += spacing;
		}
	}

	private void drawMapGrid(double fontSize, DataFrame df) {
		/*
		 * TODO fix hacking - hardcoding, over complexity; Pass in interval??
		 * More of a callers job really.
		 */
		int intervals[] = { 40, 20, 10, 5, 2, 1 };
		fontDesc.setSize(fontSize);
		setColour("#0066CC", 1);
		cr.setLineWidth(0.2);

		// only draw grids for dataframes
		Envelope env = df.getEnvelope();
		Point2D.Double origin = df.getOrigin();

		int chosen_interval = 0;
		HashMap<Integer, Integer> possible_intervals = new HashMap<Integer, Integer>();

		for (int i = 0; i < intervals.length; i++) {
			/*
			 * Floor the max and ceiling the min to get the position of the
			 * lower and upper grid lines Same logic for both axes
			 * 
			 * TODO: all very brittle, no checking, hardcoding, ... in fact not
			 * quite sure how it works.
			 */
			int I = intervals[i];
			int nintervalX = (int) (Math.floor(env.getMaxX() / I) - Math.ceil(env.getMinX() / I));
			int nintervalY = (int) (Math.floor(env.getMaxY() / I) - Math.ceil(env.getMinY() / I));
			int n = Math.min(nintervalX, nintervalY);
			if (n > 1 && n < 7) {
				possible_intervals.put(n, I);
			}
		}
		// This unreadable stuff gets the value with the minimum key
		try {
			chosen_interval = possible_intervals.get(Collections.min(possible_intervals.keySet()));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			chosen_interval = 1;
		}

		System.out.println("chosen interval: " + chosen_interval);

		double x;
		double x1;
		double x2;
		double y;
		double y1;
		double y2;
		int tic_length = 3;
		String anno;
		for (int i = -180; i <= 180; i += chosen_interval) {
			// Dual purpose: does a meridian and parallel
			Point2D.Double mapPt = df.transform(i, i);
			if (i > env.getMinX() && i < env.getMaxX()) {
				x = origin.x + mapPt.x;
				y1 = origin.y - tic_length;
				y2 = origin.y + df.getHeight() + tic_length;

				System.out.println("x: " + x);
				System.out.println("y1: " + y1);
				System.out.println("y2: " + y2);
				// meridian
				cr.moveTo(x, y1);
				cr.lineTo(x, y2);
				cr.stroke();
				anno = AnnotationFactory.gridText(i, "EW");
				annotateMap(anno, x, y1, AnchorX.C, AnchorY.B);
				annotateMap(anno, x, y2, AnchorX.C, AnchorY.T);
			}

			if (i > env.getMinY() && i < env.getMaxY()) {
				y = origin.y + mapPt.y;
				x1 = origin.x - tic_length;
				x2 = origin.x + df.getWidth() + tic_length;

				// meridian
				cr.moveTo(x1, y);
				cr.lineTo(x2, y);
				cr.stroke();
				anno = AnnotationFactory.gridText(i, "SN");
				annotateMap(anno, x1, y, AnchorX.R, AnchorY.C);
				annotateMap(anno, x2, y, AnchorX.L, AnchorY.C);
			}

		}

	}

	public void setScaleBar(Rectangle frame, double scale, int fontSize) {
		ScaleBar sb = new ScaleBar(this, frame, scale, fontSize);
	}

	/*
	 * TODO prettify
	 */
	public enum AnchorX {
		L, C, R;
		double eval(int w, double x) {
			switch (this) {
			case L:
				return x;
			case C:
				return x - (w / 2);
			case R:
				return x - w;
			}
			throw new AssertionError("Unknown op: " + this);
		}
	}

	public enum AnchorY {
		T, C, B;
		double eval(int h, double y) {
			switch (this) {
			case T:
				return y;
			case C:
				return y - (h / 2);
			case B:
				return y - h;
			}
			throw new AssertionError("Unknown op: " + this);
		}
	}

	public void addDataFrame(DataFrame df) {
		dataFrames.add(df);
	}
}
