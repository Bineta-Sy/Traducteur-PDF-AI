package com.translator.utils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class HibernateContextListner implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			SessionFactory sessionFactory = HibernateUtil.getFactory();
			System.out.println("Hibenate SessionFactory initilized.");
			sce.getServletContext().setAttribute("SessionFactory", sessionFactory);
			System.out.println("Hibenate SessionFactory set in context.");
		}catch(HibernateException e){
			System.out.println("Hibenate initialization failed."+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		SessionFactory sessionFactory = (SessionFactory)sce.getServletContext().getAttribute("SessionFactory");
		if(sessionFactory != null && sessionFactory.isOpen()) {
			sessionFactory.close();
			System.out.println("Hibernate Session closed!");
		}
		ServletContextListener.super.contextDestroyed(sce);
	}

}
