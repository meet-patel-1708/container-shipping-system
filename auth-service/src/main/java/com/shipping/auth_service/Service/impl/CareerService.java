package com.shipping.auth_service.Service.impl;

import com.shipping.auth_service.DTO.Request.HireRequest;
import com.shipping.auth_service.DTO.Request.JobRequest;
import com.shipping.auth_service.DTO.Request.JoiningDateRequest;
import com.shipping.auth_service.DTO.Response.ApplicationResponse;
import com.shipping.auth_service.DTO.Response.JobResponse;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.Repository.JobApplicationRepository;
import com.shipping.auth_service.Repository.JobPostingRepository;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.Service.EmailSenderService;
import com.shipping.auth_service.Service.FileStorageService;
import com.shipping.auth_service.entity.*;
import org.springframework.core.io.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.shipping.auth_service.Exception.ApiException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerService {
    private final JobPostingRepository jobs;
    private final JobApplicationRepository applications;
    private final UserRepository users;
    private final FileStorageService storage;
    private final EmailSenderService mail;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    public record Resume(Resource resource, String contentType, String fileName) {}

    // ------------------------------------------------------------ jobs
    @Transactional
    public JobResponse createJob(JobRequest r) {
        return toJob(jobs.save(JobPosting.builder().title(r.title().trim()).posting(r.posting().trim())
                .description(r.description().trim()).portId(r.portId()).status(JobStatus.OPEN).build()));
    }

    @Transactional
    public JobResponse setJobStatus(Long id, JobStatus s) {
        JobPosting j = jobs.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found"));
        j.setStatus(s);
        return toJob(jobs.save(j));
    }

    @Transactional()
    public List<JobResponse> openJobs() {
        return jobs.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN).stream().map(this::toJob).toList();
    }

    @Transactional()
    public List<JobResponse> allJobs() {
        return jobs.findAllByOrderByCreatedAtDesc().stream().map(this::toJob).toList();
    }

    // ------------------------------------------------------------ applicant
    @Transactional
    public ApplicationResponse apply(Long userId, Long jobId, MultipartFile file) {
        JobPosting job = jobs.findById(jobId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found"));
        if (job.getStatus() != JobStatus.OPEN)
            throw new ApiException(HttpStatus.CONFLICT, "This job is closed");
        User user = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.USER) {
            throw new ApiException(HttpStatus.CONFLICT, "Only customer accounts can apply");
        }if (applications.existsByJob_IdAndUser_UserId(jobId, userId))
            throw new ApiException(HttpStatus.CONFLICT, "You have already applied for this job");
        String key = storage.store(file);
        String name = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "resume"));
        return toApp(applications.save(JobApplication.builder().job(job).user(user).resumeKey(key)
                .resumeName(name.length() > 200 ? name.substring(0, 200) : name)
                .contentType(file.getContentType()).status(ApplicantStatus.APPLIED).build()));
    }

    @Transactional()
    public List<ApplicationResponse> mine(Long userId) {
        return applications.findByUser_UserIdOrderByCreatedAtDesc(userId).stream().map(this::toApp).toList();
    }

    // ------------------------------------------------------------ admin
    @Transactional()
    public PagedResponse<ApplicationResponse> list(ApplicantStatus status, Pageable pageable) {
        Page<JobApplication> p = status == null ? applications.findAll(pageable)
                : applications.findByStatus(status, pageable);
        return PagedResponse.from(p.map(this::toApp));
    }

    @Transactional()
    public Resume resume(Long id) {
        JobApplication a = find(id);
        return new Resume((Resource) storage.load(a.getResumeKey()), a.getContentType(), a.getResumeName());
    }

    @Transactional
    public ApplicationResponse interview(Long id) {
        JobApplication a = find(id);
        if (a.getStatus() != ApplicantStatus.APPLIED)
            throw new ApiException(HttpStatus.CONFLICT, "Only new applications can be shortlisted");
        a.setStatus(ApplicantStatus.SHORTLISTED);
        applications.save(a);
        String to = a.getUser().getEmail(), name = a.getUser().getName(), title = a.getJob().getTitle();
        afterCommit(() -> { try { mail.sendInterviewEmail(to, name, title); }
        catch (Exception e) { log.error("Interview email failed for {}", to, e); } });
        return toApp(a);
    }

    @Transactional
    public ApplicationResponse hire(Long id, HireRequest r) {
        JobApplication a = find(id);
        if (a.getStatus() != ApplicantStatus.SHORTLISTED)
            throw new ApiException(HttpStatus.CONFLICT, "Take the interview step first");
        User u = a.getUser();
        if (u.getRole() != Role.USER)
            throw new ApiException(HttpStatus.CONFLICT, "Only customer accounts can be hired");
        a.setStatus(ApplicantStatus.HIRED);
        a.setOnboardingPortId(r.portId());
        applications.save(a);
        String to = u.getEmail(), name = u.getName(), title = a.getJob().getTitle(), port = r.portName().trim();
        afterCommit(() -> { try { mail.sendHiredEmail(to, name, title, port); }
        catch (Exception e) { log.error("Hired email failed for {}", to, e); } });
        return toApp(a);
    }
    @Transactional
    public ApplicationResponse setJoiningDate(Long id, JoiningDateRequest r) {
        JobApplication a = find(id);
        if (a.getStatus() != ApplicantStatus.HIRED)
            throw new ApiException(HttpStatus.CONFLICT, "Only hired applicants can get a joining date");
        LocalDate today = LocalDate.now(IST);
        if (r.joiningDate().isBefore(today))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Joining date cannot be in the past");
        a.setJoiningDate(r.joiningDate());
        a.setOnboardingPortId(r.onboardingPortId());
        applications.save(a);

        User u = a.getUser();
        if (u.getRole() == Role.EMPLOYEE) {            // already staff (hired before this change): keep port in sync
            u.setAssignedPortId(r.onboardingPortId());
            users.save(u);
        } else if (!r.joiningDate().isAfter(today)) {  // joining today -> activate right now
            activate(a);
        }

        String to = u.getEmail(), name = u.getName(), title = a.getJob().getTitle(), port = r.portName().trim();
        LocalDate date = r.joiningDate();
        afterCommit(() -> { try { mail.sendJoiningDateEmail(to, name, title, port, date); }
        catch (Exception e) { log.error("Joining date email failed for {}", to, e); } });
        return toApp(a);
    }
    @Transactional
    public ApplicationResponse reject(Long id) {
        JobApplication a = find(id);
        if (a.getStatus() == ApplicantStatus.HIRED)
            throw new ApiException(HttpStatus.CONFLICT, "Already hired");
        a.setStatus(ApplicantStatus.REJECTED);
        return toApp(applications.save(a));
    }

    // ------------------------------------------------------------ helpers
    private JobApplication find(Long id) {
        return applications.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found"));
    }
    private JobResponse toJob(JobPosting j) {
        return new JobResponse(j.getId(), j.getTitle(), j.getPosting(), j.getDescription(), j.getPortId(),
                j.getStatus(), j.getCreatedAt());
    }
    private ApplicationResponse toApp(JobApplication a) {
        User u = a.getUser();
        return new ApplicationResponse(a.getId(), a.getJob().getId(), a.getJob().getTitle(), a.getJob().getPosting(),
                u.getUserId(), u.getName(), u.getEmail(), u.getPhone(), a.getResumeName(), a.getStatus(),
                a.getOnboardingPortId(), a.getJoiningDate(),a.getCreatedAt());
    }
    private static void afterCommit(Runnable r) {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { r.run(); }
            });
        else r.run();
    }
    /** Called by the scheduler: promote every HIRED applicant whose joining date has arrived. */
    @Transactional
    public int activateDueStaff() {
        int n = 0;
        for (JobApplication a : applications.findByStatusAndJoiningDateLessThanEqual(
                ApplicantStatus.HIRED, LocalDate.now(IST))) {
            if (activate(a)) n++;
        }
        return n;
    }

    private boolean activate(JobApplication a) {
        User u = a.getUser();
        if (u.getRole() != Role.USER) return false;    // already staff / admin
        u.setRole(Role.EMPLOYEE);
        u.setAssignedPortId(a.getOnboardingPortId());
        u.setPosting(a.getJob().getPosting());
        users.save(u);
        log.info("Staff account activated for user {}", u.getUserId());
        return true;
    }
}
