package com.translator.service;


import com.translator.dao.PersonneDAO;
import com.translator.metier.Personne;
import com.translator.utils.SpringBeansContext;


public class AuthService {

    private PersonneDAO dao;
    

    public Personne login(String login, String password) {
    	dao = SpringBeansContext.getContext().getBean(PersonneDAO.class);
        return dao.connectPersonne(login, password);
    }
   
}
