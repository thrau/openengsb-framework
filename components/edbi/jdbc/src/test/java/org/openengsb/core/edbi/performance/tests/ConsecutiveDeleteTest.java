package org.openengsb.core.edbi.performance.tests;

import org.openengsb.core.edbi.jdbc.Result;
import org.openengsb.core.edbi.performance.DatabaseSession;
import org.openengsb.core.edbi.performance.Plc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ConsecutiveDeleteTest
 */
public class ConsecutiveDeleteTest extends AbstractTest {
    private int initialSize;
    private int iterations;
    private int n;

    public ConsecutiveDeleteTest(Integer initialSize, Integer iterations, Integer n) {
        this.initialSize = initialSize;
        this.iterations = iterations;
        this.n = n;
    }

    @Override
    public void run() throws Exception {
        Result.push("Consecutive Delete " + initialSize + "," + iterations + "," + n);

        try (DatabaseSession session = new DatabaseSession()) {
            List<Plc> all = insert(session.engine(), initialSize);
            session.reboot();


            for (int i = 0; i < iterations; i++) {
                Result.result().iterate(String.valueOf(initialSize + (n * i)));

                Collections.shuffle(all);
                List<Plc> update = new ArrayList<>(all.subList(0, n));

                delete(session.engine(), update);
                all.removeAll(update);
            }
        }
    }
}
