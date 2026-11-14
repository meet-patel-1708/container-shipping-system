package com.shipping.auth_service.Service;

import com.shipping.auth_service.DTO.Request.AddressRequest;
import com.shipping.auth_service.DTO.Request.KycDocumentRequest;
import com.shipping.auth_service.DTO.Response.*;
import com.shipping.auth_service.Exception.ApiException;
import com.shipping.auth_service.Mapper.AddressMapper;
import com.shipping.auth_service.Mapper.KycDocumentMapper;
import com.shipping.auth_service.Mapper.UserMapper;
import com.shipping.auth_service.Repository.*;
import com.shipping.auth_service.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_ADDRESSES = 5;

    private static final Map<DocumentType, Pattern> FORMAT = Map.of(
            DocumentType.AADHAR, java.util.regex.Pattern.compile("^[2-9][0-9]{11}$"),
            DocumentType.PAN, java.util.regex.Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$"),
            DocumentType.PASSPORT, java.util.regex.Pattern.compile("^[A-PR-WY][0-9]{7}$"),
            DocumentType.DRIVING_LICENSE, java.util.regex.Pattern.compile("^[A-Z]{2}[0-9A-Z]{11,16}$"),
            DocumentType.VOTER_ID, java.util.regex.Pattern.compile("^[A-Z]{3}[0-9]{7}$"));

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final KycRepository kycRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final DriverApplicationRepository driverApplicationRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final KycDocumentMapper kycDocumentMapper;

    // ------------------------------------------------------------ profile

    @Transactional()
    public ProfileResponse profile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<AddressResponse> addresses = addressRepository.findByUser_UserId(userId)
                .stream().map(addressMapper::toResponse).toList();

        KycSummury kyc = kycRepository.findByUser_UserId(userId).map(this::summary).orElse(null);

        DriverSummury driver = driverApplicationRepository.findByUser_UserId(userId)
                .map(a -> new DriverSummury(a.getStatus(), a.getDriverType(), a.getRejectionReason()))
                .orElse(null);

        return new ProfileResponse(userMapper.toResponse(user), addresses, kyc, driver);
    }

    @Transactional()
    public EligibilityResponse eligibility(Long userId) {
        boolean hasAddress = !addressRepository.findByUser_UserId(userId).isEmpty();
        Kyc kyc = kycRepository.findByUser_UserId(userId).orElse(null);
        boolean hasDoc = kyc != null
                && kyc.getStatus() != KycStatus.REJECTED
                && !kycDocumentRepository.findByKyc_Id(kyc.getId()).isEmpty();

        List<String> missing = new ArrayList<>();
        if (!hasAddress) missing.add("ADDRESS");
        if (!hasDoc) missing.add("KYC_DOCUMENT");
        return new EligibilityResponse(missing.isEmpty(), hasAddress, hasDoc,
                kyc == null ? null : kyc.getStatus(), missing);
    }

    // ------------------------------------------------------------ address

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest req) {
        if (addressRepository.findByUser_UserId(userId).size() >= MAX_ADDRESSES)
            throw new ApiException(HttpStatus.CONFLICT, "You can save at most " + MAX_ADDRESSES + " addresses");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Address a = addressMapper.toEntity(req);
        a.setUser(user);
        return addressMapper.toResponse(addressRepository.save(a));
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest req) {
        Address a = ownedAddress(userId, addressId);
        addressMapper.toUpdate(req, a);
        return addressMapper.toResponse(addressRepository.save(a));
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        addressRepository.delete(ownedAddress(userId, addressId));
    }

    // ---------------------------------------------------------------- kyc

    @Transactional
    public KycSummury addKycDocument(Long userId, KycDocumentRequest req) {
        String number = req.documentNumber().trim().toUpperCase();
        if (!FORMAT.get(req.documentType()).matcher(number).matches())
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Invalid " + req.documentType() + " number format");

        Kyc kyc = kycRepository.findByUser_UserId(userId).orElseGet(() -> kycRepository.save(
                Kyc.builder().user(userRepository.getReferenceById(userId))
                        .status(KycStatus.PENDING).build()));

        // the number must not belong to anyone else (or to another document of mine)
        kycDocumentRepository.findByDocumentNumber(number).ifPresent(existing -> {
            if (!existing.getKyc().getId().equals(kyc.getId())
                    || existing.getDocumentType() != req.documentType())
                throw new ApiException(HttpStatus.CONFLICT, "This document number is already registered");
        });

        // one document per type: replace it if it exists
        KycDocument doc = kycDocumentRepository.findByKyc_Id(kyc.getId()).stream()
                .filter(d -> d.getDocumentType() == req.documentType())
                .findFirst()
                .orElseGet(() -> KycDocument.builder().kyc(kyc).documentType(req.documentType()).build());
        doc.setDocumentNumber(number);
        kycDocumentRepository.save(doc);

        // any change puts the KYC back in the admin's review queue
        if (kyc.getStatus() != KycStatus.PENDING) {
            kyc.setStatus(KycStatus.PENDING);
            kycRepository.save(kyc);
        }
        return summary(kyc);
    }

    @Transactional
    public KycSummury deleteKycDocument(Long userId, Long documentId) {
        KycDocument d = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
        Kyc kyc = d.getKyc();
        if (!kyc.getUser().getUserId().equals(userId))
            throw new ApiException(HttpStatus.NOT_FOUND, "Document not found");
        if (kyc.getStatus() == KycStatus.VERIFIED)
            throw new ApiException(HttpStatus.CONFLICT, "Verified documents cannot be deleted");
        kycDocumentRepository.delete(d);
        return summary(kyc);
    }

    // -------------------------------------------------------------- admin

    @Transactional()
    public PagedResponse<AdminKycResponse> listKyc(KycStatus status, Pageable pageable) {
        Page<Kyc> page = status == null ? kycRepository.findAll(pageable)
                : kycRepository.findByStatus(status, pageable);
        return PagedResponse.from(page.map(k -> new AdminKycResponse(
                k.getId(), k.getUser().getUserId(), k.getUser().getName(), k.getUser().getEmail(),
                k.getStatus(),
                kycDocumentRepository.findByKyc_Id(k.getId()).stream()
                        .map(d -> new AdminKycDocument(d.getId(), d.getDocumentType(), d.getDocumentNumber()))
                        .toList(),
                k.getUser().getRole().name())));
    }

    @Transactional
    public void setKycStatus(Long kycId, KycStatus status) {
        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "KYC not found"));
        if (status == KycStatus.VERIFIED && kycDocumentRepository.findByKyc_Id(kycId).isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot verify a KYC with no documents");
        kyc.setStatus(status);
        kycRepository.save(kyc);
    }

    // ------------------------------------------------------------ helpers

    private KycSummury summary(Kyc k) {
        return new KycSummury(k.getStatus(), kycDocumentRepository.findByKyc_Id(k.getId()).stream()
                .map(kycDocumentMapper::toResponse).toList());
    }

    private Address ownedAddress(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Address not found"));
        if (!a.getUser().getUserId().equals(userId))
            throw new ApiException(HttpStatus.NOT_FOUND, "Address not found");
        return a;
    }
    @Transactional()
    public boolean kycVerified(Long userId) {
        return kycRepository.findByUser_UserId(userId).map(k -> k.getStatus() == KycStatus.VERIFIED).orElse(false);
    }
}
