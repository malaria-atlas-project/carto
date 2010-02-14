package uk.ac.ox.map.carto.server;

import java.util.Set;

public class PfCountry extends Country {
	
	private Set<Integer> years;
	private Set<String> zeroed;
	
	public void setYears(Set<Integer> years) {
		this.years = years;
	}

	public Set<Integer> getYears() {
		return years;
	}
	
	public void setZeroed(Set<String> zeroed) {
		this.zeroed = zeroed;
	}

	public Set<String> getZeroed() {
		return zeroed;
	}



}
