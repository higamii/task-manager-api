package com.higamii.taskmanager.dto;

import com.higamii.taskmanager.model.TaskPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
        String title,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "A prioridade é obrigatória")
        TaskPriority priority,

        @FutureOrPresent(message = "O prazo não pode estar no passado")
        LocalDate dueDate
) {}
