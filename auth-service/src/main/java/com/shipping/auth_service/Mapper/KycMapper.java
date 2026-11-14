package com.shipping.auth_service.Mapper;
import com.shipping.auth_service.DTO.Request.KycStatusUpdateRequest;
import com.shipping.auth_service.DTO.Response.KycResponse;
import com.shipping.auth_service.entity.Kyc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class KycMapper {

    private final KycDocumentMapper kycDocumentMapper;

    public KycResponse toResponse(Kyc kyc) {
        if (kyc == null) {
            return null;
        }

        return new KycResponse(
                kyc.getId(),
                kyc.getUser().getUserId(),
                kyc.getStatus(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt()
        );
    }
    public void toUpdate(KycStatusUpdateRequest request, Kyc kyc) {
        if (request == null || kyc == null) {
            return;
        }
        if (request.status() != null) {
            kyc.setStatus(request.status());
        }
    }
}

