package com.scarescale.batch;

import com.amazonaws.services.lambda.runtime.Context;

public interface BatchJob {
    void run();
}
