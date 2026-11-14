package com.shipping.auth_service.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmailSenderService {

    void sendVerificationEmail(
            String to,
            String name,
            String verificationLink
    );
    void sendStaffInviteEmail(String to, String name, String roleLabel, String link);
    void sendInterviewEmail(String to, String name, String jobTitle);
    void sendHiredEmail(String to, String name, String jobTitle, String portName);
    void sendJoiningDateEmail(String to, String name, String jobTitle, String portName, java.time.LocalDate joiningDate);
    void sendNoticeEmail(String to, String name, LocalDate lastDay, int days);
    void sendResignationClosedEmail(String to, String name, String subject, String text);
    void sendPortTaskEmail(String to, String name, Long bookingId, Long containerId, String date, String kg);
}