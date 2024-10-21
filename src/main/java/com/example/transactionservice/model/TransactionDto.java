package com.example.transactionservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TransactionDto(
    Long id,
    @NotBlank(message = "UserId is mandatory.")
    String userId,
    @NotNull(message = "Transaction amount is mandatory.")
    Double amount
){}
