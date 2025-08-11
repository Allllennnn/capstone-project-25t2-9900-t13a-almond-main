package demo.service.impl;

import demo.pojo.User;
import demo.mapper.GroupMapper;
import demo.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    private GroupMapper groupMapper;

    @Override
    public List<User> getGroupMembers(Long groupId) {
        return groupMapper.findGroupMembers(groupId);
    }
} 