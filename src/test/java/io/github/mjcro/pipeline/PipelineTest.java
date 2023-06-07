package io.github.mjcro.pipeline;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PipelineTest {
    @Test
    public void testEmpty() {
        Pipeline<String> pipeline = new Pipeline<>();
        Assert.assertEquals(pipeline.size(), 0);
        Assert.assertTrue(pipeline.isEmpty());
        Assert.assertEquals(pipeline.process("foo").join(), "foo");
    }

    @Test
    public void testProcess() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Pipeline<String> pipeline = new Pipeline<>();
        pipeline.addLast(Step.simpleAsync(executor, s -> s + "!"));
        pipeline.addLast(Step.simpleAsync(executor, s -> s.toUpperCase(Locale.ROOT)));
        pipeline.addFirst(Step.simpleSync(s -> "Hello, " + s));

        Assert.assertEquals(pipeline.size(), 3);
        Assert.assertFalse(pipeline.isEmpty());
        Assert.assertEquals(pipeline.processJoin("World"), "HELLO, WORLD!");
        executor.shutdown();
    }

    @Test
    public void testException() {
        Pipeline<String> pipeline = new Pipeline<>();
        AtomicBoolean stepProcessed = new AtomicBoolean(true);
        pipeline.addLast(Step.sync(s -> {
            throw new Exception("Expected exception");
        }));
        pipeline.addFirst(Step.simpleSync(s -> {
            stepProcessed.set(true);
            return s;
        }));

        pipeline.process("foo").whenComplete((s, throwable) -> {
            Assert.assertTrue(stepProcessed.get());
            Assert.assertNull(s);
            Assert.assertNotNull(throwable);
            Assert.assertEquals(throwable.getClass(), Exception.class);
            Assert.assertEquals(throwable.getMessage(), "Expected exception");
        });
    }

    @Test
    public void testExceptionAsync() {
        Pipeline<String> pipeline = new Pipeline<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        AtomicBoolean stepProcessed = new AtomicBoolean(true);
        pipeline.addLast(Step.async(executor, s -> {
            throw new Exception("Expected exception");
        }));
        pipeline.addFirst(Step.simpleAsync(executor, s -> {
            stepProcessed.set(true);
            return s;
        }));

        pipeline.process("foo").whenComplete((s, throwable) -> {
            Assert.assertTrue(stepProcessed.get());
            Assert.assertNull(s);
            Assert.assertNotNull(throwable);
            Assert.assertEquals(throwable.getClass(), Exception.class);
            Assert.assertEquals(throwable.getMessage(), "Expected exception");

            executor.shutdown();
        });
    }
}