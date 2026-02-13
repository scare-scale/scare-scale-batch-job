package com.scarescale.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class BatchDispatcherTest {

    static class MockJob implements BatchJob {
        public boolean ran = false;

        @Override
        public void run() {
            ran = true;
        }
    }

    private BatchDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleRequestWithValidJob() {
        final MockJob mockJob = new MockJob();
        dispatcher = new BatchDispatcher(List.of(mockJob));

        String result = dispatcher.handleRequest(
                Map.of("jobName", "MockJob"),
                null
        );

        assertEquals("Job MockJob completed", result);
        assertTrue(mockJob.ran);
    }

    @Test
    void testHandleRequestWithMissingJobName() {
        dispatcher = new BatchDispatcher(List.of());

        assertThrows(IllegalArgumentException.class, () -> {
            dispatcher.handleRequest(Map.of(), null);
        });
    }

    @Test
    void testHandleRequestWithUnknownJob() {
        dispatcher = new BatchDispatcher(List.of());

        assertThrows(IllegalArgumentException.class, () -> {
            dispatcher.handleRequest(
                    Map.of("jobName", "UnknownJob"),
                    null
            );
        });
    }
}

