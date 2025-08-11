package demo.service.impl;

import demo.service.AgentService;
import demo.service.StudentService;
import demo.pojo.Group;
import demo.pojo.Task;
import demo.pojo.TaskAssignment;
import demo.pojo.User;
import demo.pojo.MemberWeeklyGoal;
import demo.mapper.TaskAssignmentMapper;
import demo.mapper.UserMapper;
import demo.mapper.UserGroupMapper;
import demo.mapper.MemberWeeklyGoalMapper;
import demo.utils.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TaskAssignmentMapper taskAssignmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private DocumentParser documentParser;

    @Autowired
    private MemberWeeklyGoalMapper memberWeeklyGoalMapper;

    @Value("${python.backend.url:http://localhost:8000}")
    private String pythonBackendUrl;

    @Override
    public String getInitialAssignmentAdvice(Long taskId, Long userId) {
        try {
            // Retrieve task and group info
            Task task = studentService.getTaskById(taskId);
            Group group = studentService.getGroupById(task.getGroupId());

            // Retrieve existing assignments
            List<TaskAssignment> assignments = taskAssignmentMapper.findByTaskId(taskId);

            // Transform to API format
            List<Map<String, Object>> assignmentList = assignments.stream()
                    .map(a -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("user_id", a.getUserId());
                        map.put("user_name", getUserNameById(a.getUserId()));
                        map.put("description", a.getDescription());
                        return map;
                    })
                    .toList();

            // Parse task file content if available
            String taskFileContent;
            if (task.getFileUrl() != null && !task.getFileUrl().trim().isEmpty()) {
                log.info("Starting to parse task file: {}", task.getFileUrl());
                taskFileContent = documentParser.parseDocumentFromUrl(task.getFileUrl());
                log.info("Completed parsing task file, length: {}", taskFileContent.length());
                log.info("Task file content preview (first 200 chars): {}",
                        taskFileContent.length() > 200
                                ? taskFileContent.substring(0, 200)
                                : taskFileContent);
            } else {
                taskFileContent = task.getDescription() != null
                        ? task.getDescription()
                        : "No task file provided";
                log.warn("Task file URL is empty, using task description instead");
            }

            // Build request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("project_url", task.getFileUrl() != null ? task.getFileUrl() : "Not provided");
            requestBody.put("task_description", task.getDescription());
            requestBody.put("task_file_content", taskFileContent);
            requestBody.put("group_name", group.getName());
            requestBody.put("task_name", task.getTitle());
            requestBody.put("assignments", assignmentList);

            log.info("Prepared initial assignment data to send to Python backend:");
            log.info("  project_url: {}", task.getFileUrl());
            log.info("  task_description length: {}",
                    task.getDescription() != null ? task.getDescription().length() : 0);
            log.info("  task_file_content length: {}", taskFileContent.length());
            log.info("  task_file_content preview: {}",
                    taskFileContent.length() > 200
                            ? taskFileContent.substring(0, 200)
                            : taskFileContent);
            log.info("  group_name: {}", group.getName());
            log.info("  task_name: {}", task.getTitle());
            log.info("  number of assignments: {}", assignmentList.size());

            // Set headers and send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = pythonBackendUrl + "/task-assignment/initial-advice";
            log.info("Sending request to Python backend: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && "success".equals(response.getBody().get("status"))) {
                String advice = (String) response.getBody().get("advice");
                log.info("Received advice from Python backend, length: {}", advice.length());

                // Save user submission and agent response
                studentService.saveConversationMessage(taskId, task.getGroupId(), userId, "USER",
                        "Submit initial division of labor");
                studentService.saveConversationMessage(taskId, task.getGroupId(), null, "AGENT", advice);

                return advice;
            } else {
                String errorMsg = response.getBody() != null
                        ? (String) response.getBody().get("message")
                        : "Unknown error";
                log.error("Python backend returned error: {}", errorMsg);
                return "Unable to get agent advice at this time. Please try again later.";
            }

        } catch (Exception e) {
            log.error("Error calling Python backend for initial advice", e);
            throw new RuntimeException("Error obtaining agent advice: " + e.getMessage());
        }
    }

    @Override
    public String getConfirmationAdvice(Long taskId, Long groupId,
                                        List<Map<String, Object>> originalAssignments,
                                        List<Map<String, Object>> updatedAssignments) {
        try {
            // Retrieve task and group info
            Task task = studentService.getTaskById(taskId);
            Group group = studentService.getGroupById(groupId);

            // Build request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("project_url", task.getFileUrl() != null ? task.getFileUrl() : "Not provided");
            requestBody.put("task_description", task.getDescription());
            requestBody.put("group_name", group.getName());
            requestBody.put("task_name", task.getTitle());
            requestBody.put("original_assignments", originalAssignments);
            requestBody.put("updated_assignments", updatedAssignments);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = pythonBackendUrl + "/task-assignment/confirmation-advice";
            log.info("Sending confirmation request to Python backend: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && "success".equals(response.getBody().get("status"))) {
                String advice = (String) response.getBody().get("advice");
                addAgentMessageToConversation(taskId, groupId, advice);
                return advice;
            } else {
                return "Unable to get confirmation advice at this time. Please try again later.";
            }

        } catch (Exception e) {
            log.error("Error calling Python backend for confirmation advice", e);
            return "Error obtaining confirmation advice. Please try again later.";
        }
    }

    @Override
    public String sendMessageToConversation(Long taskId, Long userId, String message) {
        try {
            Task task = studentService.getTaskById(taskId);
            Group group = studentService.getGroupById(task.getGroupId());
            List<Map<String, Object>> history = studentService.getConversationHistory(userId, taskId, group.getId());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_message", message);
            requestBody.put("task_id", taskId);
            requestBody.put("group_id", task.getGroupId());
            requestBody.put("task_name", task.getTitle());
            requestBody.put("task_description", task.getDescription());
            requestBody.put("group_name", group.getName());
            requestBody.put("conversation_history", history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = pythonBackendUrl + "/conversation/chat";
            log.info("Sending chat message to Python backend: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && "success".equals(response.getBody().get("status"))) {
                return (String) response.getBody().get("response");
            } else {
                return "Sorry, I can't process your message right now. Please try again later.";
            }

        } catch (Exception e) {
            log.error("Error sending message to conversation", e);
            return "Error processing your message. Please try again later.";
        }
    }

    @Override
    public List<Map<String, Object>> getConversationHistory(Long taskId, Long groupId) {
        try {
            String url = pythonBackendUrl + "/conversation/history?task_id=" + taskId + "&group_id=" + groupId;
            log.info("Fetching conversation history from Python backend: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() != null && "success".equals(response.getBody().get("status"))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) response.getBody().get("messages");
                return messages;
            }
            return List.of();

        } catch (Exception e) {
            log.error("Error getting conversation history", e);
            return List.of();
        }
    }

    /** Helper to add agent message to conversation history */
    private void addAgentMessageToConversation(Long taskId, Long groupId, String content) {
        try {
            studentService.saveConversationMessage(taskId, groupId, null, "AGENT", content);
        } catch (Exception e) {
            log.error("Error saving agent message to conversation history", e);
        }
    }

    private String getUserNameById(Long userId) {
        return studentService.getUserNameById(userId);
    }

    @Override
    public String analyzeWeeklyProgress(Long taskId, Integer weekNo, String meetingDocumentUrl, Long userId) {
        log.info("Analyzing weekly progress for task {} week {}, document URL: {}", taskId, weekNo, meetingDocumentUrl);
        log.info("==================================================");

        try {
            Long groupId = userGroupMapper.findGroupIdByTaskId(taskId);
            if (groupId == null) {
                return "Unable to find group for the specified task";
            }

            String userName = getUserNameById(userId);
            String requestMsg = String.format("%s requested Agent suggestions for week %d", userName, weekNo);
            studentService.saveConversationMessage(taskId, groupId, userId, "USER", requestMsg);

            String taskContent = getTaskContent(taskId);
            String assignments = getTaskAssignments(taskId);
            String weeklyGoals = getWeeklyGoals(taskId, weekNo);

            String meetingContent = "";
            if (meetingDocumentUrl != null && !meetingDocumentUrl.trim().isEmpty()) {
                log.info("Starting to parse meeting document: {}", meetingDocumentUrl);
                meetingContent = documentParser.parseDocumentFromUrl(meetingDocumentUrl);
                log.info("Completed parsing meeting document, length: {}", meetingContent.length());
                log.info("Meeting document preview: {}",
                        meetingContent.length() > 200
                                ? meetingContent.substring(0, 200)
                                : meetingContent);
            } else {
                meetingContent = "No meeting document provided";
                log.warn("Meeting document URL is empty");
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("task_id", taskId);
            requestData.put("week_no", weekNo);
            requestData.put("meeting_document_url", meetingDocumentUrl);
            requestData.put("meeting_document_content", meetingContent);
            requestData.put("task_content", taskContent);
            requestData.put("initial_assignments", assignments);
            requestData.put("weekly_goals", weeklyGoals);

            log.info("Prepared data to send to Python backend for weekly analysis:");
            log.info("  task_id: {}", taskId);
            log.info("  week_no: {}", weekNo);
            log.info("  meeting_document_url: {}", meetingDocumentUrl);
            log.info("  meeting_document_content length: {}", meetingContent.length());
            log.info("  task_content length: {}", taskContent.length());
            log.info("  initial_assignments: {}", assignments);
            log.info("  weekly_goals: {}", weeklyGoals);
            log.info("==================================================");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            String url = pythonBackendUrl + "/agent/analyze-weekly-progress";
            log.info("Sending weekly progress analysis request to Python backend: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && "success".equals(response.getBody().get("status"))) {
                String analysis = (String) response.getBody().get("analysis");
                log.info("Received analysis result, length: {}", analysis.length());
                log.info("Analysis preview: {}",
                        analysis.length() > 200
                                ? analysis.substring(0, 200)
                                : analysis);

                studentService.saveConversationMessage(taskId, groupId, null, "AGENT", analysis);
                return analysis;
            } else {
                String errorMsg = response.getBody() != null
                        ? (String) response.getBody().get("message")
                        : "Unknown error";
                log.error("Python backend returned error: {}", errorMsg);
                return "Failed to analyze weekly progress: " + errorMsg;
            }

        } catch (Exception e) {
            log.error("Error analyzing weekly progress", e);
            return "Error analyzing weekly progress. Please try again later.";
        }
    }

    @Override
    public Map<String, String> generateWeeklyGoal(Long taskId, Long studentId, Integer weekNo) {
        log.info("Generating weekly goal for task {} student {} week {}", taskId, studentId, weekNo);
        log.info("==================================================");

        try {
            Task task = studentService.getTaskById(taskId);
            if (task == null) {
                throw new RuntimeException("Task not found");
            }

            Group group = studentService.getGroupById(task.getGroupId());
            if (group == null) {
                throw new RuntimeException("Group not found");
            }

            List<TaskAssignment> assignments = taskAssignmentMapper.findByTaskId(taskId);
            List<Map<String, Object>> assignmentList = assignments.stream()
                    .map(a -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("user_id", a.getUserId());
                        map.put("user_name", getUserNameById(a.getUserId()));
                        map.put("description", a.getDescription());
                        return map;
                    })
                    .toList();

            String taskFileContent;
            if (task.getFileUrl() != null && !task.getFileUrl().trim().isEmpty()) {
                log.info("Starting to parse task file: {}", task.getFileUrl());
                taskFileContent = documentParser.parseDocumentFromUrl(task.getFileUrl());
                log.info("Completed parsing task file, length: {}", taskFileContent.length());
                log.info("Task file content preview: {}",
                        taskFileContent.length() > 200
                                ? taskFileContent.substring(0, 200)
                                : taskFileContent);
            } else {
                taskFileContent = task.getDescription() != null
                        ? task.getDescription()
                        : "No task file provided";
                log.warn("Task file URL is empty, using task description");
            }

            List<MemberWeeklyGoal> previousGoals = null;
            if (weekNo > 1) {
                previousGoals = memberWeeklyGoalMapper.findByTaskIdAndStudentId(taskId, studentId);
                log.info("Retrieved {} previous weekly goals",
                        previousGoals != null ? previousGoals.size() : 0);
                if (previousGoals != null) {
                    previousGoals.forEach(g -> log.info(
                            "Previous goal: week={}, status={}, goal={}",
                            g.getWeekNo(),
                            g.getStatus(),
                            g.getGoal() != null && g.getGoal().length() > 50
                                    ? g.getGoal().substring(0,50) + "..."
                                    : g.getGoal()
                    ));
                }
            }

            String previousMeetingContent = null;
            if (weekNo > 1) {
                String prevUrl = getMeetingDocumentUrl(taskId, weekNo - 1);
                if (prevUrl != null && !prevUrl.isEmpty()) {
                    log.info("Parsing previous meeting document: {}", prevUrl);
                    previousMeetingContent = documentParser.parseDocumentFromUrl(prevUrl);
                    log.info("Completed parsing previous meeting doc, length: {}", previousMeetingContent.length());
                    log.info("Previous meeting doc preview: {}",
                            previousMeetingContent.length() > 200
                                    ? previousMeetingContent.substring(0, 200)
                                    : previousMeetingContent);
                }
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task_id", taskId);
            requestBody.put("student_id", studentId);
            requestBody.put("week_no", weekNo);
            requestBody.put("task_content", task.getDescription());
            requestBody.put("task_file_content", taskFileContent);

            StringBuilder assignmentsStr = new StringBuilder();
            assignmentList.forEach(a ->
                    assignmentsStr.append(
                            String.format("- %s: %s\n", a.get("user_name"), a.get("description"))
                    )
            );
            requestBody.put("initial_assignments", assignmentsStr.toString());

            if (previousGoals != null && !previousGoals.isEmpty()) {
                requestBody.put("previous_goals", previousGoals);
            }
            if (previousMeetingContent != null) {
                requestBody.put("previous_meeting_content", previousMeetingContent);
            }

            log.info("Prepared data to send to Python backend for goal generation:");
            log.info("  task_id: {}", taskId);
            log.info("  student_id: {}", studentId);
            log.info("  week_no: {}", weekNo);
            log.info("  task_content length: {}", task.getDescription().length());
            log.info("  task_file_content length: {}", taskFileContent.length());
            log.info("  initial_assignments:\n{}", assignmentsStr);
            log.info("  previous_goals count: {}", previousGoals != null ? previousGoals.size() : 0);
            log.info("  previous_meeting_content length: {}",
                    previousMeetingContent != null ? previousMeetingContent.length() : 0);
            log.info("==================================================");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = pythonBackendUrl + "/weekly-goal/generate";
            log.info("Sending weekly goal generation request to Python backend: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && "success".equals(response.getBody().get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, String> weeklyGoal = (Map<String, String>) response.getBody().get("weekly_goal");
                log.info("Received weekly goal from Python backend: {}", weeklyGoal);
                return weeklyGoal;
            } else {
                String errorMsg = response.getBody() != null
                        ? (String) response.getBody().get("message")
                        : "Unknown error";
                log.error("Python backend returned error: {}", errorMsg);
                throw new RuntimeException("Weekly goal generation failed: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("Error generating weekly goal", e);
            throw new RuntimeException("Weekly goal generation failed: " + e.getMessage());
        }
    }

    /** TODO: Implement method to retrieve the meeting document URL for a specific week */
    private String getMeetingDocumentUrl(Long taskId, Integer weekNo) {
        return null;
    }

    /** Helper to get task description or error */
    private String getTaskContent(Long taskId) {
        try {
            Task task = studentService.getTaskById(taskId);
            if (task != null) {
                return task.getDescription() != null ? task.getDescription() : "No task description";
            }
            return "Unable to retrieve task information";
        } catch (Exception e) {
            log.error("Error getting task content", e);
            return "Failed to get task content";
        }
    }

    /** Helper to get formatted assignments */
    private String getTaskAssignments(Long taskId) {
        try {
            List<TaskAssignment> assignments = taskAssignmentMapper.findByTaskId(taskId);
            if (assignments.isEmpty()) {
                return "No assignment information available";
            }
            StringBuilder sb = new StringBuilder();
            for (TaskAssignment a : assignments) {
                String userName = getUserNameById(a.getUserId());
                sb.append(String.format("Member: %s, Task: %s\n", userName, a.getDescription()));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error getting task assignments", e);
            return "Failed to retrieve assignment information";
        }
    }

    /** Helper to get formatted weekly goals */
    private String getWeeklyGoals(Long taskId, Integer weekNo) {
        try {
            List<MemberWeeklyGoal> goals = memberWeeklyGoalMapper.findByTaskId(taskId);
            if (goals.isEmpty()) {
                return String.format("No goals set for week %d", weekNo);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Week %d group member goals:\n", weekNo));
            List<MemberWeeklyGoal> currentGoals = goals.stream()
                    .filter(g -> g.getWeekNo().equals(weekNo))
                    .toList();
            if (currentGoals.isEmpty()) {
                sb.append("(No specific goals found for the current week. Showing all set goals for reference):\n");
                for (MemberWeeklyGoal g : goals) {
                    String name = getUserNameById(g.getStudentId());
                    sb.append(String.format("Member: %s, Week %d goal: %s, Status: %s\n",
                            name, g.getWeekNo(), g.getGoal(), getStatusText(g.getStatus())));
                }
            } else {
                for (MemberWeeklyGoal g : currentGoals) {
                    String name = getUserNameById(g.getStudentId());
                    sb.append(String.format("Member: %s, Goal: %s, Status: %s\n",
                            name, g.getGoal(), getStatusText(g.getStatus())));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error getting weekly goals", e);
            return "Failed to retrieve weekly goals: " + e.getMessage();
        }
    }

    /** Helper to convert status code to text */
    private String getStatusText(String status) {
        if (status == null) return "UNKNOWN";
        switch (status.toUpperCase()) {
            case "NOTUPLOADED":
            case "NOT_STARTED":
                return "Not Started";
            case "PROCESSING":
            case "IN_PROGRESS":
                return "In Progress";
            case "FINISHED":
            case "COMPLETED":
                return "Completed";
            default:
                return status;
        }
    }
}
