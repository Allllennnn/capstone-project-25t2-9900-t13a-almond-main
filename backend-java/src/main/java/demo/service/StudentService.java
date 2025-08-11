package demo.service;

import demo.pojo.PageBean;
import demo.pojo.User;
import demo.pojo.Group;
import demo.pojo.Task;
import demo.pojo.TaskAssignment;
import demo.pojo.Meeting;
import demo.pojo.MemberWeeklyGoal;

import java.util.List;
import java.util.Map;

/**
 * Student service interface
 */
public interface StudentService {

    /**
     * Paginate students with optional fuzzy search by name and exact match by ID
     */
    PageBean<User> page(Integer page, Integer pageSize, String name, Long id);

    /** Retrieve the group the student belongs to and its members */
    Map<String, Object> getMyGroupWithMembers(Long userId);

    /** Leave a group */
    void leaveGroup(Long userId, Long groupId);

    /** Get all groups the student is participating in */
    List<Group> getStudentGroups(Long userId);

    /** Get the list of tasks for a specific group */
    List<Task> getGroupTasks(Long userId, Long groupId);

    /** Get the list of members for a specific group */
    List<User> getGroupMembers(Long userId, Long groupId);

    /** Submit task assignments */
    void submitTaskAssignment(Long userId, Map<String, Object> assignmentData);

    /** Get the status of task assignments */
    Map<String, Object> getTaskAssignmentStatus(Long userId, Long taskId);

    /** Retrieve task details (for Agent service use) */
    Task getTaskById(Long taskId);

    /** Retrieve group details (for Agent service use) */
    Group getGroupById(Long groupId);

    /** Update task assignments */
    void updateTaskAssignment(Long userId, Map<String, Object> assignmentData);

    /** Finalize task assignments */
    void finalizeTaskAssignment(Long userId, Long taskId);

    /** Get finalized task assignments */
    List<TaskAssignment> getFinalizedAssignments(Long userId, Long taskId);

    /** Confirm final assignments and set the number of cycles */
    void confirmTaskAssignment(Long userId, Long taskId, Integer cycle);

    /** Get meeting records for a task */
    List<Meeting> getTaskMeetings(Long userId, Long taskId);

    /** Get the student's weekly goals for a task */
    List<MemberWeeklyGoal> getStudentWeeklyGoals(Long userId, Long taskId);

    /** Save a conversation message */
    void saveConversationMessage(Long taskId, Long groupId, Long senderId, String senderType, String content);

    /** Retrieve conversation history for a task and group */
    List<Map<String, Object>> getConversationHistory(Long userId, Long taskId, Long groupId);

    /** Get username by ID (for Agent service use) */
    String getUserNameById(Long userId);

    /**
     * Update a weekly goal
     *
     * @param goalId the ID of the goal to update
     * @param goal   the new goal content
     */
    void updateWeeklyGoal(Long goalId, String goal);

    /** Save or update a weekly goal */
    void saveWeeklyGoal(MemberWeeklyGoal goal);

    /**
     * Get meeting by task ID, group ID and meeting number
     * @param taskId the task ID
     * @param groupId the group ID
     * @param meetingNo the meeting number
     * @return the meeting record or null if not found
     */
    demo.pojo.Meeting getMeetingByTaskAndNo(Long taskId, Long groupId, Integer meetingNo);
}