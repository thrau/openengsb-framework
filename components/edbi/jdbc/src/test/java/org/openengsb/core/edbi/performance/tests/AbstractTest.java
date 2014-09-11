package org.openengsb.core.edbi.performance.tests;

import java.util.List;

import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexCommitBuilder;
import org.openengsb.core.edbi.api.IndexEngine;
import org.openengsb.core.edbi.performance.ITest;
import org.openengsb.core.edbi.performance.Plc;
import org.openengsb.core.edbi.performance.PlcGenerator;

/**
 * AbstractTest
 */
public abstract class AbstractTest implements ITest {

    protected PlcGenerator generator = new PlcGenerator();

    protected List<Plc> insert(IndexEngine engine, int n) {
        List<Plc> plcs = generator.generate(n);
        IndexCommit commit = newTestCommit().insert(plcs, Plc.class).get();

        engine.commit(commit);

        return plcs;
    }

    protected void update(IndexEngine engine, List<Plc> plcs) {
        generator.modify(plcs);

        IndexCommit commit = newTestCommit().update(plcs, Plc.class).get();

        engine.commit(commit);
    }

    protected void delete(IndexEngine engine, List<Plc> plcs) {
        IndexCommit commit = newTestCommit().delete(plcs, Plc.class).get();

        engine.commit(commit);
    }

    protected IndexCommitBuilder newTestCommit() {
        return IndexCommitBuilder.create()
            .context("testContext")
            .user("testUser")
            .domain("testDomain")
            .connector("testConnector")
            .instance("testInstance");
    }

}
