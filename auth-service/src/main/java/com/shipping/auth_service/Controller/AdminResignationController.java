package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.NoticeRequest;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.DTO.Response.ResignationResponse;
import com.shipping.auth_service.Service.impl.ResignationService;
import com.shipping.auth_service.entity.ResignationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/resignations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminResignationController {
    private final ResignationService service;

    @GetMapping
    public PagedResponse<ResignationResponse> list(@RequestParam(required = false) ResignationStatus status,
                                                   @PageableDefault(size = 50, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(status, pageable);
    }
    @PutMapping("/{id}/notice")
    public ResignationResponse notice(@PathVariable Long id, @Valid @RequestBody NoticeRequest b) {
        return service.setNotice(id, b.noticeDays());
    }
    @PostMapping("/{id}/reject")
    public ResignationResponse reject(@PathVariable Long id) { return service.reject(id); }
}