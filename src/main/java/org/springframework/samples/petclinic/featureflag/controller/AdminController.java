package org.springframework.samples.petclinic.featureflag.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.featureflag.security.AdminSession;
import org.springframework.samples.petclinic.featureflag.service.FeatureFlagService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Controller
@Slf4j
public class AdminController {

	@Autowired
	private AdminSession adminSession;

	@Autowired
	private FeatureFlagService flagService;

	@GetMapping("/admin/login")
	public String loginPage() {
		if (adminSession.isAuthenticated()) {
			return "redirect:/admin/flags";
		}
		return "admin/login";
	}

	@PostMapping("/admin/login")
	public String login(@RequestParam String username, @RequestParam String password, Model model) {
		try {
			// Simple hardcoded check - for demo purposes
			if ("admin".equals(username) && "admin123".equals(password)) {
				adminSession.login(username);
				return "redirect:/admin/flags";
			}

			model.addAttribute("error", "Invalid credentials. Try admin/admin123");
			return "admin/login";
		} catch (Exception e) {
			log.error("Error during login", e);
			model.addAttribute("error", "An error occurred during login. Please try again.");
			return "admin/login";
		}
	}

	@GetMapping("/admin/logout")
	public String logout() {
		try {
			adminSession.logout();
		} catch (Exception e) {
			log.error("Error during logout", e);
		}
		return "redirect:/admin/login";
	}

	@GetMapping("/admin/flags")
	public String flagDashboard(Model model) {
		try {
			model.addAttribute("username", adminSession.getUsername());
			model.addAttribute("flags", flagService.getAllFlags("development"));
			model.addAttribute("recentAudits", flagService.getRecentAuditLog());
			return "admin/flags";
		} catch (Exception e) {
			log.error("Error loading flag dashboard", e);
			model.addAttribute("error", "Unable to load feature flags. Please check the system configuration.");
			model.addAttribute("username", adminSession.getUsername());
			model.addAttribute("flags", Collections.emptyList());
			model.addAttribute("recentAudits", Collections.emptyList());
			return "admin/flags";
		}
	}
}
