package org.openengsb.core.edbi.performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openengsb.core.edbi.jdbc.Result;
import org.openengsb.core.edbi.performance.tests.ConsecutiveDeleteTest;
import org.openengsb.core.edbi.performance.tests.ConsecutiveInsertTest;
import org.openengsb.core.edbi.performance.tests.ConsecutiveUpdateTest;
import org.openengsb.core.edbi.performance.tests.InsertTest;

/**
 * Runner
 */
public class Runner {

    @Test
    public void test() throws Exception {
        new Runner().run();
    }

    private void run() throws Exception {
        TestRunner runner = new TestRunner(3, 3);

//        runner.run(InsertTest.class, 10, 100);
//        runner.run(InsertTest.class, 50, 100);
//        runner.run(InsertTest.class, 50, 1000);
//
//        runner.run(ConsecutiveInsertTest.class, 10, 100);
//        runner.run(ConsecutiveInsertTest.class, 50, 1000);
//        runner.run(ConsecutiveInsertTest.class, 50, 10000);
//
//        runner.run(ConsecutiveUpdateTest.class, 1000, 10, 100);
//        runner.run(ConsecutiveUpdateTest.class, 50000, 50, 1000);
//        runner.run(ConsecutiveUpdateTest.class, 50000, 10, 10000);
//
//        runner.run(ConsecutiveDeleteTest.class, 1000, 10, 100);
//        runner.run(ConsecutiveDeleteTest.class, 50000, 50, 1000);

        runner.run(ConsecutiveDeleteTest.class, 50000, 5, 10000);

        writeResult(new File("/tmp/edbi"));
    }

    private void writeResult(File dir) throws IOException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory()) {
            throw new IOException("Expected " + dir + "to be a file");
        }

        Map<String, String> results = buildResult();

        for (Map.Entry<String, String> entry : results.entrySet()) {
            String name = normalize(entry.getKey()) + ".csv";

            File file = new File(dir, name);

            try (FileWriter writer = new FileWriter(file)) {
                writer.append(entry.getValue());
            }
        }
    }

    private String normalize(String str) {
        return str.replaceAll("[^a-zA-Z0-9]+", "_").replaceAll("^[_]+", "").replaceAll("[_]+$", "").toLowerCase();
    }

    private Map<String, String> buildResult() {
        Set<String> tests = new LinkedHashSet<>();

        for (Result r : Result.results()) {
            tests.add(r.getLabel());
        }

        Map<String, String> maps = new HashMap<>();
        for (String test : tests) {
            StringBuilder str = new StringBuilder();

            row(str, test, "Head", "History");

            List<Result> testResults = Result.results(test);
            for (Result result : testResults) {
                List<Result.Run> runs = result.getRuns();
                for (Result.Run run : runs) {
                    row(str, run.getLabel(), run.getHead(), run.getHistory());
                }
            }

            maps.put(test, str.toString());
        }

        return maps;
    }

    private static void row(StringBuilder str, Object... args) {
        tab(str, args);
        nl(str);
    }

    private static void nl(StringBuilder str) {
        str.append("\n");
    }

    private static void tab(StringBuilder str, Object... args) {
        str.append(join("\t", args));
    }

    private static String join(String separator, Object... args) {
        return StringUtils.join(args, separator);
    }

    public static class TestRunner {
        public int warmup;
        public int runs;

        public TestRunner(int warmup, int runs) {
            this.warmup = warmup;
            this.runs = runs;
        }

        @SuppressWarnings("unchecked")
        public void run(Class<? extends ITest> clazz, Object... args) throws Exception {
            ITest test = (ITest) new ReflectiveFactory(clazz, args).newInstance();
            run(test);
        }

        public void run(ITest test) throws Exception {
            Result.setWarmup(true);
            for (int i = 0; i < warmup; i++) {
                test.run();
            }
            Result.setWarmup(false);

            for (int i = 0; i < runs; i++) {
                test.run();
            }
        }
    }

}
