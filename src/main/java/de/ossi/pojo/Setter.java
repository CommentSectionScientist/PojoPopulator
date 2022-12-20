package de.ossi.pojo;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

record Setter(Type type, Method setter) {

    /**
     * Tries to find the property name from the name of its setter method name.
     * Presumes, that the setter method has a SETTER_PREFIX followed by an upper case letter followed by the name of the property.
     */
    String toPropertyName(String setterPrefix) {
        String nameWithoutSetPrefix = setter.getName().replaceFirst(setterPrefix, "");
        return Character.toLowerCase(nameWithoutSetPrefix.charAt(0)) + nameWithoutSetPrefix.substring(1);
    }
}
