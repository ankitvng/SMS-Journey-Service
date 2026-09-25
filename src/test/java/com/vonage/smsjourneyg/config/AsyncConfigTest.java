package com.vonage.smsjourneyg.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AsyncConfigTest {

    private AsyncConfig asyncConfig;

    @BeforeEach
    void setUp() {
        asyncConfig = new AsyncConfig();
    }

    @Test
    void shouldCreateTaskExecutorWithConfiguredValues() {

        Executor executor = asyncConfig.taskExecutor(2, 4, 10);

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        assertEquals(2, taskExecutor.getCorePoolSize());
        assertEquals(4, taskExecutor.getMaxPoolSize());
        assertEquals(10, taskExecutor.getQueueCapacity());
        assertEquals("Async-", taskExecutor.getThreadNamePrefix());

        taskExecutor.shutdown();
    }

    @Test
    void shouldRunTaskOnCallingThreadWhenExecutorIsFull() throws InterruptedException {

        Executor executor = asyncConfig.taskExecutor(1, 1, 1);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        CountDownLatch firstTaskStarted = new CountDownLatch(1);

        CountDownLatch releaseFirstTask = new CountDownLatch(1);

        AtomicReference<String> thirdTaskThread = new AtomicReference<>();

        taskExecutor.execute(() -> {

            firstTaskStarted.countDown();

            try {
                releaseFirstTask.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });


        assertTrue(firstTaskStarted.await(2, TimeUnit.SECONDS));

        taskExecutor.execute(() -> {});

        // Task 3 should trigger CallerRunsPolicy
        taskExecutor.execute(() -> {
            thirdTaskThread.set(Thread.currentThread().getName());
        });

        assertEquals(Thread.currentThread().getName(), thirdTaskThread.get());

        releaseFirstTask.countDown();

        taskExecutor.shutdown();
    }
}
