package demo.service;

import demo.pojo.TaskAssignment;
import java.util.List;

public interface TaskAssignmentService {
    void addAssignment(TaskAssignment assignment);
    void removeAssignment(Long taskId, Long userId);
    List<TaskAssignment> getAssignmentsByTaskId(Long taskId);
    List<TaskAssignment> getAssignmentsByUserId(Long userId);
} 