package io.github.mjcro.pipeline;

import io.github.mjcro.interfaces.functions.ExceptionalUnaryOperator;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.UnaryOperator;

/**
 * Handles data processing producing completable future with result.
 */
public interface Step<T> {
    /**
     * Constructs synchronous handler.
     *
     * @param processor Processor that performs data processing.
     * @return Handler.
     */
    static <T> Step<T> sync(ExceptionalUnaryOperator<T> processor) {
        Objects.requireNonNull(processor, "processor");
        return in -> {
            try {
                return CompletableFuture.completedFuture(processor.apply(in));
            } catch (Throwable e) {
                return CompletableFuture.failedFuture(e);
            }
        };
    }

    /**
     * Constructs synchronous handler.
     *
     * @param processor Processor that performs data processing.
     * @return Handler.
     */
    static <T> Step<T> simpleSync(UnaryOperator<T> processor) {
        return sync(ExceptionalUnaryOperator.fromUnaryOperator(processor));
    }

    /**
     * Constructs asynchronous handler.
     *
     * @param executor  Executor to use for processing.
     * @param processor Processor that performs data processing.
     * @return Handler.
     */
    static <T> Step<T> async(Executor executor, ExceptionalUnaryOperator<T> processor) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(processor, "processor");
        return in -> CompletableFuture.supplyAsync(() -> {
            try {
                return processor.apply(in);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Constructs asynchronous handler.
     *
     * @param executor  Executor to use for processing.
     * @param processor Processor that performs data processing.
     * @return Handler.
     */
    static <T> Step<T> simpleAsync(Executor executor, UnaryOperator<T> processor) {
        return async(executor, ExceptionalUnaryOperator.fromUnaryOperator(processor));
    }

    /**
     * Performs incoming data processing returning completable future with result.
     *
     * @param in Incoming data.
     * @return Processed data as completable future.
     */
    CompletableFuture<T> handle(T in);
}