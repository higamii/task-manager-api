package com.higamii.taskmanager.model;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    /** Regra de negócio: uma tarefa concluída é um estado final. */
    public boolean canTransitionTo(TaskStatus next) {
        return this != DONE && this != next;
    }
}
