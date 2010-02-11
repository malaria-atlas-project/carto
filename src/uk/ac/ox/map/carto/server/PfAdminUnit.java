package uk.ac.ox.map.carto.server;

public class PfAdminUnit extends AdminUnit implements RiskMap{
	private Integer risk;
	private Double afi;

	public void setRisk(Integer risk) {
		this.risk = risk;
	}

	public Integer getRisk() {
		return risk;
	}

	public void setAfi(Double afi) {
		this.afi = afi;
	}

	public Double getAfi() {
		return afi;
	}
}
