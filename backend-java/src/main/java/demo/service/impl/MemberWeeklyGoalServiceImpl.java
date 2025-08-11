package demo.service.impl;

import demo.pojo.MemberWeeklyGoal;
import demo.mapper.MemberWeeklyGoalMapper;
import demo.service.MemberWeeklyGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MemberWeeklyGoalServiceImpl implements MemberWeeklyGoalService {
    @Autowired
    private MemberWeeklyGoalMapper memberWeeklyGoalMapper;

    @Override
    public void addGoal(MemberWeeklyGoal goal) {
        memberWeeklyGoalMapper.insert(goal);
    }

    @Override
    public void updateGoal(MemberWeeklyGoal goal) {
        memberWeeklyGoalMapper.update(goal);
    }

    @Override
    public void deleteGoal(Long id) {
        memberWeeklyGoalMapper.delete(id);
    }

    @Override
    public List<MemberWeeklyGoal> getGoalsByTaskAndStudent(Long taskId, Long studentId) {
        return memberWeeklyGoalMapper.findByTaskAndStudent(taskId, studentId);
    }

    @Override
    public List<MemberWeeklyGoal> getGoalsByTaskId(Long taskId) {
        return memberWeeklyGoalMapper.findByTaskId(taskId);
    }

    @Override
    public List<MemberWeeklyGoal> getGoalsByTaskIdAndWeekNo(Long taskId, Integer weekNo) {
        return memberWeeklyGoalMapper.findByTaskIdAndWeekNo(taskId, weekNo);
    }
} 