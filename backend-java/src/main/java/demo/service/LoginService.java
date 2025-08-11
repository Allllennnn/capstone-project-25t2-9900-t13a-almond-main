package demo.service;

import demo.pojo.User;

public interface LoginService {
    /**
     * Validate username, password, and role; only returns the User if status is ACTIVE
     */
    User authenticate(String username, String password, String role);
}
