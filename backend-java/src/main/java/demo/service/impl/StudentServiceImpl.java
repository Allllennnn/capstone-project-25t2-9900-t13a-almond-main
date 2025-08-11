package demo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import demo.mapper.ConversationMapper;
import demo.mapper.GroupMapper;
import demo.mapper.MemberWeeklyGoalMapper;
import demo.mapper.MeetingMapper;
import demo.mapper.TaskAssignmentMapper;
import demo.mapper.TaskMapper;
import demo.mapper.TaskWeekMapper;
import demo.mapper.UserGroupMapper;
import demo.mapper.UserMapper;
import demo.pojo.Conversation;
import demo.pojo.Group;
import demo.pojo.MemberWeeklyGoal;
import demo.pojo.PageBean;
import demo.pojo.Task;
import demo.pojo.TaskAssignment;
import demo.pojo.User;
import demo.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserGroupMapper ugMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private TaskWeekMapper taskWeekMapper;

    @Autowired
    private MeetingMapper meetingMapper;

    @Autowired
    private MemberWeeklyGoalMapper memberWeeklyGoalMapper;

    @Override
    public PageBean<User> page(Integer page, Integer pageSize, String name, Long id) {
        PageHelper.startPage(page, pageSize);
        List<User> list = userMapper.findByRoleAndFilter("STUDENT", name, id);
        Page<User> pg = (Page<User>) list;
        return new PageBean<>(pg.getTotal(), pg.getResult());
    }

    @Override
    public Map<String, Object> getMyGroupWithMembers(Long userId) {
        Long groupId = ugMapper.findGroupIdByUserId(userId);
        Group group = groupMapper.findById(groupId);
        List<User> members = ugMapper.findMembersByGroupId(groupId);
        Map<String, Object> result = new HashMap<>();
        result.put("group", group);
        result.put("members", members);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(Long userId, Long groupId) {
        ugMapper.delete(userId, groupId);
    }

    @Override
    public List<Group> getStudentGroups(Long userId) {
        return ugMapper.findGroupsByUserId(userId);
    }

    @Override
    public List<Task> getGroupTasks(Long userId, Long groupId) {
        // Verify the user belongs to the group
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(groupId)) {
            throw new RuntimeException("User does not belong to this group");
        }
        return taskMapper.findByGroupId(groupId);
    }

    @Override
    public List<User> getGroupMembers(Long userId, Long groupId) {
        // Verify the user belongs to the group
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(groupId)) {
            throw new RuntimeException("User does not belong to this group");
        }
        return ugMapper.findMembersByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTaskAssignment(Long userId, Map<String, Object> assignmentData) {
        Long taskId = Long.valueOf(assignmentData.get("taskId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assignments =
                (List<Map<String, Object>>) assignmentData.get("assignments");

        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }

        // Remove existing assignments for the task
        taskAssignmentMapper.deleteByTaskId(taskId);

        // Insert new assignments
        for (Map<String, Object> map : assignments) {
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTaskId(taskId);
            assignment.setUserId(Long.valueOf(map.get("userId").toString()));
            assignment.setRole((String) map.get("role"));
            assignment.setDescription((String) map.get("description"));
            assignment.setAssignedBy(userId);
            assignment.setStatus("SUBMITTED");
            taskAssignmentMapper.insert(assignment);
        }
    }

    @Override
    public Map<String, Object> getTaskAssignmentStatus(Long userId, Long taskId) {
        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }

        List<TaskAssignment> assignments = taskAssignmentMapper.findByTaskId(taskId);
        Map<String, Object> result = new HashMap<>();
        if (assignments.isEmpty()) {
            result.put("status", "DRAFT");
            result.put("assignments", assignments);
        } else {
            result.put("status", assignments.get(0).getStatus());
            result.put("assignments", assignments);
        }
        return result;
    }

    @Override
    public Task getTaskById(Long taskId) {
        return taskMapper.findById(taskId);
    }

    @Override
    public Group getGroupById(Long groupId) {
        return groupMapper.findById(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskAssignment(Long userId, Map<String, Object> assignmentData) {
        Long taskId = Long.valueOf(assignmentData.get("taskId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assignments =
                (List<Map<String, Object>>) assignmentData.get("assignments");

        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }

        // Remove existing assignments
        taskAssignmentMapper.deleteByTaskId(taskId);

        // Insert updated assignments
        for (Map<String, Object> map : assignments) {
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTaskId(taskId);
            assignment.setUserId(Long.valueOf(map.get("userId").toString()));
            assignment.setRole((String) map.get("role"));
            assignment.setDescription((String) map.get("description"));
            assignment.setAssignedBy(userId);
            assignment.setStatus("SUBMITTED");
            taskAssignmentMapper.insert(assignment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finalizeTaskAssignment(Long userId, Long taskId) {
        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }

        // Mark all assignments as FINALIZED
        taskAssignmentMapper.updateStatusByTaskId(taskId, "FINALIZED");
    }

    @Override
    public List<TaskAssignment> getFinalizedAssignments(Long userId, Long taskId) {
        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }
        return taskAssignmentMapper.findByTaskIdAndStatus(taskId, "FINALIZED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConversationMessage(Long taskId,
                                        Long groupId,
                                        Long senderId,
                                        String senderType,
                                        String content) {
        // Prevent duplicate messages
        List<Map<String, Object>> recent = conversationMapper
                .findRecentByTaskAndGroup(taskId, groupId, 5);
        for (Map<String, Object> msg : recent) {
            if (content.equals(msg.get("content")) &&
                    senderType.equals(msg.get("sender_type")) &&
                    (senderId == null
                            ? msg.get("sender_id") == null
                            : senderId.equals(msg.get("sender_id")))) {
                log.info("Duplicate message detected, skipping save");
                return;
            }
        }
        // Save new message
        Conversation msg = new Conversation();
        msg.setTaskId(taskId);
        msg.setGroupId(groupId);
        msg.setSenderId(senderId);
        msg.setSenderType(senderType);
        msg.setContent(content);
        conversationMapper.insert(msg);
    }

    @Override
    public List<Map<String, Object>> getConversationHistory(Long userId,
                                                            Long taskId,
                                                            Long groupId) {
        // Validate permission
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(groupId)) {
            throw new RuntimeException("User does not belong to this group");
        }
        List<Map<String, Object>> conv = conversationMapper
                .findByTaskIdAndGroupId(taskId, groupId);
        log.info("Retrieved {} conversation entries for taskId={}, groupId={}",
                conv.size(), taskId, groupId);
        for (Map<String, Object> c : conv) {
            log.info("Conversation: senderType={}, senderId={}, senderName={}, contentLength={}",
                    c.get("sender_type"),
                    c.get("sender_id"),
                    c.get("sender_name"),
                    c.get("content") != null
                            ? c.get("content").toString().length() : 0);
        }
        return conv;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmTaskAssignment(Long userId,
                                      Long taskId,
                                      Integer cycle) {
        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }
        List<TaskAssignment> assignments = taskAssignmentMapper.findByTaskId(taskId);
        if (assignments.isEmpty()) {
            throw new RuntimeException("Task assignments have not been submitted");
        }
        boolean alreadyFinalized = assignments.stream()
                .anyMatch(a -> "FINALIZED".equals(a.getStatus()));
        if (alreadyFinalized) {
            throw new RuntimeException("Task assignments have already been finalized");
        }

        // Update cycle and status
        task.setCycle(cycle);
        task.setStatus("IN_PROGRESS");
        taskMapper.update(task);

        // Finalize assignments
        assignments.forEach(a -> {
            a.setStatus("FINALIZED");
            taskAssignmentMapper.update(a);
        });

        // Create MemberWeeklyGoal entries
        assignments.forEach(a -> {
            for (int weekNo = 1; weekNo <= cycle; weekNo++) {
                MemberWeeklyGoal goal = new MemberWeeklyGoal();
                goal.setTaskId(taskId);
                goal.setStudentId(a.getUserId());
                goal.setWeekNo(weekNo);
                goal.setGoal("");
                goal.setStatus("NOTUPLOADED");
                memberWeeklyGoalMapper.insert(goal);
            }
        });

        // Create Meeting entries
        for (int i = 1; i <= cycle; i++) {
            demo.pojo.Meeting m = new demo.pojo.Meeting();
            m.setGroupId(task.getGroupId());
            m.setTaskId(taskId);
            m.setMeetingNo(i);
            m.setMeetingDate(java.time.LocalDate.now().plusWeeks(i - 1));
            m.setStatus("UNFINISHED");
            meetingMapper.insert(m);
        }

        log.info("Task assignment confirmed for taskId={}, cycle={}", taskId, cycle);
    }

    @Override
    public List<demo.pojo.Meeting> getTaskMeetings(Long userId, Long taskId) {
        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }
        return meetingMapper.findByTaskId(taskId);
    }

    @Override
    public List<MemberWeeklyGoal> getStudentWeeklyGoals(Long userId, Long taskId) {
        // Validate task and permission
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task does not exist");
        }
        List<Long> userGroupIds = ugMapper.findGroupIdsByUserId(userId);
        if (!userGroupIds.contains(task.getGroupId())) {
            throw new RuntimeException("User does not belong to this group");
        }
        return memberWeeklyGoalMapper.findByTaskAndStudent(taskId, userId);
    }

    @Override
    public String getUserNameById(Long userId) {
        User user = userMapper.findById(userId);
        return user != null ? user.getName() : "Unknown User";
    }

    @Override
    public void updateWeeklyGoal(Long goalId, String goal) {
        MemberWeeklyGoal weeklyGoal = memberWeeklyGoalMapper.findById(goalId);
        if (weeklyGoal == null) {
            throw new RuntimeException("Weekly goal not found");
        }
        weeklyGoal.setGoal(goal);
        weeklyGoal.setStatus("PROCESSING");
        memberWeeklyGoalMapper.update(weeklyGoal);
    }

    @Override
    public demo.pojo.Meeting getMeetingByTaskAndNo(Long taskId,
                                                   Long groupId,
                                                   Integer meetingNo) {
        return meetingMapper.findByTaskIdAndMeetingNo(taskId, meetingNo);
    }

    @Override
    public void saveWeeklyGoal(MemberWeeklyGoal goal) {
        List<MemberWeeklyGoal> existing =
                memberWeeklyGoalMapper.findByTaskAndStudent(goal.getTaskId(), goal.getStudentId());
        boolean found = false;
        for (MemberWeeklyGoal g : existing) {
            if (g.getWeekNo().equals(goal.getWeekNo())) {
                g.setGoal(goal.getGoal());
                g.setStatus(goal.getStatus());
                memberWeeklyGoalMapper.update(g);
                found = true;
                break;
            }
        }
        if (!found) {
            memberWeeklyGoalMapper.insert(goal);
        }
    }
}
