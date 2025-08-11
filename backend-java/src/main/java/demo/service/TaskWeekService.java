package demo.service;

import demo.pojo.TaskWeek;
import java.util.List;

/**
 * TaskWeek service interface
 */
public interface TaskWeekService {

    /**
     * Add a weekly goal for a task
     */
    void addTaskWeek(TaskWeek taskWeek);

    /**
     * Update a weekly goal
     */
    void updateTaskWeek(TaskWeek taskWeek);

    /**
     * Delete a weekly goal by its ID
     */
    void deleteTaskWeek(Long id);

    /**
     * Retrieve all weekly goals for a given task
     */
    List<TaskWeek> getTaskWeeksByTaskId(Long taskId);

    /**
     * Retrieve a specific weekly goal by task ID and week number
     */
    TaskWeek getTaskWeekByTaskIdAndWeekNo(Long taskId, Integer weekNo);

    /**
     * Delete all weekly goals for a given task
     */
    void deleteTaskWeeksByTaskId(Long taskId);

    /**
     * Generate weekly goals for all cycles of a task
     *
     * @param taskId   ID of the task
     * @param cycle    number of weeks in the cycle
     * @param baseGoal base description for each week's goal
     */
    void generateTaskWeeksForCycle(Long taskId, Integer cycle, String baseGoal);
}
