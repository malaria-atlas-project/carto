package uk.ac.ox.map.carto.server;

import java.util.ArrayList;

import org.hibernate.Session;


import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class Admin0Service {
	
	
	public ArrayList<Admin0> getAdminUnit(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        ArrayList<Admin0> a0 = (ArrayList<Admin0>) session.createQuery("from Admin0 where countryId = 'TUR'").setMaxResults(2).list();
		return a0;
	}
	
	public Envelope getCountryExtent(){
		Session session;
        try {
			session = HibernateUtil.getSessionFactory().getCurrentSession();
        }
        catch (Throwable ex) {
            System.err.println("Initialization failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        session.beginTransaction();
        session.createQuery("from Admin"); 
        
		return null;
		
	}

}
