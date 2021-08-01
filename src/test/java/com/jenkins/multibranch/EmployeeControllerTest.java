package com.jenkins.multibranch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class EmployeeControllerTest {

	@InjectMocks
	private EmployeeController employeeController;
	
	@Test
	public void testGetHomePage() {
		String answer = this.employeeController.homePage();
		assertEquals(answer, "Home page");
	}

	@Test
	public void testGetEmployee() {
		Employee empExpected = new Employee();
		empExpected.setId(1);
		empExpected.setName("meenal");
		empExpected.setDepartment("IT");
		
		Employee empActual  = this.employeeController.getEmployee();
		assertThat(empActual).usingRecursiveComparison().isEqualTo(empExpected);
	}
}
