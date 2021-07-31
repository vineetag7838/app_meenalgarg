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
	
	@GetMapping("/employees")
	public List<Employee> getEmployees(){
		List<Employee> employees = new ArrayList<Employee>();
		
		Employee emp = new Employee();
		emp.setId(1);
		emp.setName("meenal");
		emp.setDepartment("IT");
		
		employees.add(emp);
		
		return employees;
	}
	

}
