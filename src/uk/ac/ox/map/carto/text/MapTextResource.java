package uk.ac.ox.map.carto.text;

import java.util.ListResourceBundle;

public class MapTextResource extends ListResourceBundle {
	     public Object[][] getContents() {
	         return contents;
	     }
	     static final Object[][] contents = {
	    	 {"copyright", "<b>Copyright:</b> Licensed to the Malaria Atlas Project (MAP; www.map.ox.ac.uk) under a Creative Commons Attribution 3.0 License (http://creativecommons.org/)."},
	    	 
	    	 {"apiText", 
	    		"The health management information system data " +
	    		"used to inform the stable, unstable and malaria free categories " +
	    		"were available for {0} at {1} level{2}."},
	    	 {"mohText", "A personal communiation from the Ministry of Health (MOH) was used to zero risk in the following %s."},
	    	 {"apiTextNoInfo","No health management information system data could be accessed. Stable risk is assumed to exist throughout the country."},
	    	 {"apiTextNationalMedIntel","Medical intelligence allowed the definition of risk within the national territory as shown above."},
	    	 {"pfTitle","<i>Plasmodium falciparum</i> risk in %s"},
	    	 {"pvTitle","<i>Plasmodium vivax</i> risk in %s"},
	    	 {"medintelText", "Medical intelligence was used to zero risk in %s."},
	    	 {"ithgText", "Data from international travel and health guidelines was used to zero risk in %s."},
	    	 {"adminText", "the following administrative units: %s"},
	    	 {"citiesText", "the following cities: %s"},
	    	 {"islandsText", "the following islands: %s"},
	     };
	     
 }
