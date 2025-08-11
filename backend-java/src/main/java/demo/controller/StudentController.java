package demo.controller;

import demo.pojo.Result;
import demo.pojo.PageBean;
import demo.pojo.User;
import demo.pojo.Task;
import demo.service.StudentService;
import demo.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import demo.pojo.MemberWeeklyGoal;
import demo.pojo.Meeting;
import demo.service.MemberWeeklyGoalService;

@Slf4j
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private MemberWeeklyGoalService memberWeeklyGoalService;

    /** Paginate students */
    @GetMapping
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       String name,
                       Long id) {
        log.info("Student pagination request - page={}, pageSize={}, name={}, id={}", page, pageSize, name, id);
        PageBean<User> pageBean = studentService.page(page, pageSize, name, id);
        return Result.success(pageBean);
    }

    /** View own group and its members */
    @GetMapping("/my-group")
    public Result myGroup(@RequestAttribute("userId") Long userId) {
        log.info("Student {} viewing own group and members", userId);
        return Result.success(studentService.getMyGroupWithMembers(userId));
    }

    /** Leave own group */
    @PostMapping("/groups/{groupId}/leave")
    public Result leaveGroup(@RequestAttribute("userId") Long userId,
                             @PathVariable Long groupId) {
        log.info("Student {} leaving group {}", userId, groupId);
        studentService.leaveGroup(userId, groupId);
        return Result.success("Successfully left the group");
    }

    /** Get all groups the student participates in */
    @GetMapping("/groups")
    public Result getStudentGroups(@RequestAttribute("userId") Long userId) {
        log.info("Student {} fetching all joined groups", userId);
        return Result.success(studentService.getStudentGroups(userId));
    }

    /** Get task list for a specific group */
    @GetMapping("/groups/{groupId}/tasks")
    public Result getGroupTasks(@RequestAttribute("userId") Long userId,
                                @PathVariable Long groupId) {
        log.info("Student {} fetching tasks for group {}", userId, groupId);
        return Result.success(studentService.getGroupTasks(userId, groupId));
    }

    /** Get member list for a specific group */
    @GetMapping("/groups/{groupId}/members")
    public Result getGroupMembers(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long groupId) {
        log.info("Student {} fetching members for group {}", userId, groupId);
        return Result.success(studentService.getGroupMembers(userId, groupId));
    }

    /** Submit task assignments */
    @PostMapping("/task-assignments")
    public Result submitTaskAssignment(@RequestAttribute("userId") Long userId,
                                       @RequestBody Map<String, Object> assignmentData) {
        log.info("Student {} submitting task assignments", userId);
        studentService.submitTaskAssignment(userId, assignmentData);
        return Result.success("Task assignments submitted successfully");
    }

    /** Get task assignment status */
    @GetMapping("/task-assignments/{taskId}/status")
    public Result getTaskAssignmentStatus(@RequestAttribute("userId") Long userId,
                                          @PathVariable Long taskId) {
        log.info("Student {} fetching assignment status for task {}", userId, taskId);
        return Result.success(studentService.getTaskAssignmentStatus(userId, taskId));
    }

    /** Get agent assignment advice */
    @PostMapping("/agent/advice")
    public Result getAgentAdvice(@RequestAttribute("userId") Long userId,
                                 @RequestBody Map<String, Object> requestData) {
        log.info("Student {} requesting agent assignment advice", userId);
        Long taskId = Long.valueOf(requestData.get("taskId").toString());
        try {
            String advice = agentService.getInitialAssignmentAdvice(taskId, userId);
            return Result.success(Map.of("advice", advice));
        } catch (Exception e) {
            log.error("Failed to get agent advice", e);
            return Result.error("Failed to get agent advice: " + e.getMessage());
        }
    }

    /** Analyze weekly progress */
    @PostMapping("/agent/analyze-weekly-progress")
    public Result analyzeWeeklyProgress(@RequestAttribute("userId") Long userId,
                                        @RequestBody Map<String, Object> requestData) {
        log.info("Student {} requesting weekly progress analysis", userId);
        try {
            Long taskId = Long.valueOf(requestData.get("taskId").toString());
            Integer weekNo = Integer.valueOf(requestData.get("weekNo").toString());
            String documentUrl = (String) requestData.get("meetingDocumentUrl");

            log.info("Analysis parameters - taskId: {}, weekNo: {}, documentUrl: {}", taskId, weekNo, documentUrl);
            String analysis = agentService.analyzeWeeklyProgress(taskId, weekNo, documentUrl, userId);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("Weekly progress analysis failed", e);
            return Result.error("Weekly progress analysis failed: " + e.getMessage());
        }
    }

    /** Check meeting upload requirements */
    @PostMapping("/meetings/check-upload-requirements")
    public Result checkMeetingUploadRequirements(@RequestAttribute("userId") Long userId,
                                                 @RequestBody Map<String, Object> requestData) {
        log.info("Student {} checking meeting upload requirements", userId);
        try {
            Long taskId = Long.valueOf(requestData.get("taskId").toString());
            Integer meetingNo = Integer.valueOf(requestData.get("meetingNo").toString());

            // Condition 1: all members have set weekly goals
            List<MemberWeeklyGoal> weeklyGoals =
                    memberWeeklyGoalService.getGoalsByTaskIdAndWeekNo(taskId, meetingNo);
            Task task = studentService.getTaskById(taskId);
            List<User> groupMembers = studentService.getGroupMembers(userId, task.getGroupId());
            Map<String, Object> goalsCheck = checkWeeklyGoalsStatus(weeklyGoals, groupMembers, meetingNo);

            // Condition 2: previous meeting completed
            Map<String, Object> previousMeetingCheck =
                    checkPreviousMeetingStatus(taskId, task.getGroupId(), meetingNo);

            boolean canUpload = (boolean) goalsCheck.get("allReady")
                    && (boolean) previousMeetingCheck.get("previousCompleted");
            Map<String, Object> response = new HashMap<>();
            response.put("canUpload", canUpload);
            response.put("goalsCheck", goalsCheck);
            response.put("previousMeetingCheck", previousMeetingCheck);

            if (canUpload) {
                response.put("message", "Meeting can be uploaded");
            } else {
                List<String> issues = new ArrayList<>();
                if (!(boolean) goalsCheck.get("allReady")) {
                    issues.add((String) goalsCheck.get("message"));
                }
                if (!(boolean) previousMeetingCheck.get("previousCompleted")) {
                    issues.add((String) previousMeetingCheck.get("message"));
                }
                response.put("message", String.join("; ", issues));
            }
            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to check meeting upload requirements", e);
            return Result.error("Failed to check meeting upload requirements: " + e.getMessage());
        }
    }

    private Map<String, Object> checkWeeklyGoalsStatus(
            List<MemberWeeklyGoal> weeklyGoals,
            List<User> groupMembers,
            Integer meetingNo) {

        Map<String, Object> result = new HashMap<>();
        Map<Long, MemberWeeklyGoal> memberGoalMap = weeklyGoals.stream()
                .collect(Collectors.toMap(MemberWeeklyGoal::getStudentId, goal -> goal));
        List<String> notReadyMembers = new ArrayList<>();

        for (User member : groupMembers) {
            MemberWeeklyGoal goal = memberGoalMap.get(member.getId());
            if (goal == null || "NOTUPLOADED".equals(goal.getStatus())) {
                notReadyMembers.add(
                        member.getName() + " (Status: " +
                                (goal == null ? "NOT_SET" : goal.getStatus()) + ")"
                );
            }
        }

        boolean allReady = notReadyMembers.isEmpty();
        result.put("allReady", allReady);
        if (allReady) {
            result.put("message", "All team members have set their weekly goals to PROCESSING or FINISHED");
        } else {
            result.put("message", "The following members have not set their weekly goals to PROCESSING: "
                    + String.join(", ", notReadyMembers));
        }
        return result;
    }

    private Map<String, Object> checkPreviousMeetingStatus(
            Long taskId, Long groupId, Integer meetingNo) {

        Map<String, Object> result = new HashMap<>();
        if (meetingNo <= 1) {
            result.put("previousCompleted", true);
            result.put("message", "This is the first meeting, no previous meeting to check");
        } else {
            Meeting previousMeeting =
                    studentService.getMeetingByTaskAndNo(taskId, groupId, meetingNo - 1);
            if (previousMeeting == null) {
                result.put("previousCompleted", false);
                result.put("message", "Previous meeting (Meeting " + (meetingNo - 1) + ") does not exist");
            } else if (!"COMPLETED".equals(previousMeeting.getStatus())) {
                result.put("previousCompleted", false);
                result.put("message", "Previous meeting (Meeting " + (meetingNo - 1) + ") is not completed yet");
            } else {
                result.put("previousCompleted", true);
                result.put("message", "Previous meeting (Meeting " + (meetingNo - 1) + ") is completed");
            }
        }
        return result;
    }

    /** Debug: fetch raw conversation history */
    @GetMapping("/conversations/debug")
    public Result debugConversationHistory(@RequestAttribute("userId") Long userId,
                                           @RequestParam Long taskId,
                                           @RequestParam Long groupId) {
        log.info("DEBUG: Student {} fetching raw conversation history for task {} group {}", userId, taskId, groupId);
        try {
            List<Map<String, Object>> messages =
                    studentService.getConversationHistory(userId, taskId, groupId);
            for (int i = 0; i < messages.size(); i++) {
                Map<String, Object> msg = messages.get(i);
                log.info("DEBUG Message {}: sender_id={}, sender_type={}, sender_name={}, content_preview={}",
                        i,
                        msg.get("sender_id"),
                        msg.get("sender_type"),
                        msg.get("sender_name"),
                        msg.get("content") != null
                                ? msg.get("content").toString().substring(0, Math.min(50, msg.get("content").toString().length())) + "..."
                                : "null"
                );
            }
            return Result.success(messages);
        } catch (Exception e) {
            log.error("DEBUG: failed to fetch conversation history", e);
            return Result.error("Failed to fetch conversation history: " + e.getMessage());
        }
    }

    /** Get conversation history */
    @GetMapping("/conversations")
    public Result getConversationHistory(@RequestAttribute("userId") Long userId,
                                         @RequestParam Long taskId,
                                         @RequestParam Long groupId) {
        log.info("Student {} fetching conversation history for task {} group {}", userId, taskId, groupId);
        List<Map<String, Object>> messages = studentService.getConversationHistory(userId, taskId, groupId);
        return Result.success(messages);
    }

    /** Send a message in the conversation */
    @PostMapping("/conversations/messages")
    public Result sendMessage(@RequestAttribute("userId") Long userId,
                              @RequestBody Map<String, Object> messageData) {
        log.info("Student {} sending message to conversation", userId);
        Long taskId = Long.valueOf(messageData.get("taskId").toString());
        String content = (String) messageData.get("content");
        try {
            Task task = studentService.getTaskById(taskId);
            studentService.saveConversationMessage(taskId, task.getGroupId(), userId, "USER", content);
            String response = agentService.sendMessageToConversation(taskId, userId, content);
            studentService.saveConversationMessage(taskId, task.getGroupId(), null, "AGENT", response);
            return Result.success(Map.of("response", response));
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return Result.error("Failed to send message: " + e.getMessage());
        }
    }

    /** Update task assignment */
    @PutMapping("/task-assignments")
    public Result updateTaskAssignment(@RequestAttribute("userId") Long userId,
                                       @RequestBody Map<String, Object> assignmentData) {
        log.info("Student {} updating task assignment", userId);
        studentService.updateTaskAssignment(userId, assignmentData);
        return Result.success("Task assignment updated successfully");
    }

    /** Finalize task assignment */
    @PostMapping("/task-assignments/{taskId}/finalize")
    public Result finalizeTaskAssignment(@RequestAttribute("userId") Long userId,
                                         @PathVariable Long taskId) {
        log.info("Student {} finalizing assignment for task {}", userId, taskId);
        studentService.finalizeTaskAssignment(userId, taskId);
        return Result.success("Task assignment finalized");
    }

    /** Get finalized task assignments */
    @GetMapping("/task-assignments/{taskId}/finalized")
    public Result getFinalizedAssignments(@RequestAttribute("userId") Long userId,
                                          @PathVariable Long taskId) {
        log.info("Student {} fetching finalized assignments for task {}", userId, taskId);
        return Result.success(studentService.getFinalizedAssignments(userId, taskId));
    }

    /** Confirm final assignment and cycle count */
    @PostMapping("/task-assignments/{taskId}/confirm")
    public Result confirmTaskAssignment(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long taskId,
                                        @RequestBody Map<String, Object> request) {
        log.info("Student {} confirming assignment for task {}", userId, taskId);
        try {
            Integer cycle = (Integer) request.get("cycle");
            if (cycle == null || cycle <= 0) {
                return Result.error("Cycle count must be a positive integer");
            }
            studentService.confirmTaskAssignment(userId, taskId, cycle);
            return Result.success("Task assignment confirmed successfully");
        } catch (Exception e) {
            log.error("Failed to confirm task assignment", e);
            return Result.error("Failed to confirm task assignment: " + e.getMessage());
        }
    }

    /** Get meeting records for a task */
    @GetMapping("/tasks/{taskId}/meetings")
    public Result getTaskMeetings(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long taskId) {
        log.info("Student {} fetching meeting records for task {}", userId, taskId);
        try {
            return Result.success(studentService.getTaskMeetings(userId, taskId));
        } catch (Exception e) {
            log.error("Failed to fetch meeting records", e);
            return Result.error("Failed to fetch meeting records: " + e.getMessage());
        }
    }

    /** Get student's weekly goals for a task */
    @GetMapping("/tasks/{taskId}/weekly-goals")
    public Result getStudentWeeklyGoals(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long taskId) {
        log.info("Student {} fetching weekly goals for task {}", userId, taskId);
        try {
            return Result.success(studentService.getStudentWeeklyGoals(userId, taskId));
        } catch (Exception e) {
            log.error("Failed to fetch weekly goals", e);
            return Result.error("Failed to fetch weekly goals: " + e.getMessage());
        }
    }

    /** Get conversation history for a task */
    @GetMapping("/tasks/{taskId}/conversations")
    public Result getTaskConversationHistory(@RequestAttribute("userId") Long userId,
                                             @PathVariable Long taskId) {
        log.info("Student {} fetching conversation history for task {}", userId, taskId);
        try {
            Task task = studentService.getTaskById(taskId);
            List<Map<String, Object>> messages =
                    studentService.getConversationHistory(userId, taskId, task.getGroupId());
            return Result.success(messages);
        } catch (Exception e) {
            log.error("Failed to fetch conversation history", e);
            return Result.error("Failed to fetch conversation history: " + e.getMessage());
        }
    }

    /** Get agent advice for a task */
    @GetMapping("/tasks/{taskId}/agent-advice")
    public Result getTaskAgentAdvice(@RequestAttribute("userId") Long userId,
                                     @PathVariable Long taskId) {
        log.info("Student {} requesting agent advice for task {}", userId, taskId);
        try {
            String advice = agentService.getInitialAssignmentAdvice(taskId, userId);
            return Result.success(Map.of("advice", advice));
        } catch (Exception e) {
            log.error("Failed to get agent advice", e);
            return Result.error("Failed to get agent advice: " + e.getMessage());
        }
    }

    /** AI-generated weekly goals */
    @PostMapping("/tasks/{taskId}/weekly-goals/generate")
    public Result generateWeeklyGoal(@RequestAttribute("userId") Long userId,
                                     @PathVariable("taskId") Long taskId,
                                     @RequestParam("weekNo") Integer weekNo) {
        log.info("Student requesting AI-generated weekly goals - taskId={}, userId={}, weekNo={}",
                taskId, userId, weekNo);
        try {
            Map<String, String> generatedGoal =
                    agentService.generateWeeklyGoal(taskId, userId, weekNo);
            MemberWeeklyGoal goal = new MemberWeeklyGoal();
            goal.setTaskId(taskId);
            goal.setStudentId(userId);
            goal.setWeekNo(weekNo);
            goal.setGoal(generatedGoal.get("goal"));
            goal.setStatus("NOTUPLOADED");
            studentService.saveWeeklyGoal(goal);
            return Result.success(generatedGoal);
        } catch (Exception e) {
            log.error("Failed to generate weekly goal", e);
            return Result.error("Failed to generate weekly goal: " + e.getMessage());
        }
    }

    /** Update a weekly goal */
    @PutMapping("/weekly-goals/{goalId}")
    public Result updateWeeklyGoal(@PathVariable Long goalId, @RequestBody Map<String, String> updateData) {
        try {
            String goal = updateData.get("goal");
            studentService.updateWeeklyGoal(goalId, goal);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update weekly goal: " + e.getMessage());
        }
    }
}
