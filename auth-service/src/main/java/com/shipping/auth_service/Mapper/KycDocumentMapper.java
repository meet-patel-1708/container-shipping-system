package com.shipping.auth_service.Mapper;

import com.shipping.auth_service.DTO.Request.KycDocumentRequest;
import com.shipping.auth_service.DTO.Response.KycDocumentResponse;
import com.shipping.auth_service.entity.KycDocument;
import org.springframework.stereotype.Component;

@Component
public class KycDocumentMapper {

    public KycDocumentResponse toResponse(KycDocument d) {
        if (d == null) return null;
        return new KycDocumentResponse(d.getId(), d.getDocumentType(),
                mask(d.getDocumentNumber()), d.getCreatedAt());
    }

    private static String mask(String n) {
        if (n == null || n.length() <= 4) return n;
        return "*".repeat(n.length() - 4) + n.substring(n.length() - 4);
    }

    public KycDocument toEntity(KycDocumentRequest request) {
        if (request == null) {
            return null;
        }
        return KycDocument.builder()
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .build();
    }

    public void toUpdate(KycDocumentRequest request, KycDocument document) {
        if (request == null || document == null) {
            return;
        }
        if (request.documentType() != null) {
            document.setDocumentType(request.documentType());
        }
        if (request.documentNumber() != null) {
            document.setDocumentNumber(request.documentNumber());
        }
    }
}

