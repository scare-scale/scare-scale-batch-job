package com.scarescale.batch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public record BatchDispatcher(
        Iterable<BatchJob> jobs) implements RequestHandler<Map<String, Object>, String> {

    public BatchDispatcher() {
        this(ServiceLoader.load(BatchJob.class));
    }

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        String jobName = (String) event.get("jobName");
        if (jobName == null) {
            throw new IllegalArgumentException("Missing jobName in event");
        }

        // Find matching runner
        Optional<BatchJob> job = StreamSupport.stream(jobs.spliterator(), false)
                .filter(p -> p.getClass().getSimpleName().equals(jobName))
                .findFirst();

        // Run Job
        job.orElseThrow(() -> new IllegalArgumentException("Unknown job: " + jobName)).run();

        return "Job " + jobName + " completed";
    }

}