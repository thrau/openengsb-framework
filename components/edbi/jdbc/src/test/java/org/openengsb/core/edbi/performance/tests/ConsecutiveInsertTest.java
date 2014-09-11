package org.openengsb.core.edbi.performance.tests;

import org.openengsb.core.edbi.jdbc.Result;
import org.openengsb.core.edbi.performance.DatabaseSession;

/**
* ConsecutiveInsertTest
*/
public class ConsecutiveInsertTest extends AbstractTest {

    private int iterations;
    private int n;

    public ConsecutiveInsertTest(Integer iterations, Integer n) {
        this.iterations = iterations;
        this.n = n;
    }

    @Override
    public void run() throws Exception {
        Result.push("Consecutive Insert " + iterations + "," + n);

        try (DatabaseSession session = new DatabaseSession()) {
            for (int i = 1; i <= iterations; i++) {
                Result.result().iterate(String.valueOf(i * n));
                insert(session.engine(), n);
            }
        }
    }
}
