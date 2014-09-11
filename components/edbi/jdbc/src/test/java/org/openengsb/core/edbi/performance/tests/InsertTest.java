package org.openengsb.core.edbi.performance.tests;

import org.openengsb.core.edbi.jdbc.Result;
import org.openengsb.core.edbi.performance.DatabaseSession;

/**
 * InsertTest
 */
public class InsertTest extends AbstractTest {

    private int iterations;
    private int n;

    public InsertTest(Integer iterations, Integer n) {
        this.iterations = iterations;
        this.n = n;
    }

    @Override
    public void run() throws Exception {
        Result.push("Insert " + iterations + "," + n);

        for (int i = 1; i <= iterations; i++) {
            try (DatabaseSession session = new DatabaseSession()) {
                Result.result().iterate(String.valueOf(i * n));
                insert(session.engine(), i * n);
            }
        }
    }
}
