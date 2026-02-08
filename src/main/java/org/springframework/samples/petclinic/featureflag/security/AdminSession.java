package org.springframework.samples.petclinic.featureflag.security;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Getter
@Component
@SessionScope
public class AdminSession {
	private boolean authenticated = false;
	private String username;

	public void login(String username) {
		this.authenticated = true;
		this.username = username;
	}

	public void logout() {
		this.authenticated = false;
		this.username = null;
	}

}
