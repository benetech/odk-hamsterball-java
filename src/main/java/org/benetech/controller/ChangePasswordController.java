package org.benetech.controller;

import org.benetech.client.OdkClient;
import org.benetech.client.OdkClientFactory;
import org.benetech.model.form.ChangePasswordForm;
import org.benetech.security.WebServiceDelegatingAuthenticationProvider;
import org.opendatakit.api.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChangePasswordController {

	@Autowired
	OdkClientFactory odkClientFactory;

	@Autowired
	WebServiceDelegatingAuthenticationProvider authenticationProvider;

	@GetMapping("/password")
	public String passwordForm(Model model) {

		return "change_password_form";
	}

	@PostMapping("/password")
	public String passwordSubmit(@ModelAttribute ChangePasswordForm changePasswordForm, Model model,
			Authentication authentication) {

		if (changePasswordForm.getPassword1().equals(changePasswordForm.getPassword2())) {
			OdkClient odkClient = odkClientFactory.getOdkClient();
			odkClient.setCurrentUserPassword(changePasswordForm.getPassword1());
			model.addAttribute("msg", "Password changed successfully.");
			model.addAttribute("css", "success");

			// Log in with new password
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					authentication.getPrincipal(), changePasswordForm.getPassword1());
			Authentication newAuthentication = authenticationProvider.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(newAuthentication);

			return "change_password_form";
		} else {
			model.addAttribute("msg", "Passwords don't match.");
			model.addAttribute("css", "danger");

			return "change_password_form";
		}

	}
}
