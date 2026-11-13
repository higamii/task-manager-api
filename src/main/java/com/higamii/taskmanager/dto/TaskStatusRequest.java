package com.higamii.taskmanager.dto;

import com.higamii.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(
        @NotNull(message = "O status é obrigatório")
        TaskStatus status
) {}
