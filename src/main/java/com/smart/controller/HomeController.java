package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;


	@RequestMapping("/")
	public String Home(Model model) {
		model.addAttribute("title","Home - phone book");
		return"home";
	}

	@RequestMapping("/about")
	public String About(Model model) {
		model.addAttribute("title","About -  phone book");
		return"about";
	}


	@RequestMapping("/signup")
	public String Signup(Model model) {
		model.addAttribute("title","Register -  phone book");
		model.addAttribute("user",new User());
		return"signup";
	}

	// this handler for registering user


	@RequestMapping(value= "/do_register",method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value="agreement",defaultValue="false") boolean agreement,Model model,HttpSession session) {

		try {
			if(!agreement) {

				System.out.println("You are not agreed the terms and conditons");
				throw new Exception("You are not agreed the terms and conditons");
			}
			
			if(result1.hasErrors()) {
				System.out.println("Error " +result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}


			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement " +agreement);
			System.out.println("User " +user);
			User result= this.userRepository.save(user);

			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully Registered !!","alert-success"));
			return "signup";

		}

		catch(Exception e) {

			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Somethying went wrong !! " +e.getMessage(),"alert-danger"));
			return "signup";

		}



	}
	
	
	/// handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login Page");
		
		return "login";
	}


	/* Testing */
	/*
	 * @Autowired private UserRepository userRepository;
	 * 
	 * @GetMapping("/test")
	 * 
	 * @ResponseBody public String test() {
	 * 
	 * User user=new User(); user.setName("Libul"); user.setEmail("satya@g.com");
	 * 
	 * Contact contact = new Contact(); user.getContacts().add(contact);
	 * 
	 * userRepository.save(user); return "Working"; }
	 */
}
