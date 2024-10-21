package com.example.transactionservice.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record NotificationDto (

    Long notificationId,
    @NotNull
    Long userId
){};
