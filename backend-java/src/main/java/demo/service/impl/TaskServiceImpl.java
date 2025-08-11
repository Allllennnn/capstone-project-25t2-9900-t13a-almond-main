package demo.service.impl;

import demo.pojo.Task;
import demo.mapper.TaskMapper;
import demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public void addTask(Task task) {
        taskMapper.insert(task);
    }

    @Override
    public Task getTaskById(Long id) {
        return taskMapper.findById(id);
    }

    @Override
    public List<Task> getTasksByGroupId(Long groupId) {
        return taskMapper.findByGroupId(groupId);
    }
} 