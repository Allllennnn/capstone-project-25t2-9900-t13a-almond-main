package demo.controller;

import demo.pojo.Result;
import demo.pojo.Task;
import demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /** Create a new task */
    @PostMapping("/add")
    public Result addTask(@RequestBody Task task) {
        // Default status set to INITIALIZING when creating a new task
        task.setStatus("INITIALIZING");
        taskService.addTask(task);
        return Result.success(task.getId());
    }

    /** Get task by ID */
    @GetMapping("/get")
    public Result getTaskById(@RequestParam Long id) {
        Task task = taskService.getTaskById(id);
        return Result.success(task);
    }

    /** Get tasks by group ID */
    @GetMapping("/by-group")
    public Result getTasksByGroupId(@RequestParam Long groupId) {
        List<Task> list = taskService.getTasksByGroupId(groupId);
        return Result.success(list);
    }
}
