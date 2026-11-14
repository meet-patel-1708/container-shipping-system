package com.shipping.auth_service.Controller;


import com.shipping.auth_service.DTO.Request.HireRequest;
import com.shipping.auth_service.DTO.Request.JobRequest;
import com.shipping.auth_service.DTO.Request.JobStatusRequest;
import com.shipping.auth_service.DTO.Request.JoiningDateRequest;
import com.shipping.auth_service.DTO.Response.ApplicationResponse;
import com.shipping.auth_service.DTO.Response.JobResponse;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.Service.impl.CareerService;
import com.shipping.auth_service.entity.ApplicantStatus;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/careers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCareerController {
    private final CareerService service;

    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest b) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createJob(b));
    }
    @GetMapping("/jobs")
    public List<JobResponse> jobs() { return service.allJobs(); }

    @PutMapping("/jobs/{id}/status")
    public JobResponse status(@PathVariable Long id, @Valid @RequestBody JobStatusRequest b) {
        return service.setJobStatus(id, b.status());
    }
    @GetMapping("/applications")
    public PagedResponse<ApplicationResponse> list(@RequestParam(required = false) ApplicantStatus status,
                                                   @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(status, pageable);
    }
    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<Resource> resume(@PathVariable Long id) {
        CareerService.Resume r = service.resume(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(r.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(r.fileName()).build().toString())
                .header("X-Content-Type-Options", "nosniff").body(r.resource());
    }
    @PostMapping("/applications/{id}/interview")
    public ApplicationResponse interview(@PathVariable Long id) { return service.interview(id); }

    @PostMapping("/applications/{id}/hire")
    public ApplicationResponse hire(@PathVariable Long id, @Valid @RequestBody HireRequest b) {
        return service.hire(id, b);
    }
    @PostMapping("/applications/{id}/reject")
    public ApplicationResponse reject(@PathVariable Long id) { return service.reject(id); }
    @PostMapping("/applications/{id}/joining-date")
    public ApplicationResponse joiningDate(@PathVariable Long id, @Valid @RequestBody JoiningDateRequest b) {
        return service.setJoiningDate(id, b);
    }
}
