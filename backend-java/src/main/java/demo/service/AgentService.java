package demo.service;

import java.util.List;
import java.util.Map;

/**
 * Agent service interface
 */
public interface AgentService {

    /**
     * Get initial assignment advice for a task
     *
     * @param taskId ID of the task
     * @param userId ID of the requesting user
     * @return advice string
     */
    String getInitialAssignmentAdvice(Long taskId, Long userId);

    /**
     * Get confirmation advice after assignments have been updated
     *
     * @param taskId               ID of the task
     * @param groupId              ID of the group
     * @param originalAssignments  list of original assignment maps
     * @param updatedAssignments   list of updated assignment maps
     * @return advice string
     */
    String getConfirmationAdvice(Long taskId,
                                 Long groupId,
                                 List<Map<String, Object>> originalAssignments,
                                 List<Map<String, Object>> updatedAssignments);

    /**
     * Send a message into the conversation
     *
     * @param taskId  ID of the task
     * @param userId  ID of the sending user
     * @param message content to send
     * @return agent's response
     */
    String sendMessageToConversation(Long taskId, Long userId, String message);

    /**
     * Get the conversation history for a task and group
     *
     * @param taskId  ID of the task
     * @param groupId ID of the group
     * @return list of message maps
     */
    List<Map<String, Object>> getConversationHistory(Long taskId, Long groupId);

    /**
     * Analyze weekly progress based on a meeting document
     *
     * @param taskId             ID of the task
     * @param weekNo             week number
     * @param meetingDocumentUrl URL of the meeting document to analyze
     * @param userId             ID of the requesting user
     * @return analysis result string
     */
    String analyzeWeeklyProgress(Long taskId, Integer weekNo, String meetingDocumentUrl, Long userId);

    /**
     * Generate a weekly goal for a student
     *
     * @param taskId    ID of the task
     * @param studentId ID of the student
     * @param weekNo    week number
     * @return map containing the generated weekly goal
     */
    Map<String, String> generateWeeklyGoal(Long taskId, Long studentId, Integer weekNo);
}
