/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.edbi.jdbc;

import org.openengsb.core.edbi.jdbc.api.SchemaMapper;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;
import org.openengsb.core.edbi.jdbc.sql.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facades {@link org.openengsb.core.edbi.jdbc.HeadTableEngine} and
 * {@link org.openengsb.core.edbi.jdbc.HistoryTableEngine}.
 */
public class DefaultSchemaMapper implements SchemaMapper {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSchemaMapper.class);

    private TableEngine headTableEngine;
    private TableEngine historyTableEngine;

    public DefaultSchemaMapper(TableEngine headTableEngine, TableEngine historyTableEngine) {
        this.headTableEngine = headTableEngine;
        this.historyTableEngine = historyTableEngine;
    }

    @Override
    public boolean exists(JdbcIndex<?> index) {
        boolean headExists = headTableEngine.exists(index);
        boolean histExists = historyTableEngine.exists(index);

        if (headExists ^ histExists) {
            throw new IllegalStateException("Index is inconsitent. Head: " + headExists + ", history: " + histExists);
        }

        return headExists;
    }

    @Override
    public void create(JdbcIndex<?> index) {
        // TODO: handle inconsistency
        if (exists(index)) {
            LOG.debug("Skipping schema creation for {}. Schema exists.", index.getName());
            return;
        }

        Table headTable = headTableEngine.create(index);
        Table histTable = historyTableEngine.create(index);

        index.setHeadTableName(headTable.getName());
        index.setHistoryTableName(histTable.getName());
    }

    @Override
    public void drop(JdbcIndex<?> index) {
        if (!exists(index)) {
            LOG.debug("Skipping schema drop for {}. Schema doesn't exists.", index.getName());
            return;
        }

        // TODO
        // headTableEngine.drop(index);
        // historyTableEngine.drop(index);
    }

    @Override
    public void execute(InsertOperation operation) {
        LOG.debug("Executing InsertOpration");

        long then = System.currentTimeMillis();
        headTableEngine.execute(operation);
        Result.result().currentRun().logHead(then);

        then = System.currentTimeMillis();
        historyTableEngine.execute(operation);
        Result.result().currentRun().logHistory(then);
    }

    @Override
    public void execute(UpdateOperation operation) {
        LOG.debug("Executing UpdateOperation");

        long then = System.currentTimeMillis();
        headTableEngine.execute(operation);
        Result.result().currentRun().logHead(then);

        then = System.currentTimeMillis();
        historyTableEngine.execute(operation);
        Result.result().currentRun().logHistory(then);
    }

    @Override
    public void execute(DeleteOperation operation) {
        LOG.debug("Executing DeleteOperation");

        long then = System.currentTimeMillis();
        headTableEngine.execute(operation);
        Result.result().currentRun().logHead(then);

        then = System.currentTimeMillis();
        historyTableEngine.execute(operation);
        Result.result().currentRun().logHistory(then);
    }

}
