package demo.controller;

import demo.pojo.Result;
import demo.pojo.TaskAssignment;
import demo.service.TaskAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for task assignment operations
 */
@RestController
@RequestMapping("/task-assignment")
public class TaskAssignmentController {

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    /** Add a new task assignment */
    @PostMapping("/add")
    public Result addAssignment(@RequestBody TaskAssignment assignment) {
        taskAssignmentService.addAssignment(assignment);
        return Result.success();
    }

    /** Remove a task assignment */
    @DeleteMapping("/remove")
    public Result removeAssignment(@RequestParam Long taskId,
                                   @RequestParam Long userId) {
        taskAssignmentService.removeAssignment(taskId, userId);
        return Result.success();
    }

    /** Retrieve assignments by task ID */
    @GetMapping("/by-task")
    public Result getAssignmentsByTaskId(@RequestParam Long taskId) {
        List<TaskAssignment> list = taskAssignmentService.getAssignmentsByTaskId(taskId);
        return Result.success(list);
    }

    /** Retrieve assignments by user ID */
    @GetMapping("/by-user")
    public Result getAssignmentsByUserId(@RequestParam Long userId) {
        List<TaskAssignment> list = taskAssignmentService.getAssignmentsByUserId(userId);
        return Result.success(list);
    }
}
