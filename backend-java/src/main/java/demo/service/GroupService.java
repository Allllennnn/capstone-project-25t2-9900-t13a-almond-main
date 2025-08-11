package demo.service;

import demo.pojo.User;
import java.util.List;

public interface GroupService {
    List<User> getGroupMembers(Long groupId);
} 