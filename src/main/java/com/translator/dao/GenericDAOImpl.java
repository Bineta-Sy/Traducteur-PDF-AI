package com.translator.dao;


import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import jakarta.faces.context.FacesContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class GenericDAOImpl<T> implements GenericDAO<T>{
	private Class<T> entityClass;
	
	public GenericDAOImpl(Class<T> entityClass) {
		super();
		this.entityClass = entityClass;
	}

	@Override
	public T findById(int id) {
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		Session session = null;
		try {
			session = sessionFactory.openSession();
			CriteriaBuilder  builder = session.getCriteriaBuilder();
			CriteriaQuery<T> cq = builder.createQuery(entityClass);
			Root<T> root = cq.from(entityClass);
			cq.select(root).where(builder.equal(root.get("id"), id));
			T result = session.createQuery(cq).uniqueResult();
			return result;
			
		}catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
		return null;
	}


	@Override
	public List<T> findAll() {
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		Session session = null;
		try {
			session = sessionFactory.openSession();
			CriteriaBuilder  criteria = session.getCriteriaBuilder();
			CriteriaQuery<T> cq = criteria.createQuery(entityClass);
			Root<T> root = cq.from(entityClass);
			cq.select(root);
			List<T> results = session.createQuery(cq).getResultList();
			return results; 
			
		}catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
            
        }
		return null;
	}
	
	
	@Override
	public void persist(T entity) {
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		Transaction tx = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.persist(entity);
//			System.out.println("this voyageur is save success!");
			tx.commit();
		}catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
	}

	@Override
	public void remove(T entity) {
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		Transaction tx = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.remove(entity);
			System.out.println("object deleted.");
			tx.commit();
		}catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
		
	}
	@Override
	public void update(T entity) {
		SessionFactory sessionFactory = (SessionFactory)FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("SessionFactory");
		Transaction tx = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			session.merge(entity);
			tx.commit();
		}catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
		
	}


	
	 
}
