package com.jenkins.multibranch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

	
	@GetMapping("/")
	public String homePage() {
		return "Home page";
	}
	
	@GetMapping("/employee")
	public Employee getEmployee(){
				
		Employee emp = new Employee();
		emp.setId(1);
		emp.setName("meenal");
		emp.setDepartment("IT");
		
		return emp;
	}
	

}
