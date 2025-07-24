package com.interview.security.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_roles")
public class UserRole {
    
    @Id
    private Long id;
    
    private Long userId;
    
    private Long roleId;
    
    // Constructors
    public UserRole() {}
    
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    
    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", userId=" + userId +
                ", roleId=" + roleId +
                '}';
    }
}