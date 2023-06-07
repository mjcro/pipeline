package io.github.mjcro.pipeline;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StepTest {
    @Test
    public void testSync() {
        String name = Thread.currentThread().getName();
        Step<Integer> step = Step.simpleSync($i -> {
            Assert.assertEquals(Thread.currentThread().getName(), name);
            return $i * $i;
        });

        Assert.assertEquals(step.handle(9).join(), 81);
        Assert.assertEquals(step.handle(-2).join(), 4);
    }

    @Test
    public void testAsync() {
        String name = Thread.currentThread().getName();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Step<Integer> step = Step.simpleAsync(executor, $i -> {
            Assert.assertNotEquals(Thread.currentThread().getName(), name);
            return $i * $i;
        });

        Assert.assertEquals(step.handle(9).join(), 81);
        Assert.assertEquals(step.handle(-2).join(), 4);
        executor.shutdown();
    }
}