package com.deliciouspizza.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Hello world!";
    }

    @GetMapping("/test-customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String testCustomer() {
        return "Hello, Customer!";
    }

    @GetMapping("/test-employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String testEmployee() {
        return "Hello, employee";
    }
}
