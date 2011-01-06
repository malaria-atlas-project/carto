package uk.ac.ox.map.carto;

import java.io.File;
import java.io.IOException;

import org.gnome.gtk.Gtk;

import com.sun.java.swing.plaf.gtk.GTKColorType;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import test.FeatureRasterizer;
import uk.ac.ox.map.base.model.vector.Anopheline;
import uk.ac.ox.map.base.service.VectorService;
import uk.ac.ox.map.imageio.FltReader;
import uk.ac.ox.map.imageio.RasterDescriptor;

public class TestRasterize {
  
  static VectorService vectorService = new VectorService();
  final static String baseDir = "/home/will/map1_public/data/vector/species_mapping_outputs/model_results/Anopheles.%s_results/probability-map";

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    Gtk.init(args);
    
    Anopheline ano = vectorService.getAnophelineById(49);
    String rootPath = String.format(baseDir, ano.getAbbreviation());
    
    Geometry geom = ano.getExpertOpinion().getGeom();
		RasterDescriptor rasDesc = FltReader.getRasterDescriptor(new File(rootPath + ".hdr"), new File(rootPath + ".flt"));
		
		FeatureRasterizer fr = new FeatureRasterizer();
		fr.rasterize((MultiPolygon) geom, rasDesc);
    
  }

}
