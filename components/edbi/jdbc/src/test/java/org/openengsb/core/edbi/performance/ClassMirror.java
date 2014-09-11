package org.openengsb.core.edbi.performance;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassMirror
 */
public class ClassMirror<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ClassMirror.class);

    private final Class<T> type;
    private final Map<String, PropertyDescriptor> properties;

    private List<PropertyDescriptor> propertyList;

    public ClassMirror(final Class<T> type) {
        this.type = type;
        try {
            this.properties = resolvePropertyMap(type);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<T> getType() {
        return type;
    }

    public Map<String, PropertyDescriptor> getPropertyMap() {
        return properties;
    }

    public List<PropertyDescriptor> getProperties() {
        if (propertyList == null) {
            propertyList = new ArrayList<>(getPropertyMap().values());
        }
        return propertyList;
    }

    public void set(String property, Object value, T object) {
        try {
            getPropertyMap().get(property).getWriteMethod().invoke(object, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("Failed to write property " + property, e);
        }
    }

    public Map<String, Object> read(T object) {
        Map<String, PropertyDescriptor> properties = getPropertyMap();

        Map<String, Object> map = new HashMap<>(properties.size());
        for (Map.Entry<String, PropertyDescriptor> entry : properties.entrySet()) {
            try {
                map.put(entry.getKey(), entry.getValue().getReadMethod().invoke(object));
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error("Failed to visit property " + entry.getValue().getName(), e);
            }
        }
        return map;
    }

    private List<String> delcaredPropertyNames;

    public List<String> getDeclaredFieldNames() {
        if (delcaredPropertyNames == null) {
            delcaredPropertyNames = new ArrayList<>();

            for (Field field : getType().getDeclaredFields()) {
                delcaredPropertyNames.add(field.getName());
            }
        }

        return delcaredPropertyNames;
    }

    protected Map<String, PropertyDescriptor> resolvePropertyMap(Class<?> type) throws IntrospectionException {
        List<PropertyDescriptor> list = resolveProperties(type);
        Map<String, PropertyDescriptor> map = new LinkedHashMap<>(list.size());

        for (PropertyDescriptor pd : list) {
            map.put(pd.getName(), pd);
        }

        map.remove("class");
        map.remove("serialVersionUID");

        return map;

    }

    protected List<PropertyDescriptor> resolveProperties(Class<?> type) throws IntrospectionException {
        return new ArrayList<>(Arrays.asList(Introspector.getBeanInfo(type).getPropertyDescriptors()));
    }
}
