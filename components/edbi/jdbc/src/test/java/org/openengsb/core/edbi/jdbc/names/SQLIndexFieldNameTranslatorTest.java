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
package org.openengsb.core.edbi.jdbc.names;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openengsb.core.edbi.api.IndexField;

/**
 * SQLIndexFieldNameTranslatorTest
 */
public class SQLIndexFieldNameTranslatorTest {

    @Test
    public void translate_returnsCorrectString() throws Exception {
        IndexField<?> field = mock(IndexField.class);
        when(field.getName()).thenReturn("someProperty");

        assertEquals("SOMEPROPERTY", new SQLIndexFieldNameTranslator().translate(field));
    }

    @Test(expected = IllegalArgumentException.class)
    public void translateNull_throwsException() throws Exception {
        new SQLIndexFieldNameTranslator().translate(null);
    }

}
