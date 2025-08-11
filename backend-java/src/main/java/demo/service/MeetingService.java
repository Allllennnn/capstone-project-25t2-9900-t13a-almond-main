package demo.service;

import demo.pojo.Meeting;
import java.util.List;
import java.util.Map;

/**
 * Meeting service interface
 */
public interface MeetingService {

    /**
     * Retrieve all meetings for a given task
     *
     * @param taskId ID of the task
     * @return list of Meeting objects
     */
    List<Meeting> getMeetingsByTaskId(Long taskId);

    /**
     * Upload a meeting document
     *
     * @param userId      ID of the user uploading
     * @param taskId      ID of the task
     * @param meetingNo   the meeting number
     * @param documentUrl URL of the meeting document
     * @return the created or updated Meeting
     */
    Meeting uploadMeetingDocument(Long userId, Long taskId, Integer meetingNo, String documentUrl);

    /**
     * Check if the specified meeting can be uploaded
     * (previous meetings must be completed)
     *
     * @param taskId    ID of the task
     * @param meetingNo the meeting number to check
     * @return a map containing "canUpload" (boolean) and "message" (String)
     */
    Map<String, Object> checkCanUploadMeeting(Long taskId, Integer meetingNo);

    /**
     * Check the weekly goals status of all group members
     *
     * @param taskId ID of the task
     * @param weekNo the week number
     * @return a map containing status details
     */
    Map<String, Object> checkGroupMembersWeeklyGoalsStatus(Long taskId, Integer weekNo);

    /**
     * Complete a meeting (set its status to COMPLETED)
     *
     * @param meetingId ID of the meeting to complete
     */
    void completeMeeting(Long meetingId);

    /**
     * Retrieve the details of a specific meeting
     *
     * @param meetingId ID of the meeting
     * @return the Meeting object
     */
    Meeting getMeetingById(Long meetingId);

    /**
     * Create a new meeting record
     *
     * @param groupId   ID of the group
     * @param taskId    ID of the task
     * @param meetingNo the meeting number
     * @return the newly created Meeting
     */
    Meeting createMeeting(Long groupId, Long taskId, Integer meetingNo);
}
