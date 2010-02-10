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
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Session session = HibernateUtil.getCurrentSession();
        session.beginTransaction();
        Criteria testCriteria = session.createCriteria(AdminUnit.class);
        testCriteria.add(new SpatialFilter("geom", env));
        testCriteria.add(Restrictions.eq("adminLevel", "0"));
        
//        ArrayList<AdminUnit> a0 = (ArrayList<AdminUnit>) session.createQuery("from Admin0").set.list();
        ArrayList<AdminUnit> a0 = (ArrayList<AdminUnit>) testCriteria.list();
		return a0;
	}
	
	public Country getCountry(String countryId){
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Session session = HibernateUtil.getCurrentSession();
        session.beginTransaction();
        return (Country) session.createQuery("from Country where id = :country_id")
        .setParameter("country_id", countryId).uniqueResult();
		
	}

}
