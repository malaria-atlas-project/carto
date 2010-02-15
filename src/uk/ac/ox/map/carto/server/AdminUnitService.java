package uk.ac.ox.map.carto.server;

import java.util.ArrayList;


import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.TypedValue;
import org.hibernatespatial.SpatialRelation;
import org.hibernatespatial.criterion.SpatialFilter;
import org.hibernatespatial.criterion.SpatialRestrictions;


import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class AdminUnitService {
	
	private final org.hibernate.classic.Session session = HibernateUtil.getCurrentSession();
	public AdminUnitService() {
        session.beginTransaction();
	}
	
	
	
	public ArrayList<AdminUnit> getAdminUnit(Geometry env){
        Criteria testCriteria = session.createCriteria(AdminUnit.class);
        testCriteria.add(new SpatialFilter("geom", env));
        testCriteria.add(Restrictions.eq("adminLevel", "0"));
        ArrayList<AdminUnit> a0 = (ArrayList<AdminUnit>) testCriteria.list();
		return a0;
	}
	
	public PfCountry getCountry(String countryId){
        return (PfCountry) session.createQuery("from PfCountry where id = :country_id")
        .setParameter("country_id", countryId).uniqueResult();
	}

	public ArrayList<PfCountry> getCountries(){
        return (ArrayList<PfCountry>) session.createQuery("from PfCountry")
        .list();
	}
	
	public ArrayList<PfAdminUnit> getPfAdminUnits(String countryId) {
		session.clear();
        Criteria testCriteria = session.createCriteria(PfAdminUnit.class);
        testCriteria.add(Restrictions.eq("countryId", countryId));
        ArrayList<PfAdminUnit> pf = (ArrayList<PfAdminUnit>) testCriteria.list();
		return pf;
	}

}
