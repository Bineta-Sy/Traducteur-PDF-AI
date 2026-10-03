package com.translator.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringBeansContext {
	
	private static ApplicationContext context;
	
	static {
		context = new ClassPathXmlApplicationContext ("SpringBeans.xml");
	}
	
	public static ApplicationContext getContext() {
		return context;
	}

}
