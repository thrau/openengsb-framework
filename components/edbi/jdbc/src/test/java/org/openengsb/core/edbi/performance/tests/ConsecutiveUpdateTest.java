package org.openengsb.core.edbi.performance.tests;

import java.util.Collections;
import java.util.List;

import org.openengsb.core.edbi.jdbc.Result;
import org.openengsb.core.edbi.performance.DatabaseSession;
import org.openengsb.core.edbi.performance.Plc;

/**
 * ConsecutiveUpdateTest
 */
public class ConsecutiveUpdateTest extends AbstractTest {

    private int initialSize;
    private int iterations;
    private int n;

    public ConsecutiveUpdateTest(Integer initialSize, Integer iterations, Integer n) {
        this.initialSize = initialSize;
        this.iterations = iterations;
        this.n = n;
    }

    @Override
    public void run() throws Exception {
        Result.push("Consecutive Update " + initialSize + "," + iterations + "," + n);

        try (DatabaseSession session = new DatabaseSession()) {

            List<Plc> all = insert(session.engine(), initialSize);
            session.reboot();

            List<Plc> update = all.subList(0, n);

            for (int i = 0; i < iterations; i++) {
                Result.result().iterate(String.valueOf(initialSize + (n * i)));
                Collections.shuffle(all);

                update(session.engine(), update);
            }
        }
    }

}
