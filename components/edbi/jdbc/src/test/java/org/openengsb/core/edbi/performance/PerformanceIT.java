package org.openengsb.core.edbi.performance;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexCommitBuilder;
import org.openengsb.core.edbi.jdbc.AbstractH2DatabaseTest;
import org.openengsb.core.edbi.jdbc.JdbcIndex;
import org.openengsb.core.edbi.jdbc.JdbcIndexEngine;
import org.openengsb.core.edbi.jdbc.JdbcIndexEngineFactory;
import org.openengsb.core.edbi.jdbc.driver.h2.Driver;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PerformanceIT
 */
public class PerformanceIT extends AbstractH2DatabaseTest {

    @Override
    protected String[] getInitScriptResourceNames() {
        return new String[]{
            "index-schema.h2.sql"
        };
    }

    Driver driver;
    JdbcIndexEngine engine;
    PlcGenerator generator;

    @Before
    public void setUp() throws Exception {
        driver = new Driver(getDataSource());
        JdbcIndexEngineFactory factory = new JdbcIndexEngineFactory(driver);
        engine = factory.create();
        generator = new PlcGenerator();
    }

    @Test
    public void test_create() throws Exception {
        long then;

        then = System.currentTimeMillis();
        engine.createIndex(Plc.class);
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));

        then = System.currentTimeMillis();
        engine.getIndex(Plc.class);
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
    }

    @Test
    public void consecutive_insert() throws Exception {
        int iterations = 50;
        int n = 10000;

        reset();
        insert(n);
        reset();

        long total = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            List<Plc> records = generator.generate(n);
            IndexCommit commit = newTestCommit().insert(records, Plc.class).get();
            System.out.print(String.valueOf(i * n) + "\t");
            long start = System.currentTimeMillis();
            engine.commit(commit);
            report(start, String.valueOf(i * n));
        }

        report(total, "total");
    }

    @Test
    public void insert() throws Exception {
        int n = 10;
        int step = 1000;

        reset();
        insert(step);

        for (int i = 0; i < n; i++) {
            reset();
            int cnt = (i+1) * step;
            System.out.print(cnt + "\t");
            insert(cnt);
        }
    }

    @Test
    public void update() throws Exception {
        int n = 50000;

        reset();
        insert(n);
        reset();

        int steps = 50;
        int step = n/steps;

        List<Plc> list = insert(n);

        for (int i = 0; i < steps; i++) {
            List<Plc> plcs = list.subList(i * step, (i + 1) * step);
            update(plcs);
        }
    }

    @Test
    public void delete() throws Exception {
        int n = 50000;

        reset();
        insert(n);
        reset();

        int steps = 50;
        int step = n/steps;

        List<Plc> list = insert(n);

        for (int i = 0; i < steps; i++) {
            List<Plc> plcs = list.subList(i * step, (i + 1) * step);
            System.out.printf("%d\t", i * step + n);
            delete(plcs);
        }
    }

    @Test
    public void query() throws Exception {
        JdbcIndex<Plc> index = engine.getIndex(Plc.class);
        long then;
        List<Map<String, Object>> maps;
        String sql;
        Map<String, Object> row;

        then = System.currentTimeMillis();
        sql = "SELECT * FROM " + index.getHeadTableName() + " WHERE UUID = ?";
        row = jdbc().queryForMap(sql, "6323fc2f-7a4a-48bb-a21f-bcdc9c65a88e");
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
        System.out.println(row);


        // count the amount of commits on a model
        then = System.currentTimeMillis();
        sql = "SELECT COUNT(DISTINCT REV_COMMIT) FROM " + index.getHistoryTableName();
        row = jdbc().queryForMap(sql);
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
        System.out.println(row);


        // count the amount of commits on a model
        then = System.currentTimeMillis();
        sql = "SELECT DISTINCT REV_COMMIT FROM " + index.getHistoryTableName();
        maps = jdbc().queryForList(sql);
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
        System.out.println(maps);

        // select the amount of deleted items
        then = System.currentTimeMillis();
        sql = "SELECT COUNT(*) FROM " + index.getHistoryTableName() + " WHERE REV_OPERATION = ?";
        row = jdbc().queryForMap(sql, "DELETE");
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
        System.out.println(row);


        // select ids from deleted signals
        then = System.currentTimeMillis();
        sql = "SELECT UUID FROM " + index.getHistoryTableName() + " WHERE REV_OPERATION = ?";
        maps = jdbcTemplate.queryForList(sql, "DELETE");
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
        System.out.println(maps.size());

        // select items from head table
        then = System.currentTimeMillis();
        sql = "SELECT * FROM " + index.getHeadTableName() + " LIMIT 1000,1000";
        maps = jdbcTemplate.queryForList(sql);
        System.out.printf("took %d ms\n", (System.currentTimeMillis() - then));
        System.out.println(maps.size());
    }

    @Test
    public void drop() throws Exception {
        purge();
    }

    public void dump(List<Plc> plcs) throws Exception {
        dump(plcs, new File("/tmp/plcs"));
    }

    public List<Plc> read() throws Exception {
        return read(new File("/tmp/plcs"));
    }

    public void dump(List<Plc> list, File file) throws Exception {
        new ObjectMapper().writeValue(file, list);
    }

    public List<Plc> read(File file) throws Exception {
        return read(file, Plc.class);
    }

    public <T> List<T> read(File file, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(file, type);
    }

    protected List<Plc> insert(int n) {
        List<Plc> plcs = generator.generate(n);
        IndexCommit commit = newTestCommit().insert(plcs, Plc.class).get();

        long start = System.currentTimeMillis();
        engine.commit(commit);
        report(start, "insert " + n);

        return plcs;
    }

    protected void update(List<Plc> plcs) {
        generator.modify(plcs);
        IndexCommit commit = newTestCommit().update(plcs, Plc.class).get();

        long start = System.currentTimeMillis();
        engine.commit(commit);
        report(start, "update");
    }

    protected void delete(List<Plc> plcs) {
        IndexCommit commit = newTestCommit().delete(plcs, Plc.class).get();

        long start = System.currentTimeMillis();
        engine.commit(commit);
        report(start, "delete");
    }

    protected static void report(long start, String prefix) {
        //System.out.printf("%s\t%d\n", prefix, System.currentTimeMillis() - start);
    }

    protected void purge() throws Exception {
        String sql = "SELECT TABLE_HEAD, TABLE_HISTORY FROM INDEX_INFORMATION";

        List<Map<String, Object>> maps = jdbc().queryForList(sql);

        for (Map<String, Object> map : maps) {
            jdbc().execute("DROP TABLE IF EXISTS " + String.valueOf(map.get("TABLE_HEAD")));
            jdbc().execute("DROP TABLE IF EXISTS " + String.valueOf(map.get("TABLE_HISTORY")));
        }

        jdbc().execute("DROP TABLE IF EXISTS INDEX_INFORMATION");
        jdbc().execute("DROP TABLE IF EXISTS INDEX_FIELD_INFORMATION");
    }

    @Override
    protected DataSource createDataSource() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();

        // dataSource.setURL("jdbc:h2:tcp://localhost/tmp/test");
        dataSource.setURL("jdbc:h2:/tmp/test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    protected void reset() throws Exception {
        purge();
        tearDownDataSource();
        setUpDataSource();
        setUp();

        if (!engine.indexExists(Plc.class)) {
            engine.createIndex(Plc.class);
        }
    }

    @Override
    protected void shutdownDataSource(DataSource dataSource) throws SQLException {
        super.shutdownDataSource(dataSource);
    }

    private IndexCommitBuilder newTestCommit() {
        return IndexCommitBuilder.create()
            .context("testContext")
            .user("testUser")
            .domain("testDomain")
            .connector("testConnector")
            .instance("testInstance");
    }
}
