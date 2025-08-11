package demo.service.impl;

import demo.pojo.TaskWeek;
import demo.mapper.TaskWeekMapper;
import demo.service.TaskWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of TaskWeekService
 */
@Service
public class TaskWeekServiceImpl implements TaskWeekService {

    @Autowired
    private TaskWeekMapper taskWeekMapper;

    @Override
    public void addTaskWeek(TaskWeek taskWeek) {
        taskWeekMapper.insert(taskWeek);
    }

    @Override
    public void updateTaskWeek(TaskWeek taskWeek) {
        taskWeekMapper.update(taskWeek);
    }

    @Override
    public void deleteTaskWeek(Long id) {
        taskWeekMapper.delete(id);
    }

    @Override
    public List<TaskWeek> getTaskWeeksByTaskId(Long taskId) {
        return taskWeekMapper.findByTaskId(taskId);
    }

    @Override
    public TaskWeek getTaskWeekByTaskIdAndWeekNo(Long taskId, Integer weekNo) {
        return taskWeekMapper.findByTaskIdAndWeekNo(taskId, weekNo);
    }

    @Override
    public void deleteTaskWeeksByTaskId(Long taskId) {
        taskWeekMapper.deleteByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateTaskWeeksForCycle(Long taskId, Integer cycle, String baseGoal) {
        // Remove existing weekly goals for the task
        taskWeekMapper.deleteByTaskId(taskId);

        // Generate weekly goals for each week in the cycle
        for (int weekNo = 1; weekNo <= cycle; weekNo++) {
            TaskWeek taskWeek = new TaskWeek();
            taskWeek.setTaskId(taskId);
            taskWeek.setWeekNo(weekNo);
            taskWeek.setGoal(String.format("Week %d goal: %s", weekNo, baseGoal));
            taskWeekMapper.insert(taskWeek);
        }
    }
}
