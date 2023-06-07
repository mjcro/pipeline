package io.github.mjcro.pipeline;

import io.github.mjcro.interfaces.ints.WithSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Simple pipeline implementation.
 * <p>
 * Is not thread-safe during initialization step, when {@link #addFirst(Step)} and {@link #addLast(Step)}
 * methods are invoked, but after that pipeline can be safely used in concurrent environment.
 */
public class Pipeline<T> implements WithSize {
    private final ArrayList<Step<T>> steps = new ArrayList<>();

    /**
     * Adds processing step to the start of processing queue.
     *
     * @param step Step to add.
     */
    public void addFirst(Step<T> step) {
        if (step != null) {
            Collections.reverse(steps);
            steps.add(step);
            Collections.reverse(steps);
        }
    }

    /**
     * Adds processing step to the end of processing queue.
     *
     * @param step Step to add.
     */
    public void addLast(Step<T> step) {
        if (step != null) {
            steps.add(step);
        }
    }

    @Override
    public int size() {
        return steps.size();
    }

    /**
     * Performs data processing.
     *
     * @param data Data to process.
     * @return Processing result as completable future.
     */
    public CompletableFuture<T> process(T data) {
        if (isEmpty()) {
            return CompletableFuture.completedFuture(data);
        }

        CompletableFuture<T> current = null;
        for (final Step<T> handler : steps) {
            if (current == null) {
                current = handler.handle(data);
            } else {
                current = current.thenCompose(handler::handle);
            }
        }
        return current;
    }

    /**
     * Performs data processing.
     *
     * @param data Data to process.
     * @return Processing result.
     */
    public T processJoin(T data) {
        return process(data).join();
    }
}
