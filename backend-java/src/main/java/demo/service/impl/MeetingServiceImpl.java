package demo.service.impl;

import demo.mapper.MeetingMapper;
import demo.mapper.MemberWeeklyGoalMapper;
import demo.mapper.UserGroupMapper;
import demo.pojo.Meeting;
import demo.pojo.MemberWeeklyGoal;
import demo.pojo.User;
import demo.service.MeetingService;
import demo.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingMapper meetingMapper;

    @Autowired
    private MemberWeeklyGoalMapper memberWeeklyGoalMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private AgentService agentService;

    @Override
    public List<Meeting> getMeetingsByTaskId(Long taskId) {
        return meetingMapper.findByTaskId(taskId);
    }

    @Override
    @Transactional
    public Meeting uploadMeetingDocument(Long userId, Long taskId, Integer meetingNo, String documentUrl) {
        log.info("User {} uploading document for task {}, meeting {}: {}", userId, taskId, meetingNo, documentUrl);

        // 1. Check if meeting can be uploaded
        Map<String, Object> checkResult = checkCanUploadMeeting(taskId, meetingNo);
        if (!(Boolean) checkResult.get("canUpload")) {
            throw new RuntimeException((String) checkResult.get("message"));
        }

        // 2. Check weekly goals status of group members
        Map<String, Object> goalsCheckResult = checkGroupMembersWeeklyGoalsStatus(taskId, meetingNo);
        if (!(Boolean) goalsCheckResult.get("allProcessing")) {
            throw new RuntimeException((String) goalsCheckResult.get("message"));
        }

        // 3. Find or create meeting record
        Meeting meeting = meetingMapper.findByTaskIdAndMeetingNo(taskId, meetingNo);
        if (meeting == null) {
            // Create a new meeting record
            meeting = new Meeting();
            meeting.setTaskId(taskId);
            meeting.setMeetingNo(meetingNo);
            meeting.setMeetingDate(LocalDate.now());
            meeting.setStatus("UNFINISHED");
            meeting.setDocumentUrl(documentUrl);

            // Get groupId
            List<User> groupMembers = userGroupMapper.findMembersByTaskId(taskId);
            if (!groupMembers.isEmpty()) {
                // Find group ID by task (assuming all members in the same group)
                Long groupId = userGroupMapper.findGroupIdByTaskId(taskId);
                meeting.setGroupId(groupId);
            }

            meetingMapper.insert(meeting);
        } else {
            // Update existing meeting record
            meeting.setDocumentUrl(documentUrl);
            meeting.setStatus("UNFINISHED");
            meetingMapper.update(meeting);
        }

        // 4. Call Agent service for automated analysis
        try {
            agentService.analyzeWeeklyProgress(taskId, meetingNo, documentUrl, userId);
        } catch (Exception e) {
            log.error("Failed to call Agent service", e);
            // Do not impact meeting upload, only log error
        }

        return meeting;
    }

    @Override
    public Map<String, Object> checkCanUploadMeeting(Long taskId, Integer meetingNo) {
        Map<String, Object> result = new HashMap<>();

        log.info("Checking if meeting can be uploaded - taskId: {}, meetingNo: {}", taskId, meetingNo);

        // For the first meeting, allow upload directly
        if (meetingNo <= 1) {
            log.info("First meeting, upload allowed");
            result.put("canUpload", true);
            result.put("message", "Can upload meeting.");
            return result;
        }

        // Check if all previous meetings are completed
        int unfinishedCount = meetingMapper.countUnfinishedPreviousMeetings(taskId, meetingNo);
        log.info("{} unfinished previous meetings found", unfinishedCount);

        if (unfinishedCount > 0) {
            log.warn("There are unfinished previous meetings, upload not allowed");
            result.put("canUpload", false);
            result.put("message", String.format(
                    "Cannot upload Meeting %d. There are %d unfinished previous meetings.",
                    meetingNo, unfinishedCount));
        } else {
            log.info("All previous meetings completed, upload allowed");
            result.put("canUpload", true);
            result.put("message", "Can upload meeting.");
        }

        return result;
    }

    @Override
    public Map<String, Object> checkGroupMembersWeeklyGoalsStatus(Long taskId, Integer weekNo) {
        Map<String, Object> result = new HashMap<>();

        // Retrieve all group members' weekly goals for the current cycle
        List<MemberWeeklyGoal> weeklyGoals =
                memberWeeklyGoalMapper.findByTaskIdAndWeekNo(taskId, weekNo);

        // Find members whose status is not PROCESSING
        List<MemberWeeklyGoal> notProcessingGoals = weeklyGoals.stream()
                .filter(goal -> !"PROCESSING".equals(goal.getStatus()))
                .collect(Collectors.toList());

        if (!notProcessingGoals.isEmpty()) {
            List<String> memberNames = notProcessingGoals.stream()
                    .map(goal -> "Student ID: " + goal.getStudentId() +
                            " (Status: " + goal.getStatus() + ")")
                    .collect(Collectors.toList());

            result.put("allProcessing", false);
            result.put("message", "The following members have not set their weekly goals to PROCESSING: " +
                    String.join(", ", memberNames));
            result.put("notProcessingMembers", notProcessingGoals);
        } else {
            result.put("allProcessing", true);
            result.put("message", "All members have set their weekly goals to PROCESSING.");
        }

        return result;
    }

    @Override
    @Transactional
    public void completeMeeting(Long meetingId) {
        log.info("Completing meeting: {}", meetingId);

        Meeting meeting = meetingMapper.findById(meetingId);
        if (meeting == null) {
            throw new RuntimeException("Meeting not found");
        }

        // 1. Update meeting status to COMPLETED
        meetingMapper.updateStatus(meetingId, "COMPLETED");

        // 2. Set all group members' weekly goals for that cycle to FINISHED
        List<MemberWeeklyGoal> weeklyGoals =
                memberWeeklyGoalMapper.findByTaskIdAndWeekNo(
                        meeting.getTaskId(), meeting.getMeetingNo());
        for (MemberWeeklyGoal goal : weeklyGoals) {
            if ("PROCESSING".equals(goal.getStatus())) {
                memberWeeklyGoalMapper.updateStatus(goal.getId(), "FINISHED");
            }
        }

        log.info("Meeting {} completed, associated weekly goals updated to FINISHED", meetingId);
    }

    @Override
    public Meeting getMeetingById(Long meetingId) {
        return meetingMapper.findById(meetingId);
    }

    @Override
    public Meeting createMeeting(Long groupId, Long taskId, Integer meetingNo) {
        Meeting meeting = new Meeting();
        meeting.setGroupId(groupId);
        meeting.setTaskId(taskId);
        meeting.setMeetingNo(meetingNo);
        meeting.setMeetingDate(LocalDate.now());
        meeting.setStatus("UNFINISHED");

        meetingMapper.insert(meeting);
        return meeting;
    }
}
