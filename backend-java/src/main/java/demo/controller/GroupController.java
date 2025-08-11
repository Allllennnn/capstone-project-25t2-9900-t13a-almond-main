package demo.controller;

import demo.pojo.Result;
import demo.pojo.User;
import demo.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {
    @Autowired
    private GroupService groupService;

    /** Get a list of group members */
    @GetMapping("/members")
    public Result getGroupMembers(@RequestParam Long groupId) {
        List<User> members = groupService.getGroupMembers(groupId);
        return Result.success(members);
    }
} 