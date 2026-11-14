package com.shipping.auth_service.Service.impl;

import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.DTO.Response.ResignationResponse;
import com.shipping.auth_service.Repository.ResignationRepository;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.Service.EmailSenderService;
import com.shipping.auth_service.entity.Resignation;
import com.shipping.auth_service.entity.ResignationStatus;
import com.shipping.auth_service.entity.Role;
import com.shipping.auth_service.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.shipping.auth_service.Exception.ApiException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResignationService {
    private static final List<ResignationStatus> OPEN = List.of(ResignationStatus.REQUESTED, ResignationStatus.NOTICE);

    private final ResignationRepository repo;
    private final UserRepository users;
    private final EmailSenderService mail;

    // ---------------------------------------------------------- employee
    @Transactional
    public ResignationResponse request(Long userId, String reason) {
        User u = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() != Role.EMPLOYEE)
            throw new ApiException(HttpStatus.CONFLICT, "Only staff can resign through this form");
        if (repo.findByUser_UserIdAndStatusIn(userId, OPEN).isPresent())
            throw new ApiException(HttpStatus.CONFLICT, "You already have an open resignation");
        return toResponse(repo.save(Resignation.builder().user(u).reason(reason.trim())
                .status(ResignationStatus.REQUESTED).build()));
    }

    @Transactional()
    public ResignationResponse mine(Long userId) {
        return repo.findFirstByUser_UserIdOrderByRequestedAtDesc(userId).map(this::toResponse).orElse(null);
    }

    @Transactional
    public ResignationResponse withdraw(Long userId) {
        Resignation r = repo.findByUser_UserIdAndStatusIn(userId, OPEN)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No open resignation"));
        if (r.getStatus() == ResignationStatus.NOTICE)
            throw new ApiException(HttpStatus.CONFLICT, "Your notice period has started. Ask an admin to cancel it.");
        r.setStatus(ResignationStatus.WITHDRAWN);
        return toResponse(repo.save(r));
    }

    // ------------------------------------------------------------- admin
    @Transactional()
    public PagedResponse<ResignationResponse> list(ResignationStatus status, Pageable pageable) {
        Page<Resignation> p = status == null ? repo.findAll(pageable) : repo.findByStatus(status, pageable);
        return PagedResponse.from(p.map(this::toResponse));
    }

    /** First call starts the notice period; later calls change the length (start date stays fixed). */
    @Transactional
    public ResignationResponse setNotice(Long id, int days) {
        Resignation r = find(id);
        if (r.getStatus() != ResignationStatus.REQUESTED && r.getStatus() != ResignationStatus.NOTICE)
            throw new ApiException(HttpStatus.CONFLICT, "This resignation is already closed");
        if (r.getNoticeStartDate() == null) r.setNoticeStartDate(LocalDate.now());
        r.setNoticeDays(days);
        r.setLastWorkingDate(r.getNoticeStartDate().plusDays(days));
        r.setStatus(ResignationStatus.NOTICE);
        repo.save(r);
        String to = r.getUser().getEmail(), name = r.getUser().getName();
        LocalDate last = r.getLastWorkingDate();
        afterCommit(() -> safe(() -> mail.sendNoticeEmail(to, name, last, days)));
        return toResponse(r);
    }

    @Transactional
    public ResignationResponse reject(Long id) {
        Resignation r = find(id);
        if (r.getStatus() != ResignationStatus.REQUESTED && r.getStatus() != ResignationStatus.NOTICE)
            throw new ApiException(HttpStatus.CONFLICT, "This resignation is already closed");
        r.setStatus(ResignationStatus.REJECTED);
        repo.save(r);
        String to = r.getUser().getEmail(), name = r.getUser().getName();
        afterCommit(() -> safe(() -> mail.sendResignationClosedEmail(to, name, "Resignation update",
                "Your resignation request was not accepted. You continue in your role.")));
        return toResponse(r);
    }

    // --------------------------------------------------- end of notice job
    @Transactional
    public int completeDue() {
        List<Resignation> due = repo.findByStatusAndLastWorkingDateBefore(ResignationStatus.NOTICE, LocalDate.now());
        for (Resignation r : due) {
            User u = r.getUser();
            u.setRole(Role.USER);
            u.setAssignedPortId(null);
            u.setPosting(null);
            users.save(u);
            r.setStatus(ResignationStatus.COMPLETED);
            repo.save(r);
            String to = u.getEmail(), name = u.getName();
            afterCommit(() -> safe(() -> mail.sendResignationClosedEmail(to, name, "Your employment has ended",
                    "Your notice period is complete and your staff access has ended. Thank you for your service. "
                            + "You can still use your customer account.")));
            log.info("Employment ended for userId={}", u.getUserId());
        }
        return due.size();
    }

    // ----------------------------------------------------------- helpers
    private Resignation find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resignation not found"));
    }
    private ResignationResponse toResponse(Resignation r) {
        User u = r.getUser();
        return new ResignationResponse(r.getId(), u.getUserId(), u.getName(), u.getEmail(), u.getPosting(),
                u.getAssignedPortId(), r.getReason(), r.getStatus(), r.getNoticeDays(),
                r.getLastWorkingDate(), r.getRequestedAt());
    }
    private void safe(Runnable r) { try { r.run(); } catch (Exception e) { log.error("Email failed", e); } }
    private static void afterCommit(Runnable r) {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { r.run(); }
            });
        else r.run();
    }
}