package com.higamii.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.higamii.taskmanager.dto.TaskRequest;
import com.higamii.taskmanager.dto.TaskResponse;
import com.higamii.taskmanager.dto.TaskSummary;
import com.higamii.taskmanager.exception.BusinessRuleException;
import com.higamii.taskmanager.exception.ResourceNotFoundException;
import com.higamii.taskmanager.model.Task;
import com.higamii.taskmanager.model.TaskPriority;
import com.higamii.taskmanager.model.TaskStatus;
import com.higamii.taskmanager.repository.TaskRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    private Task taskWithStatus(TaskStatus status) {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Estudar Spring");
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(status);
        return task;
    }

    @Test
    @DisplayName("Deve criar uma tarefa com status TODO")
    void shouldCreateTaskWithTodoStatus() {
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = service.create(
                new TaskRequest("Estudar Spring", "Capítulo 3", TaskPriority.HIGH, null));

        assertThat(response.title()).isEqualTo("Estudar Spring");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a tarefa não existe")
    void shouldThrowWhenTaskNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Não deve permitir editar tarefa concluída")
    void shouldNotUpdateDoneTask() {
        when(repository.findById(1L)).thenReturn(Optional.of(taskWithStatus(TaskStatus.DONE)));

        assertThatThrownBy(() -> service.update(1L,
                new TaskRequest("Novo título", null, TaskPriority.LOW, null)))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve reabrir tarefa concluída")
    void shouldNotReopenDoneTask() {
        when(repository.findById(1L)).thenReturn(Optional.of(taskWithStatus(TaskStatus.DONE)));

        assertThatThrownBy(() -> service.updateStatus(1L, TaskStatus.TODO))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Deve avançar o status de TODO para IN_PROGRESS")
    void shouldMoveTaskToInProgress() {
        when(repository.findById(1L)).thenReturn(Optional.of(taskWithStatus(TaskStatus.TODO)));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = service.updateStatus(1L, TaskStatus.IN_PROGRESS);

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Deve calcular o resumo de tarefas")
    void shouldCalculateSummary() {
        when(repository.countByStatus(TaskStatus.TODO)).thenReturn(3L);
        when(repository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(2L);
        when(repository.countByStatus(TaskStatus.DONE)).thenReturn(5L);

        TaskSummary summary = service.summary();

        assertThat(summary.total()).isEqualTo(10L);
    }
}
