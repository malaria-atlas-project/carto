package uk.ac.ox.map.carto.server;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernatespatial.criterion.SpatialFilter;

import com.vividsolutions.jts.geom.Envelope;

public class AdminUnitService {
	private static final int SRID = 4326;
	
	private final org.hibernate.classic.Session session = HibernateUtil.getCurrentSession();
	public AdminUnitService() {
        session.beginTransaction();
	}
	
	public List<AdminUnit> getAdminUnits(Envelope env){
        Criteria testCriteria = session.createCriteria(AdminUnit.class);
        testCriteria.add(new SpatialFilter("geom", env, SRID));
        testCriteria.add(Restrictions.eq("adminLevel", "0"));
//        testCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return testCriteria.list();
	}
	
	public List<WaterBody> getWaterBodies(Envelope env){
        Criteria testCriteria = session.createCriteria(WaterBody.class);
        testCriteria.add(new SpatialFilter("geom", env, SRID));
		return testCriteria.list();
	}

	public List<Country> getCountries(String parasite){
		
        Criteria testCriteria = session.createCriteria(Country.class);
        if (parasite.compareTo("pf") == 0)
	        testCriteria.add(Restrictions.eq("pfEndemic", true));
        else if (parasite.compareTo("pv") == 0)
	        testCriteria.add(Restrictions.eq("pvEndemic", true));
        testCriteria.addOrder(Order.asc("id"));
        return testCriteria.list();
	}
	
	public List<AdminUnitRisk> getRiskAdminUnits(Country country, String parasite) {
		session.clear();
        Criteria testCriteria = session.createCriteria(AdminUnitRisk.class);
        testCriteria.add(Restrictions.eq("countryId", country.getId()));
        testCriteria.add(Restrictions.eq("parasite", parasite));
		return testCriteria.list();
	}
	
	public List<Exclusion> getExclusions(Country country) {
        Criteria testCriteria = session.createCriteria(Exclusion.class);
        testCriteria.add(Restrictions.eq("country", country));
		return testCriteria.list();
	}
	
	public Country getCountry(String countryId){
        return (Country) session.createQuery("from Country where id = :country_id")
        .setParameter("country_id", countryId).uniqueResult();
	}

	public List<Integer> getYears(Country country, String parasite) {
		Query query = session.getNamedQuery("api.years");
		query.setParameter("country_id", country.getId());
		query.setParameter("parasite", parasite);
		return (List<Integer>) query.list();
	}
	
	public List<String> getZeroed(Country country, String parasite) {
		Query query = session.getNamedQuery("api.medintelZeroed");
		query.setParameter("country_id", country.getId());
		query.setParameter("parasite", parasite);
		return (List<String>) query.list();
	}
	
	public String getAdminLevel(Country country, String parasite) {
		Query query = session.getNamedQuery("api.admin_level");
		query.setParameter("country_id", country.getId());
		query.setParameter("parasite", parasite);
		String adminLevel = (String) query.uniqueResult();
		if (adminLevel == null)
			adminLevel = "";
		return adminLevel;
	}
	
	public Integer getAdminUnitCount(Country country, String parasite) {
		Query query = session.getNamedQuery("api.counts");
		query.setParameter("country_id", country.getId());
		query.setParameter("parasite", parasite);
		return (Integer) query.uniqueResult();
	}
}
