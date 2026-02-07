package org.springframework.samples.petclinic.featureflag.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class AdminSession {
	private boolean authenticated = false;
	private String username;

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void login(String username) {
		this.authenticated = true;
		this.username = username;
	}

	public void logout() {
		this.authenticated = false;
		this.username = null;
	}

	public String getUsername() {
		return username;
	}
}
