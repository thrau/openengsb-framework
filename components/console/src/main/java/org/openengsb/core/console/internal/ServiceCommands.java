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

package org.openengsb.core.console.internal;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.security.service.AccessDeniedException;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Command(scope = "openengsb", name = "service", description = "Prints out the created OpenEngSB services.")
public class ServiceCommands extends OsgiCommandSupport {

    @Argument(index = 0, name = "command", description = "The service command argument (CREATE, UPDATE, DELETE)",
            required = true, multiValued = false)
    String arg = null;

    @Argument(index = 1, name = "serviceId", description = "The domain id to be instantiated", required = false,
            multiValued = false)
    String serviceId = null;

    @Argument(index = 2, name = "serviceAttributes", description = "The service attributes (alphabetic order)",
            required = false, multiValued = true)
    List<String> attributes = null;

    private OsgiUtilsService osgiUtilsService;

    private enum ARGUMENTS {
        LIST,
        CREATE,
        UPDATE,
        DELETE
    }

    protected Object doExecute() throws Exception {
        ServiceReference sr = getBundleContext().getServiceReference("org.openengsb.core.api.OsgiUtilsService");
        ServiceReference commandSessionReference =
                getBundleContext().getServiceReference("org.apache.felix.service.command.CommandProcessor");
        osgiUtilsService = getService(OsgiUtilsService.class, sr);
        CommandProcessor commandProcessor = getService(CommandProcessor.class, commandSessionReference);
        CommandSession commandSession = commandProcessor.createSession(System.in, System.err, System.out);
        InputStream keyboard = commandSession.getKeyboard();

        List<DomainProvider> serviceList = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());

        ARGUMENTS arguments = ARGUMENTS.valueOf(arg.toUpperCase());
        switch (arguments) {
            case LIST:
                listServices(serviceList);
                break;
            case CREATE:
                createService(serviceList, keyboard);
                //TODO
                break;
            case UPDATE:
                //TODO
                break;
            case DELETE:
                //TODO
                break;
            default:
                System.err.println("Invalid Argument");
                break;
        }

        return null;
    }

    private void createService(List<DomainProvider> serviceList, InputStream keyboard) {
        boolean readInput = true;
        try {
            while (readInput) {
                char read = (char) keyboard.read();

            }
        } catch (IOException e) {
            //TODO error handling
            e.printStackTrace();
        }

    }

    private void listServices(List<DomainProvider> serviceList) {
        OutputStreamFormater.printValue("Services");
        /**List<ServiceReference> listServiceReferences = osgiUtilsService.listServiceReferences(Domain.class);
         for (ServiceReference ref : listServiceReferences) {
         Domain service = osgiUtilsService.getService(Domain.class, ref);
         try {
         String instanceId = service.getInstanceId();
         if (instanceId != null) {
         OutputStreamFormater.printValue(instanceId, service.getAliveState().toString());
         }
         } catch (AccessDeniedException ex) {
         OutputStreamFormater.printValue(ex.getMessage());
         }
         }
         **/
        for (DomainProvider dp : serviceList) {
            String domainType = dp.getId();
            List<ConnectorProvider> connectorProviders = osgiUtilsService.listServices(
                    ConnectorProvider.class, String.format("(%s=%s)", Constants.DOMAIN_KEY, domainType));

            Locale defaultLocale = Locale.getDefault();

            OutputStreamFormater.printValue(dp.getName().getString(defaultLocale),
                    dp.getDescription().getString(defaultLocale));
            for (ConnectorProvider connectorProvider : connectorProviders) {
                String serviceId = connectorProvider.getId();
                String serviceName = connectorProvider.getDescriptor().getName().getString(defaultLocale);
                String serviceDescription = connectorProvider.getDescriptor().getDescription().getString(defaultLocale);
                if (serviceId != null && serviceName != null && serviceDescription != null) {
                    OutputStreamFormater.printValuesWithPrefix(serviceId, serviceName, serviceDescription);
                }
            }
        }

    }


}