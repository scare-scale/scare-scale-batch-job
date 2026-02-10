package com.scarescale.batch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class BatchDispatcherTest {

    static class TestJob implements BatchJob {
        boolean ran = false;

        @Override
        public void run() {
            ran = true;
        }
    }

    @Test
    void runsMatchingJob() {
        TestJob job = new TestJob();
        BatchDispatcher dispatcher = new BatchDispatcher(List.of(job));

        Map<String, Object> event = Map.of("jobName", "TestJob");

        String result = dispatcher.handleRequest(event, null);

        assertTrue(job.ran);
        assertEquals("Job TestJob completed", result);
    }

    @Test
    void throwsForMissingJobName() {
        BatchDispatcher dispatcher = new BatchDispatcher(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> dispatcher.handleRequest(Map.of(), null));
    }

    @Test
    void throwsForUnknownJob() {
        BatchDispatcher dispatcher = new BatchDispatcher(List.of());

        Map<String, Object> event = Map.of("jobName", "DoesNotExist");

        assertThrows(IllegalArgumentException.class,
                () -> dispatcher.handleRequest(event, null));
    }
}