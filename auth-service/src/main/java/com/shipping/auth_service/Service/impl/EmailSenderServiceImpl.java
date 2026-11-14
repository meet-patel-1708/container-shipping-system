package com.shipping.auth_service.Service.impl;

import com.shipping.auth_service.Service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(
            String to,
            String name,
            String verificationLink
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);

        message.setSubject(
                "Verify your Container Shipping System account"
        );

        message.setText(
                "Hello " + name + ",\n\n"
                        + "Thank you for registering with Container Shipping System.\n\n"
                        + "Please verify your email address by clicking the link below:\n\n"
                        + verificationLink + "\n\n"
                        + "This verification link will expire in 15 minutes.\n\n"
                        + "If you did not create this account, please ignore this email.\n\n"
                        + "Regards,\n"
                        + "Container Shipping System"
        );

        mailSender.send(message);
    }
    @Override
    public void sendStaffInviteEmail(String to, String name, String roleLabel, String link) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(to);
        m.setSubject("You have been invited to Container Shipping System");
        m.setText("Hello " + name + ",\n\n"
                + "You have been invited to join Container Shipping System as " + roleLabel + ".\n\n"
                + "Set your password using this link (valid for 48 hours):\n\n"
                + link + "\n\n"
                + "If you were not expecting this, ignore this email.\n\n"
                + "Regards,\nContainer Shipping System");
        mailSender.send(m);
    }
    @Override
    public void sendInterviewEmail(String to, String name, String jobTitle) {
        send(to, "You are shortlisted for an interview",
                "Hello " + name + ",\n\nCongratulations! You are shortlisted for an interview for the position of "
                        + jobTitle + ". Your interview will be conducted soon and we will contact you with the details.");
    }
    @Override
    public void sendHiredEmail(String to, String name, String jobTitle, String portName) {
        send(to, "Congratulations, you got the job",
                "Hello " + name + ",\n\nCongratulations! You are selected as " + jobTitle + ". Your onboarding place is "
                        + portName + ".\n\nWe will email you your joining date soon. Your staff account will be "
                        + "activated from that date.");
    }
    private void send(String to, String subject, String body) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(to);
        m.setSubject(subject + " - Container Shipping System");
        m.setText(body + "\n\nRegards,\nContainer Shipping System");
        mailSender.send(m);
    }
    @Override
    public void sendJoiningDateEmail(String to, String name, String jobTitle, String portName, java.time.LocalDate joiningDate) {
        String date = joiningDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        send(to, "Your joining date",
                "Hello " + name + ",\n\nYour joining date for the position of " + jobTitle + " is " + date
                        + ". Please report to " + portName + " on that day.\n\nYour staff account will be activated "
                        + "automatically on " + date + ". Please log out and log in again on or after that day, "
                        + "then upload your documents in Profile so an admin can verify them.");
    }
    @Override
    public void sendNoticeEmail(String to, String name, LocalDate lastDay, int days) {
        send(to, "Your notice period",
                "Hello " + name + ",\n\nYour resignation has been accepted. Your notice period is " + days
                        + " day(s) and your last working day is " + lastDay + ". You keep full access until then.");
    }
    @Override
    public void sendResignationClosedEmail(String to, String name, String subject, String text) {
        send(to, subject, "Hello " + name + ",\n\n" + text);
    }
    @Override
    public void sendPortTaskEmail(String to, String name, Long bookingId, Long containerId, String date, String kg) {
        send(to, "Goods due at your port",
                "Hello " + name + ",\n\nBooking #" + bookingId + " (" + kg + " kg) is scheduled for " + date
                        + ". The goods must now be ported to container #" + containerId
                        + ". Open Movements, send the customer the check-in code and verify it when the goods arrive.");
    }
}