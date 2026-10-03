package com.translator.dao;

import java.util.List;


public interface GenericDAO<T> {

	public T findById(int id);

	public List<T> findAll();

	public void persist(T entity);

	public void remove(T entity);

	public void update(T entity);
	

}
