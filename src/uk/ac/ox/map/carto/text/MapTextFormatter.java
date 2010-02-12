package uk.ac.ox.map.carto.text;

public class MapTextFormatter {
	static String copyright = "<b>Copyright:</b> Licensed to the Malaria Atlas Project (MAP; www.map.ox.ac.uk) under a Creative Commons Attribution 3.0 License (http://creativecommons.org/).";
	static String apiText = "The health management information system data used to inform the stable, unstable and malaria free categories were available at administrative level %s for the following years: %s.";
	static String ithgText = "Data from international and travel health guidelines was used to zero risk in Bago, Bago (Pegu), Magwe, Mandalay, Man Da Lay, Sagaing, Sa Gaing, Yangon and Yangon (Rangoon).";
	
	public static String apiText(){
		return apiText;
	}
	
	public static String getCopyright(){
		return copyright;
	}

}
