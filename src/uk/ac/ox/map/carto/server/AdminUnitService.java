package uk.ac.ox.map.carto.server;

import java.util.ArrayList;


import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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
	
	
	public ArrayList<AdminUnit> getAdminUnit(Geometry env){
		Session session = HibernateUtil.getCurrentSession();
        session.beginTransaction();
        Criteria testCriteria = session.createCriteria(AdminUnit.class);
        testCriteria.add(new SpatialFilter("geom", env));
        testCriteria.add(Restrictions.eq("adminLevel", "0"));
        ArrayList<AdminUnit> a0 = (ArrayList<AdminUnit>) testCriteria.list();
		return a0;
	}
	
	public PfCountry getCountry(String countryId){
		Session session = HibernateUtil.getCurrentSession();
        session.beginTransaction();
        return (PfCountry) session.createQuery("from PfCountry where id = :country_id")
        .setParameter("country_id", countryId).uniqueResult();
	}
	
	public ArrayList<PfAdminUnit> getPfAdminUnits(String countryId) {
		Session session = HibernateUtil.getCurrentSession();
        session.beginTransaction();
        Criteria testCriteria = session.createCriteria(PfAdminUnit.class);
        testCriteria.add(Restrictions.eq("countryId", countryId));
        ArrayList<PfAdminUnit> pf = (ArrayList<PfAdminUnit>) testCriteria.list();
		return pf;
	}

}
