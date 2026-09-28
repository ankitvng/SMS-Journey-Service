package com.vonage.smsjourney.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(executor).isNotNull().isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(4);
        assertThat(taskExecutor.getQueueCapacity()).isEqualTo(10);
        assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("Async-");

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


        assertThat(firstTaskStarted.await(2, TimeUnit.SECONDS)).isTrue();

        taskExecutor.execute(() -> {
        });

        // Task 3 should trigger CallerRunsPolicy
        taskExecutor.execute(() -> {
            thirdTaskThread.set(Thread.currentThread().getName());
        });

        assertThat(thirdTaskThread.get()).isEqualTo(Thread.currentThread().getName());

        releaseFirstTask.countDown();

        taskExecutor.shutdown();
    }
}
