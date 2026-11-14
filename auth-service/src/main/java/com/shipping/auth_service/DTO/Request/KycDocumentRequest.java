package com.shipping.auth_service.DTO.Request;

import com.shipping.auth_service.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record KycDocumentRequest(
        @NotNull(message = "documentType is required") DocumentType documentType,

        @NotBlank(message = "documentNumber is required")
        @Pattern(regexp = "^[A-Za-z0-9]{8,20}$", message = "Invalid document number")
        String documentNumber
) {}