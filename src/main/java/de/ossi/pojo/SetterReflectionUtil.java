package de.ossi.pojo;

import lombok.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class SetterReflectionUtil {
    @SafeVarargs
    static List<Setter> getAllSetters(Class<?> beanClass, @NonNull Predicate<Method>... filters) {
        Predicate<Method> allPredicate = Arrays.stream(filters).reduce(Predicate::and).orElse(p -> true);
        return Arrays.stream(beanClass.getMethods())
                .filter(allPredicate)
                .map(m -> new Setter(m.getParameterTypes()[0], m))
                .toList();
    }

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
}
