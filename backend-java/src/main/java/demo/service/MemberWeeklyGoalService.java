package demo.service;

import demo.pojo.MemberWeeklyGoal;
import java.util.List;

public interface MemberWeeklyGoalService {
    void addGoal(MemberWeeklyGoal goal);
    void updateGoal(MemberWeeklyGoal goal);
    void deleteGoal(Long id);
    List<MemberWeeklyGoal> getGoalsByTaskAndStudent(Long taskId, Long studentId);
    List<MemberWeeklyGoal> getGoalsByTaskId(Long taskId);
    List<MemberWeeklyGoal> getGoalsByTaskIdAndWeekNo(Long taskId, Integer weekNo);
} 