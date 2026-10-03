package com.translator.beans;

import java.io.Serializable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.translator.metier.Personne;
import com.translator.service.AuthService;
import com.translator.utils.SpringBeansContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@RequestScoped
public class LoginBean implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Personne p = SpringBeansContext.getContext().getBean(Personne.class);
	
	private AuthService authService = SpringBeansContext.getContext().getBean(AuthService.class);
	
	private UserInfo userInfo = SpringBeansContext.getContext().getBean(UserInfo.class);
	
	private String login;
	private String password;

	public String logPersonne() {
			p = authService.login(login, password) ;
			if(p != null) {
				System.out.print("u sucessfully logged in");
				System.out.println(p);
				
				userInfo = SpringBeansContext.getContext().getBean(UserInfo.class);
				userInfo.setCurrentUser(p);
		        return "/index.xhtml?faces-redirect=true"; 
				
			}
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "mot de pass ou login incorrect", null));
    		       return null;
	}
	
	
	// getters and setters
	
	
	public String getLogin() {
		return login;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}


	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}


	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public Personne getP() {
		return p;
	}

	public void setP(Personne p) {
		this.p = p;
	}

	public AuthService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthService authService) {
		this.authService = authService;
	}
	
	
}
