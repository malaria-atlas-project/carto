package uk.ac.ox.map.carto.server;

public class APICountry {
	private Integer year;
	private Boolean medintelZeroed;
	private String adminLevel;
	private Country country;
	private String parasite;
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getYear() {
		return year;
	}
	public void setMedintelZeroed(Boolean medintelZeroed) {
		this.medintelZeroed = medintelZeroed;
	}
	public Boolean getMedintelZeroed() {
		return medintelZeroed;
	}
	public void setAdminLevel(String adminLevel) {
		this.adminLevel = adminLevel;
	}
	public String getAdminLevel() {
		return adminLevel;
	}
	public void setCountry(Country country) {
		this.country = country;
	}
	public Country getCountry() {
		return country;
	}
	public void setParasite(String parasite) {
		this.parasite = parasite;
	}
	public String getParasite() {
		return parasite;
	}
}
