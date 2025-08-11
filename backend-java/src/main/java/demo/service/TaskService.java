package demo.service;

import demo.pojo.Task;
import java.util.List;

public interface TaskService {
    void addTask(Task task);
    Task getTaskById(Long id);
    List<Task> getTasksByGroupId(Long groupId);
} 