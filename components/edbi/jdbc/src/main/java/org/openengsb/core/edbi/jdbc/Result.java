package org.openengsb.core.edbi.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {

    private static final Run DUMMY = new Run();
    private static final Result DUMMY_RESULT = new DummyResult();

    private static Result CURRENT_RESULT = DUMMY_RESULT;
    private static List<Result> RESULTS = new ArrayList<>();

    private static boolean WARMUP;

    private String label;
    private List<Run> runs;
    private Run currentRun;
    private boolean logging;

    public Result(String label, int iterations) {
        this.label = label;
        this.runs = new ArrayList<>(iterations);
        this.currentRun = DUMMY;
        this.logging = false;
    }

    public static List<Result> results() {
        return RESULTS;
    }

    public static List<Result> results(String label) {
        List<Result> list = new ArrayList<>();

        for (Result result : results()) {
            if (label.equals(result.getLabel())) {
                list.add(result);
            }
        }

        return list;
    }

    public static Result push(String label) {
        return push(label, 50);
    }

    public static Result push(String label, int iterations) {
        if (isWarmup()) {
            return DUMMY_RESULT;
        }

        CURRENT_RESULT = new Result(label, iterations);
        RESULTS.add(CURRENT_RESULT);
        CURRENT_RESULT.startLog();
        return CURRENT_RESULT;
    }

    public static boolean isWarmup() {
        return WARMUP;
    }

    public static void setWarmup(boolean warmup) {
        Result.WARMUP = warmup;
    }

    public static Result result() {
        if (isWarmup()) {
            return DUMMY_RESULT;
        }
        return CURRENT_RESULT;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public void startLog() {
        setLogging(true);
    }

    public void stopLog() {
        setLogging(false);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public Run iterate(String label) {
        if (isLogging()) {
            currentRun = new Run(label);
            runs.add(currentRun);
            return currentRun;
        } else {
            return DUMMY;
        }
    }

    public Run currentRun() {
        if (logging) {
            return currentRun;
        } else {
            return DUMMY;
        }
    }

    @Override
    public String toString() {
        return String.format("Result %s %s", getLabel(), getRuns());
    }

    public static class Run {
        private String label;

        private long head;
        private long history;

        public Run() {

        }

        public Run(String label) {
            this.label = label;
        }

        public void logHead(long start) {
            setHead(System.currentTimeMillis() - start);
        }

        public void logHistory(long start) {
            setHistory(System.currentTimeMillis() - start);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getHead() {
            return head;
        }

        public void setHead(long head) {
            this.head = head;
        }

        public long getHistory() {
            return history;
        }

        public void setHistory(long history) {
            this.history = history;
        }

        @Override
        public String toString() {
            return String.format("%s,%d,%d", label, head, history);
        }
    }

    public static class DummyResult extends Result {
        public DummyResult() {
            super("dummy", 0);
        }

        @Override
        public List<Run> getRuns() {
            return Collections.emptyList();
        }

        @Override
        public Run iterate(String label) {
            return DUMMY;
        }

        @Override
        public Run currentRun() {
            return DUMMY;
        }
    }
}
