package com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks;

import lombok.Getter;

/**
 * An abstract task class with a generic type parameter representing the result type.
 * <p>
 * Each task follows a lifecycle with {@code init()}, {@code onTick()}, and {@code end()} methods.
 * The task begins in a {@link TaskStatus#PENDING} state by default
 * Subclasses must implement {@code getResult()} to return the final output once the task is complete.
 * <p>
 * In the subclass, please initialize taskStatus to {@link TaskStatus#RUNNING}. Once the task has successfully finished,
 * set it to{@link TaskStatus#SUCCESS}. If the task failed, set taskStatus to {@link TaskStatus#FAILURE}, and attach
 * an error.
 * @param <T> the type of result this task produces upon successful completion
 */
public abstract class AbstractInventoryTask<T> {
    @Getter
    protected TaskStatus taskStatus = TaskStatus.PENDING;

    @Getter
    protected String error;

    abstract public void init();
    abstract public void onTick();
    abstract public void end();
    abstract public T getResult();
}
