package com.smart.controller;



import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response

	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println("USER NAME: "+userName);

		// get the user using userName(Email)

		User user = userRepository.getUserByUserName(userName);

		System.out.println("User "+user);

		model.addAttribute("user",user);
	}

	//dashboard home
	@RequestMapping("/index")
	public String Dashboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}

	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());


		return "normal/add_contact_form";
	}

	//processing add contact
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) {

try {
		String name = principal.getName();
		User user =this.userRepository.getUserByUserName(name);
		
		
		///Processing and uploading file
		
		if(file.isEmpty()) {
			// if the file is empty then try our messgae
			
			System.out.println("File is empty");
			contact.setImage("download.png");
			
			
		}else {
			// file the file to folder and update the name to contact
			
			contact.setImage(file.getOriginalFilename());
			
			/* File saveFile = new ClassPathResourse("static/img").getFile(); */
			
			File saveFile=new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			 System.out.println("Target Path: "+path);
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		user.getContacts().add(contact);
		contact.setUser(user);
		
		this.userRepository.save(user);



		System.out.println("DATA "+contact);
		System.out.println("Added to data base");
		
		///Success message...
		
		session.setAttribute("message", new Message("Your contact is added !! And more..","success"));
		
		

}catch(Exception e){
	
	System.out.println("Error : "+e);
	e.printStackTrace();
	
	/// error message
	
	session.setAttribute("message", new Message("Something is went !! try again.","danger"));
}
		return "normal/add_contact_form";

	}
	
	
	//show contacts handler
	// per page =5[n]
	//current page =0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title","Show user contacts");
		/*
		 * //contacts from db
		 * 
		 * String userName = principal.getName();
		 * 
		 * User user = this.userRepository.getUserByUserName(userName); List<Contact>
		 * contacts = user.getContacts();
		 */
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		m.addAttribute("contacts", contacts);
		
				return "normal/show_contacts";
	}
	
	///showing perticular contact details.
	@RequestMapping("/{cid}/contact")
	public String showContactDeatil(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		System.out.println("CID : "+cid);
		
		Optional<Contact> contactOptional= this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		//
		
		String userName= principal.getName();
		User user =this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			
			model.addAttribute("contact", contact);
		}
		
		return "normal/contact_detail";
	}
	
	/// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model,HttpSession session,Principal principal) {
		
		Optional<Contact> contactOptional= this.contactRepository.findById(cid);
		
		Contact contact = contactOptional.get();
		
//        contact.setUser(null);
		
	User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        this.userRepository.save(user);
        
        
        
		this.contactRepository.delete(contact);
		
		session.setAttribute("message",new Message("Contact deleted successfully...","success"));
		
		
		return "redirect:/user/show-contacts/0";
		
		
	}
	
	// Open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		
		model.addAttribute("title","update contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact",contact);
		
		return "normal/update_form";
		
	}
	
	
	//update process-update
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal) {
		
		try {
			
			/* old contact details */
			
			Contact oldContact= this.contactRepository.findById(contact.getCid()).get();
			//image validate
			if(!file.isEmpty()) {
				/// file work
				//rewrite
				
				
				/* delete old photo */
				
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldContact.getImage());
				
				/* update new photo */
				
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				 System.out.println("Target Path: "+path);
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
				
			}else {
				
				contact.setImage(oldContact.getImage());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		contact.setUser(user);
		
		this.contactRepository.save(contact);
		
		session.setAttribute("message",new Message("Your contact is updated ...","success"));
		
		
		System.out.println("Contact Name : "+contact.getName());
		System.out.println("Contact ID : "+contact.getCid());
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}

	
	// your profile
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		
		
		model.addAttribute("title", "Your Profile");
	return "normal/profile";	
	}
} 