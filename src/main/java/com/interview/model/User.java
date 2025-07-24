package com.interview.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Table("users")
public class User {
    
    @Id
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String department;
    
    private boolean enabled = true;
    
    // Constructors
    public User() {}
    
    public User(String name, String email, String department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }
    
    public User(String name, String email, String password, String department) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.department = department;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + 
               "', department='" + department + "'}";
    }
}