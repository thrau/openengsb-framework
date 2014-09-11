package org.openengsb.core.edbi.performance;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.openengsb.core.edbi.jdbc.JdbcIndexEngine;
import org.openengsb.core.edbi.jdbc.JdbcIndexEngineFactory;
import org.openengsb.core.edbi.jdbc.driver.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DatabaseSession
 */
public class DatabaseSession implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSession.class);

    protected static AtomicInteger cnt = new AtomicInteger(0);

    protected String path;

    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;
    private JdbcIndexEngine indexEngine;

    private boolean dropAfterClose;

    public DatabaseSession() throws SQLException {
        this("/tmp/test_" + cnt.incrementAndGet());
    }

    public DatabaseSession(String path) throws SQLException {
        this.path = path;
        this.dropAfterClose = true;

        this.dataSource = createEmbeddedDataSource(path);
        createEdbiSchema();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public boolean isDropAfterClose() {
        return dropAfterClose;
    }

    public void setDropAfterClose(boolean dropAfterClose) {
        this.dropAfterClose = dropAfterClose;
    }

    /**
     * Lazy getter for a JdbcTemplate for this database session.
     *
     * @return a JdbcTemplate
     * @throws java.sql.SQLException propagated exceptions
     */
    public JdbcTemplate jdbc() throws SQLException {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(getDataSource());
        }

        return jdbcTemplate;
    }

    public JdbcIndexEngine engine() {
        if(indexEngine == null) {
            indexEngine = createEngine();
        }

        return indexEngine;
    }

    /**
     * Execute arbitrary SQL using a JDBC Statement on the current database.
     *
     * @param sql the SQL to execute
     * @throws java.sql.SQLException propagated JDBC exceptions
     */
    public void execute(String sql) throws SQLException {
        try (Connection c = getDataSource().getConnection()) {
            try (Statement statement = c.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    /**
     * Shuts down the data source and re-creates it.
     *
     * @throws java.sql.SQLException propagated sql exceptions
     */
    public void reboot() throws SQLException {
        shutdown();
        dataSource = createEmbeddedDataSource();
    }

    /**
     * Drops all schema objects in this database.
     *
     * @throws java.sql.SQLException propagated exceptions
     */
    public void truncate() throws SQLException {
        execute("DROP ALL OBJECTS");
    }

    public void shutdown() throws SQLException {
        jdbcTemplate = null;
        indexEngine = null;
        execute("SHUTDOWN");
    }

    public void drop() throws IOException {
        LOG.info("Dropping files for {}", path);
        new File(path + ".h2.db").delete();
        new File(path + ".lock.db").delete();
        new File(path + ".trace.db").delete();
    }

    @Override
    public void close() throws IOException {
        LOG.info("Closing database session {}", path);
        try {
            shutdown();
        } catch (SQLException e) {
            LOG.error("SQL error while shutting down data source", e);
        }

        if (isDropAfterClose()) {
            sleep(200);
            drop();
        }
    }

    public void createEdbiSchema() throws SQLException {
        String string;

        try {
            string = readResourceContent("index-schema.h2.sql");
        } catch (IOException e) {
            throw new RuntimeException("Could not read from resource", e);
        }

        try (Connection c = dataSource.getConnection()) {
            c.createStatement().execute(string);
        }
    }

    protected DataSource createEmbeddedDataSource() throws SQLException {
        return createEmbeddedDataSource(path);
    }

    protected DataSource createEmbeddedDataSource(String path) throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL("jdbc:h2:" + path + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("");
        dataSource.setPassword("");

        return dataSource;
    }

    private JdbcIndexEngine createEngine() {
        return createEngine(getDataSource());
    }

    private JdbcIndexEngine createEngine(DataSource dataSource) {
        Driver driver = new Driver(dataSource);
        JdbcIndexEngineFactory factory = new JdbcIndexEngineFactory(driver);
        return factory.create();
    }

    protected String readResourceContent(String resource) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Stream for resource " + resource + " is null");
            }

            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer);
            return writer.toString();
        }
    }

    protected boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return false;
        } catch (InterruptedException e) {
            return true;
        }
    }

}
