package com.translator.dao;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import com.translator.metier.Personne;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;


public class PersonneDAO {

	public Personne findPersonneWithLogin(String login, String password) {
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		Session session = null;
		try {
			session = sessionFactory.openSession();
			CriteriaBuilder  builder = session.getCriteriaBuilder();
			CriteriaQuery<Personne> cq = builder.createQuery(Personne.class);
			Root<Personne> root = cq.from(Personne.class);
			cq.select(root).where(builder.equal(root.get("login"), login), builder.equal(root.get("password"), password));
			Personne personne = session.createQuery(cq).uniqueResult();
			if(personne != null) System.out.println("le nom connecté:"+login);
			return personne;
			
		}catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
		return null;
	
	}
	
	public Personne connectPersonne(String login, String psw) {
		Session session = null;
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		try {
			session = sessionFactory.openSession();
			Personne p = findPersonneWithLogin(login, psw);
			if(p != null) {
				System.out.println("connected.");
				return p;
			}
			System.out.println("Not connected.");
			return null;
			
		}catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
		return null;
	}

}
