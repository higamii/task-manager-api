package com.higamii.taskmanager.service;

import com.higamii.taskmanager.dto.TaskRequest;
import com.higamii.taskmanager.dto.TaskResponse;
import com.higamii.taskmanager.dto.TaskSummary;
import com.higamii.taskmanager.exception.BusinessRuleException;
import com.higamii.taskmanager.exception.ResourceNotFoundException;
import com.higamii.taskmanager.model.Task;
import com.higamii.taskmanager.model.TaskPriority;
import com.higamii.taskmanager.model.TaskStatus;
import com.higamii.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        apply(task, request);
        return TaskResponse.from(repository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(TaskStatus status, TaskPriority priority, Pageable pageable) {
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        return repository.findAll(spec, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return TaskResponse.from(getOrThrow(id));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getOrThrow(id);
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessRuleException("Tarefas concluídas não podem ser editadas");
        }
        apply(task, request);
        return TaskResponse.from(repository.save(task));
    }

    public TaskResponse updateStatus(Long id, TaskStatus newStatus) {
        Task task = getOrThrow(id);
        if (!task.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessRuleException(
                    "Transição inválida de " + task.getStatus() + " para " + newStatus);
        }
        task.setStatus(newStatus);
        return TaskResponse.from(repository.save(task));
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public TaskSummary summary() {
        long todo = repository.countByStatus(TaskStatus.TODO);
        long inProgress = repository.countByStatus(TaskStatus.IN_PROGRESS);
        long done = repository.countByStatus(TaskStatus.DONE);
        return new TaskSummary(todo, inProgress, done, todo + inProgress + done);
    }

    private Task getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa " + id + " não encontrada"));
    }

    private void apply(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
    }
}
