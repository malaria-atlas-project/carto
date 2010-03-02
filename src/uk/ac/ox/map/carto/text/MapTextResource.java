package uk.ac.ox.map.carto.text;

import java.util.ListResourceBundle;

public class MapTextResource extends ListResourceBundle {
	     public Object[][] getContents() {
	         return contents;
	     }
	     static final Object[][] contents = {
	    	 {"copyright", "<b>Copyright:</b> Licensed to the Malaria Atlas Project (MAP; www.map.ox.ac.uk) under a Creative Commons Attribution 3.0 License (http://creativecommons.org/)."},
	    	 {"apiText","The health management information system data used to inform the stable, unstable and malaria free categories were available for %s administrative units at level %s for the following years: %s."},
	    	 {"pfTitle","<i>Plasmodium falciparum</i> risk in %s"},
	    	 {"pvTitle","<i>Plasmodium vivax</i> risk in %s"},
	    	 {"medintelText", "Medical intelligence was used to zero risk in %s."},
	    	 {"ithgText", "Data from international and travel health guidelines was used to zero risk in %s."},
	    	 {"adminText", "the following administrative units: %s"},
	    	 {"citiesText", "the following cities: %s"},
	    	 {"islandsText", "the following islands: %s"},
	     };
 }
