package demo.controller;

import demo.pojo.Meeting;
import demo.pojo.Result;
import demo.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    /**
     * Retrieve all meetings for a given task
     */
    @GetMapping("/task/{taskId}")
    public Result getMeetingsByTaskId(@RequestAttribute("userId") Long userId,
                                      @PathVariable Long taskId) {
        log.info("User {} fetching meetings for task {}", userId, taskId);
        try {
            List<Meeting> meetings = meetingService.getMeetingsByTaskId(taskId);
            return Result.success(meetings);
        } catch (Exception e) {
            log.error("Failed to fetch meetings list", e);
            return Result.error("Failed to fetch meetings list: " + e.getMessage());
        }
    }

    /**
     * Upload a meeting document
     */
    @PostMapping("/upload")
    public Result uploadMeetingDocument(@RequestAttribute("userId") Long userId,
                                        @RequestBody Map<String, Object> requestData) {
        log.info("User {} uploading meeting document", userId);
        try {
            Long taskId = Long.valueOf(requestData.get("taskId").toString());
            Integer meetingNo = Integer.valueOf(requestData.get("meetingNo").toString());
            String documentUrl = (String) requestData.get("documentUrl");

            Meeting meeting = meetingService.uploadMeetingDocument(userId, taskId, meetingNo, documentUrl);
            return Result.success(meeting);
        } catch (Exception e) {
            log.error("Failed to upload meeting document", e);
            return Result.error("Failed to upload meeting document: " + e.getMessage());
        }
    }

    /**
     * Check if a meeting can be uploaded
     */
    @GetMapping("/check-can-upload/{taskId}/{meetingNo}")
    public Result checkCanUploadMeeting(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long taskId,
                                        @PathVariable Integer meetingNo) {
        log.info("User {} checking upload permission for task {} meeting {}", userId, taskId, meetingNo);
        try {
            Map<String, Object> result = meetingService.checkCanUploadMeeting(taskId, meetingNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to check upload permission", e);
            return Result.error("Failed to check upload permission: " + e.getMessage());
        }
    }

    /**
     * Check weekly goals status for group members
     */
    @GetMapping("/check-goals/{taskId}/{weekNo}")
    public Result checkGroupMembersWeeklyGoalsStatus(@RequestAttribute("userId") Long userId,
                                                     @PathVariable Long taskId,
                                                     @PathVariable Integer weekNo) {
        log.info("User {} checking weekly goals status for task {} week {}", userId, taskId, weekNo);
        try {
            Map<String, Object> result = meetingService.checkGroupMembersWeeklyGoalsStatus(taskId, weekNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to check weekly goals status", e);
            return Result.error("Failed to check weekly goals status: " + e.getMessage());
        }
    }

    /**
     * Mark a meeting as completed
     */
    @PostMapping("/complete/{meetingId}")
    public Result completeMeeting(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long meetingId) {
        log.info("User {} completing meeting {}", userId, meetingId);
        try {
            meetingService.completeMeeting(meetingId);
            return Result.success("Meeting marked as completed");
        } catch (Exception e) {
            log.error("Failed to complete meeting", e);
            return Result.error("Failed to complete meeting: " + e.getMessage());
        }
    }

    /**
     * Retrieve meeting details by ID
     */
    @GetMapping("/{meetingId}")
    public Result getMeetingById(@RequestAttribute("userId") Long userId,
                                 @PathVariable Long meetingId) {
        log.info("User {} fetching details for meeting {}", userId, meetingId);
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);
            if (meeting == null) {
                return Result.error("Meeting not found");
            }
            return Result.success(meeting);
        } catch (Exception e) {
            log.error("Failed to fetch meeting details", e);
            return Result.error("Failed to fetch meeting details: " + e.getMessage());
        }
    }
}
