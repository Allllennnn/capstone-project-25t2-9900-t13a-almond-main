package demo.service.impl;

import demo.pojo.TaskAssignment;
import demo.mapper.TaskAssignmentMapper;
import demo.service.TaskAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskAssignmentServiceImpl implements TaskAssignmentService {
    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;

    @Override
    public void addAssignment(TaskAssignment assignment) {
        taskAssignmentMapper.insert(assignment);
    }

    @Override
    public void removeAssignment(Long taskId, Long userId) {
        taskAssignmentMapper.delete(taskId, userId);
    }

    @Override
    public List<TaskAssignment> getAssignmentsByTaskId(Long taskId) {
        return taskAssignmentMapper.findByTaskId(taskId);
    }

    @Override
    public List<TaskAssignment> getAssignmentsByUserId(Long userId) {
        return taskAssignmentMapper.findByUserId(userId);
    }
} 