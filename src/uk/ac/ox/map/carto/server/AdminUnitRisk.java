package uk.ac.ox.map.carto.server;

public class AdminUnitRisk extends AdminUnit implements RiskMap {
	private String countryId;
	private Integer risk;
	private Double api;
	private String parasite;

	public void setRisk(Integer risk) {
		this.risk = risk;
	}

	public Integer getRisk() {
		return risk;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setApi(Double api) {
		this.api = api;
	}

	public Double getApi() {
		return api;
	}

	public void setParasite(String parasite) {
		this.parasite = parasite;
	}

	public String getParasite() {
		return parasite;
	}
}
