package demo.controller;

import demo.pojo.Result;
import demo.pojo.MemberWeeklyGoal;
import demo.service.MemberWeeklyGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member-weekly-goal")
public class MemberWeeklyGoalController {

    @Autowired
    private MemberWeeklyGoalService memberWeeklyGoalService;

    /** Add a member weekly goal */
    @PostMapping("/add")
    public Result addGoal(@RequestBody MemberWeeklyGoal goal) {
        memberWeeklyGoalService.addGoal(goal);
        return Result.success();
    }

    /** Update a member weekly goal */
    @PutMapping("/update")
    public Result updateGoal(@RequestBody MemberWeeklyGoal goal) {
        memberWeeklyGoalService.updateGoal(goal);
        return Result.success();
    }

    /** Delete a member weekly goal */
    @DeleteMapping("/delete")
    public Result deleteGoal(@RequestParam Long id) {
        memberWeeklyGoalService.deleteGoal(id);
        return Result.success();
    }

    /** Retrieve goals by task and student */
    @GetMapping("/by-task-student")
    public Result getGoalsByTaskAndStudent(@RequestParam Long taskId,
                                           @RequestParam Long studentId) {
        List<MemberWeeklyGoal> list = memberWeeklyGoalService.getGoalsByTaskAndStudent(taskId, studentId);
        return Result.success(list);
    }

    /** Retrieve all member goals for a specific task */
    @GetMapping("/by-task")
    public Result getGoalsByTaskId(@RequestParam Long taskId) {
        List<MemberWeeklyGoal> list = memberWeeklyGoalService.getGoalsByTaskId(taskId);
        return Result.success(list);
    }
}
