package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Response.ApplicationResponse;
import com.shipping.auth_service.DTO.Response.JobResponse;
import com.shipping.auth_service.Security.UserPrinciple;
import com.shipping.auth_service.Service.impl.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {
    private final CareerService service;

    @GetMapping("/jobs")
    public List<JobResponse> jobs() { return service.openJobs(); }

    @PostMapping(value = "/jobs/{id}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApplicationResponse apply(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                     @AuthenticationPrincipal UserPrinciple p) {
        return service.apply(p.userId(), id, file);
    }

    @GetMapping("/my")
    public List<ApplicationResponse> mine(@AuthenticationPrincipal UserPrinciple p) {
        return service.mine(p.userId());
    }
}

