package com.translator.beans;

import com.translator.metier.Personne;


public class UserInfo {	
	
	private Personne currentUser;

	public Personne getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(Personne currentUser) {
		this.currentUser = currentUser;
	}
	
}
