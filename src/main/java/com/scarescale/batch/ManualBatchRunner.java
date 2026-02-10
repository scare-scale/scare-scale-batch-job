package com.scarescale.batch;

import java.util.Map;

public class ManualBatchRunner {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: BatchRunner <JobName>");
            return;
        }

        String jobName = args[0];

        BatchDispatcher dispatcher = new BatchDispatcher();

        String result = dispatcher.handleRequest(
                Map.of("jobName", jobName),
                null
        );

        System.out.println(result);
    }
}