package com.shipping.auth_service.DTO.Request;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record JoiningDateRequest(@NotNull @FutureOrPresent LocalDate joiningDate,
                                 @NotNull Long onboardingPortId,
                                 @NotBlank @Size(max = 100) String portName) {}
