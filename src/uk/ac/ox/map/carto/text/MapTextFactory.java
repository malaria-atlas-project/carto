package uk.ac.ox.map.carto.text;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MapTextFactory {
  
  private static final Configuration cfg = new Configuration();
  
  public String processTemplate(Map<String, Object> root, String template) throws IOException, TemplateException {
    Template temp = cfg.getTemplate(template);
    Writer out = new StringWriter();
    temp.process(root, out);
    return out.toString();
  }
}
