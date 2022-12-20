package de.ossi.pojo;

import lombok.NonNull;

import java.lang.reflect.Method;
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
}
