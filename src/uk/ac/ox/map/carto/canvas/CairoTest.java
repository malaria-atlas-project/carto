package uk.ac.ox.map.carto.canvas;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.datanucleus.plugin.EclipsePluginRegistry;
import org.freedesktop.bindings.FIXME;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Gtk;

import uk.ac.ox.map.carto.canvas.style.Colour;
import uk.ac.ox.map.carto.feature.Feature;
import uk.ac.ox.map.carto.server.AdminUnit;
import uk.ac.ox.map.carto.server.AdminUnitRisk;
import uk.ac.ox.map.carto.server.AdminUnitService;
import uk.ac.ox.map.carto.server.Country;
import uk.ac.ox.map.carto.server.Exclusion;
import uk.ac.ox.map.carto.server.FeatureLayer;
import uk.ac.ox.map.carto.text.MapTextResource;
import uk.ac.ox.map.carto.util.EnvelopeFactory;
import uk.ac.ox.map.carto.util.StringUtil;
import uk.ac.ox.map.carto.util.SystemUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
        
public class CairoTest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Gtk.init(null);
		AdminUnitService adminUnitService = new AdminUnitService();
		
		/*
		String countryId = "VUT";
		Country country  = adminUnitService.getCountry(countryId);
		drawMap(adminUnitService, country, "pv");
		drawMap(adminUnitService, country, "pf");
		
		*/
		drawMap(adminUnitService, "pf");
		drawMap(adminUnitService, "pv");
	}

	private static void 
	drawMap(AdminUnitService adminUnitService, String parasite) 
	throws IOException, InterruptedException {
		ArrayList<Country> pfCountries = adminUnitService.getCountries(parasite);
		for (Country country : pfCountries) {
	        System.out.println("Processing:" + country.getId());
			drawMap(adminUnitService, country, parasite);
		}
	}
		
	public static void drawMap(AdminUnitService adminUnitService, Country country, String parasite) throws IOException, InterruptedException {
		
		/*
		 * Get country then use extent to: 
		 * 1. Create map canvas
		 * 2. Get appropriate admin units
		 */
		
		/*
		 * Get country envelope
		 * Get admin units (level 0) overlapping data frame and draw them
		 */
		Polygon poly = (Polygon) country.getEnvelope();
		Envelope env = EnvelopeFactory.envelopeFromPolygon(poly, 1.05);
		
        ArrayList<AdminUnit> adminUnits = adminUnitService.getAdminUnits(poly);
       
        int h, w; w=460; h = 460; 
		PdfSurface dfSurface = new PdfSurface("/tmp/tmp_dataframe.pdf", w, h);
		DataFrame df= new DataFrame(dfSurface, w, h, env);
		df.setBackgroundColour("#bee8ff", 1);
        df.setLineColour("#000000", (float) 0.7);
        df.setLineWidth(0.1);
        df.setFillColour("#cccccc", (float) 0.8);
        
        for (AdminUnit admin0 : adminUnits) {
        	df.drawMultiPolygon((MultiPolygon) admin0.getGeom());
		}
        
        /*
         * Draw the risk units
         */
        HashMap<Integer, Colour> colours = new HashMap<Integer, Colour>();
        colours.put(0, new Colour("#ffffff", 1));
        colours.put(1, new Colour("#ffbebe", 1));
        colours.put(2, new Colour("#cd6666", 1));
        colours.put(9, new Colour("#ffff00", 1));
        
        /*
         * TODO: possible to obtain a nice feature class, colours included, direct from the hibernate service?
         */
        
        /*
         * Get risk admin units
         */
		FeatureLayer<MultiPolygon> pfFeats = new FeatureLayer<MultiPolygon>();
        List<AdminUnitRisk> pfUnits = adminUnitService.getRiskAdminUnits(country, parasite);
        for (AdminUnitRisk adminUnitRisk : pfUnits) {
        	pfFeats.addFeature((MultiPolygon) adminUnitRisk.getGeom(), colours.get(adminUnitRisk.getRisk()));
		}
        df.drawFeatures(pfFeats);

        /*
         * Get excluded cities
         */
		FeatureLayer<MultiPolygon> excl = new FeatureLayer<MultiPolygon>();
        List<Exclusion> ithgExcl = adminUnitService.getExclusions(country);
        Colour exclColour = new Colour("#000000", 1);
        List<String> exclCities = new ArrayList<String>();
        List<String> exclIslands = new ArrayList<String>();
        for (Exclusion exclusion : ithgExcl) {
        	excl.addFeature((MultiPolygon) exclusion.getGeom(), exclColour);
        	if (exclusion.getExclusionType().compareTo("urban area")==0)
	        	exclCities.add(exclusion.getName());
        	else if (exclusion.getExclusionType().compareTo("island")==0)
	        	exclIslands.add(exclusion.getName());
		}
        df.drawFeatures(excl);
        
        /*
         * Draw the rest of the canvas
         */
        w=500; h = 707; 
		PdfSurface mapSurface = new PdfSurface("/tmp/tmp_mapsurface.pdf", w, h);
		MapCanvas mapCanvas = new MapCanvas(mapSurface, w, h);
		mapCanvas.drawDataFrame(df, 20, 40);
        dfSurface.finish();
		
		Rectangle frame = new Rectangle(20,530,430,20);
		mapCanvas.setScaleBar(frame, df.getScale(), 7);
		mapCanvas.drawMapBorders();
		mapCanvas.setColour("#0066CC", 1);
		mapCanvas.drawMapGrids(4.5);
		
		/*
		 * Draw the legend
		 * TODO: combine this and the styling in a layer
		 */
		
		List<LegendItem> legend = new ArrayList<LegendItem>();
		legend.add(new LegendItem("Malaria free", colours.get(0)));
		
		//TODO - hacks!
		if (parasite.compareTo("pf")==0)
			legend.add(new LegendItem("<i>Pf</i>API &lt; 0.1‰", colours.get(1)));
		else
			legend.add(new LegendItem("<i>Pv</i>API &lt; 0.1‰", colours.get(1)));
			
		if (parasite.compareTo("pf")==0)
			legend.add(new LegendItem("<i>Pf</i>API ≥ 0.1‰", colours.get(2)));
		else
			legend.add(new LegendItem("<i>Pv</i>API ≥ 0.1‰", colours.get(2)));
		
		legend.add(new LegendItem("ITHG exclusions", exclColour));
		legend.add(new LegendItem("No data", colours.get(9)));
		
		Rectangle legendFrame = new Rectangle(390, 555, 150, 200);
		mapCanvas.drawLegend(legendFrame, legend);
		
		
		/*
		 * Text stuff
		 * TODO: factor all this string building out?
		 */
		MapTextResource mtr = new MapTextResource();
		Rectangle titleFrame = new Rectangle(0, 5, 500, 0);
		
		String mapTitle = null;
		if (parasite.compareTo("pf")==0)
			mapTitle = String.format((String) mtr.getObject("pfTitle"), country.getName());
		else if (parasite.compareTo("pv")==0)
			mapTitle = String.format((String) mtr.getObject("pvTitle"), country.getName());
		
		mapCanvas.setTitle(
			mapTitle,
			titleFrame,
			11);
		
		List<String> mapTextItems = new ArrayList<String>();
		
		List<Integer> years = adminUnitService.getYears(country, parasite);
		String yearsText = StringUtil.getReadableList(years);
		mapTextItems.add(String.format(
				(String) mtr.getObject("apiText"), 
				adminUnitService.getAdminUnitCount(country, parasite),
				adminUnitService.getAdminLevel(country, parasite),
				yearsText 
		));
		
		List<String> zeroed = adminUnitService.getZeroed(country, parasite);
		
		if (zeroed.size() > 0 || exclCities.size() > 0 || exclIslands.size() > 0) {
			String ithgText = (String) mtr.getObject("ithgText");
			List<String> txt = new ArrayList<String>();
			if (zeroed.size() > 0) {
				txt.add(String.format((String) mtr.getObject("adminText"), StringUtil.getReadableList(zeroed)));
			}
			if (exclCities.size() > 0) {
				txt.add(String.format((String) mtr.getObject("citiesText"), StringUtil.getReadableList(exclCities)));
			}
			if (exclIslands.size() > 0) {
				txt.add(String.format((String) mtr.getObject("islandsText"), StringUtil.getReadableList(exclIslands)));
			}
			mapTextItems.add(String.format(ithgText, StringUtil.getReadableList(txt)));
		}
			
		System.out.println(exclIslands);
		System.out.println(exclCities);
		System.out.println(zeroed);
		
		mapTextItems.add((String) mtr.getObject("copyright"));
		
		Rectangle mapTextFrame = new Rectangle(50, 555, 320, 0);
		mapCanvas.drawTextFrame(mapTextItems, mapTextFrame, 6);
		
		/*
		 * Finally put on the logo
		
		Dataset x = gdal.Open("/home/will/map1_public/maps/map_overlay3.png");
		Band band = x.GetRasterBand(1);

		int xsize = x.getRasterXSize();
		int ysize = x.getRasterYSize();

	   ByteBuffer y = band.ReadRaster_Direct(0, 0, xsize, ysize, gdalconst.GDT_Byte);
	   byte[] bytes = new byte[y.remaining()];
	   y.get(bytes);
	   
		Pixbuf pb = new Pixbuf(bytes);
		Rectangle logoFrame = new Rectangle(300, 400, 250, 0);
		mapCanvas.setLogo(pb, logoFrame);
		gdal.AllRegister();
		
		 */
		
		/*
		 * Understand this better! Does data frame require finishing? Does the data frame even need a pdf surface?
		 */
		mapSurface.finish();
		SystemUtil.addBranding(country.getId(), parasite);
//		SystemUtil.convertToPng(country.getId()+"_pf");

	}

}
