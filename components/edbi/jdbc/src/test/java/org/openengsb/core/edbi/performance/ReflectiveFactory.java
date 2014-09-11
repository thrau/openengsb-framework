package org.openengsb.core.edbi.performance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
* ReflectiveFactory
*/
public class ReflectiveFactory<T> {
    private final Class<T> clazz;
    private final Object[] params;
    private final Constructor<T> constructor;

    public ReflectiveFactory(Class<T> clazz, Object... params) {
        this.clazz = clazz;
        this.params = params;
        try {
            this.constructor = clazz.getConstructor(paramClasses(params));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get constructor for " + clazz, e);
        }
    }

    public T newInstance() {
        try {
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to instantiate " + clazz + " with " + Arrays.deepToString(params), e);
        }
    }

    private Class[] paramClasses(Object[] params) {
        Class[] classes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            classes[i] = params[i].getClass();
        }
        return classes;
    }
}
