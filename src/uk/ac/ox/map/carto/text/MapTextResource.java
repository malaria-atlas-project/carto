package uk.ac.ox.map.carto.text;

import java.util.ListResourceBundle;

public class MapTextResource extends ListResourceBundle {
	     public Object[][] getContents() {
	         return contents;
	     }
	     static final Object[][] contents = {
	    	 {"copyright", "<b>Copyright:</b> Licensed to the Malaria Atlas Project (MAP; www.map.ox.ac.uk) under a Creative Commons Attribution 3.0 License (http://creativecommons.org/)."},
	    	 
	    	 {"apiText", 
	    		"<b>HMIS:</b> The health management information system (HMIS) data " +
	    		"used to inform the stable, unstable and malaria free categories " +
	    		"%s %s."},
	    	 {"mohText", "A personal communiation from the Ministry of Health (MOH) was used to zero risk in %s."},
	    	 {"apiTextNoInfo","No health management information system data could be accessed. Stable risk is assumed to exist throughout the country."},
	    	 {"apiTextNationalMedIntel","Medical intelligence allowed the definition of risk within the national territory as shown above."},
	    	 {"pfTitle","<i>Plasmodium falciparum</i> risk in %s"},
	    	 {"pvTitle","<i>Plasmodium vivax</i> risk in %s"},
	    	 {"medintelText", "Medical intelligence was used to zero risk in %s."},
	    	 {"ithgText1", "<b>ITHG:</b> Data from international travel and health guidelines (ITHG) were used to zero risk in %s."},
	    	 {"ithgText2", "<b>ITHG:</b> Data from international travel and health guidelines (ITHG) were used to modify risk in %s."},
	    	 {"adminText", "the following administrative units: %s"},
	    	 {"citiesText", "the following cities: %s"},
	    	 {"islandsText", "the following islands: %s"},
	     };
	     
 }
