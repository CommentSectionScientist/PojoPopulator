import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * TODO doesnt work with records, because their setters dont have the "set" Prefix
 * TODO Inheritance isnt supported
 * TODO Should primitves and their wrappers be handled differently?
 */
public class PojoMaker<B> {

    public static final String DEFAULT_STRING = "string";
    public static final Number DEFAULT_NUMBER = 1;
    public static final LocalDateTime DEFAULT_LOCALDATETIME = LocalDateTime.of(1970, 1, 1, 1, 1);

    private static final String SETTER_PREFIX = "set";
    private static final String NO_NAME = "";
    private final Class<B> beanClass;
    private final Set<PropertySupplier<?>> propertySuppliers = initDefaultPropertySuppliers();
    private final List<Setter> setters;

    public PojoMaker(Class<B> beanClass) {
        this.beanClass = beanClass;
        setters = getAllSetters(beanClass);
    }

    public <T> PojoMaker<B> withValue(Class<T> clazz, Supplier<T> supplier) {
        return withValue(clazz, NO_NAME, supplier);
    }

    public <T> PojoMaker<B> withValue(Class<T> clazz, String propertyName, Supplier<T> supplier) {
        PropertySupplier<T> newSupplier = new PropertySupplier<>(clazz, propertyName, supplier);
        propertySuppliers.remove(newSupplier);
        propertySuppliers.add(newSupplier);
        return this;
    }

    public B make() {
        try {
            B bean = beanClass.getDeclaredConstructor().newInstance();
            populate(bean);
            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void populate(B bean) throws IllegalAccessException, InvocationTargetException {
        Map<Setter, Optional<PropertySupplier<?>>> settersToPopulate = setters.stream()
                //cant have duplicate keys, because a class can only have on methode with the same name
                .collect(Collectors.toMap(Function.identity(), this::getPropertySupplier));
        settersToPopulate.entrySet()
                .forEach(UncheckedUtil.unchecked(e -> {
                    if (e.getValue().isPresent()) {
                        e.getKey().setter.invoke(bean, e.getValue().get().supplier.get());
                    }
                }));
    }

    private Optional<PropertySupplier<?>> getPropertySupplier(Setter setter) {
        List<PropertySupplier<?>> equalClassSuppliers = propertySuppliers.stream()
                .filter(s -> s.type == setter.type)
                .toList();
        Optional<PropertySupplier<?>> equalFieldNameSupplier = equalClassSuppliers.stream().
                filter(p -> p.propertyName.equals(setter.toPropertyName())).findAny();
        if (equalFieldNameSupplier.isPresent()) {
            return equalFieldNameSupplier;
        }
        //get default PropertySupplier
        return equalClassSuppliers.stream()
                .filter(p -> NO_NAME.equals(p.propertyName))
                .findAny();
    }


    private List<Setter> getAllSetters(Class<B> beanClass) {
        return Arrays.stream(beanClass.getMethods())
                .filter(this::isSetter)
                .map(m -> new Setter(m.getParameterTypes()[0], m))
                .toList();
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith(SETTER_PREFIX) && method.getParameterCount() == 1;
    }

    private Set<PropertySupplier<?>> initDefaultPropertySuppliers() {
        Set<PropertySupplier<?>> defaultPropertySupplier = new HashSet<>();

        defaultPropertySupplier.add(new PropertySupplier<>(Integer.class, NO_NAME, DEFAULT_NUMBER::intValue));
        defaultPropertySupplier.add(new PropertySupplier<>(Long.class, NO_NAME, DEFAULT_NUMBER::longValue));
        defaultPropertySupplier.add(new PropertySupplier<>(Double.class, NO_NAME, DEFAULT_NUMBER::doubleValue));
        defaultPropertySupplier.add(new PropertySupplier<>(Float.class, NO_NAME, DEFAULT_NUMBER::floatValue));

        defaultPropertySupplier.add(new PropertySupplier<>(Boolean.class, NO_NAME, () -> false));

        defaultPropertySupplier.add(new PropertySupplier<>(String.class, NO_NAME, () -> DEFAULT_STRING));
        defaultPropertySupplier.add(new PropertySupplier<>(LocalDateTime.class, NO_NAME, () -> DEFAULT_LOCALDATETIME));
        return defaultPropertySupplier;
    }

    private record Setter(Type type, Method setter) {
        String toPropertyName() {
            String nameWithoutSetPrefix = setter.getName().replaceFirst(SETTER_PREFIX, "");
            return Character.toLowerCase(nameWithoutSetPrefix.charAt(0)) + nameWithoutSetPrefix.substring(1);
        }
    }

    private record PropertySupplier<T>(Type type, String propertyName, Supplier<T> supplier) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PropertySupplier<?> that = (PropertySupplier<?>) o;
            return Objects.equals(type, that.type) && Objects.equals(propertyName, that.propertyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, propertyName);
        }
    }
}
