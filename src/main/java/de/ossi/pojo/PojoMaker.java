package de.ossi.pojo;

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
 * TODO option to set setter method prefix
 * TODO use lombok
 */
public class PojoMaker<B> {

    public static final String DEFAULT_STRING = "string";
    public static final Number DEFAULT_NUMBER = 1;
    public static final LocalDateTime DEFAULT_LOCALDATETIME = LocalDateTime.of(1970, 1, 1, 1, 1);

    private static final String DEFAULT_SETTER_PREFIX = "set";
    private static final String NO_NAME = "";
    private final Class<B> beanClass;
    private final Set<PropertySupplier<?>> propertySuppliers = new HashSet<>();
    private final List<Setter> setters = new ArrayList<>();
    private boolean usingDefaultSuppliers = true;
    private String setterPrefix = DEFAULT_SETTER_PREFIX;

    public PojoMaker(Class<B> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Sets a supplier to be used to populate all properties of the specified type.
     * only if no other property supplier specified by {@link PojoMaker#withPropertyValue(Class, String, Supplier)}.
     *
     * @param clazz    the Type of the property fields be populated
     * @param supplier the supplier to be used to populate the property fields
     */
    public <T> PojoMaker<B> withDefaultValue(Class<T> clazz, Supplier<T> supplier) {
        PropertySupplier<T> newSupplier = new PropertySupplier<>(clazz, NO_NAME, supplier);
        return addSupplier(newSupplier);
    }

    /**
     * Sets a supplier to be used to populate the specified property of the type provided.
     * Will be used prior to the supplier provided by {@link PojoMaker#withDefaultValue(Class, Supplier)}.
     *
     * @param clazz        the Type of the property field to be populated
     * @param propertyName <b>cannot be empty!</b>
     * @param supplier     the supplier to be used to populate the property field
     */
    public <T> PojoMaker<B> withPropertyValue(Class<T> clazz, String propertyName, Supplier<T> supplier) {
        checkPropertyName(propertyName);
        PropertySupplier<T> newSupplier = new PropertySupplier<>(clazz, propertyName, supplier);
        addSupplier(newSupplier);
        return this;
    }

    private <T> PojoMaker<B> addSupplier(PropertySupplier<T> newSupplier) {
        propertySuppliers.remove(newSupplier);
        propertySuppliers.add(newSupplier);
        return this;
    }

    private void checkPropertyName(String propertyName) {
        if (propertyName == null || NO_NAME.equals(propertyName)) {
            throw new PropertyNameException("The Propertname can not be empty.");
        }
    }

    /**
     * Sets the option to <b>not</b> use default suppliers to populate the property fields.
     * If no other suppliers are provided for a field type, these fields will not be initialized -> null.
     */
    public PojoMaker<B> usingNoDefaultSuppliers() {
        usingDefaultSuppliers = false;
        return this;
    }


    /**
     * Sets the option to <b>use</b> default suppliers to populate the property fields.
     */
    public PojoMaker<B> usingDefaultSuppliers() {
        usingDefaultSuppliers = true;
        return this;
    }

    public PojoMaker<B> usingSetterPrefix(String setterPrefix) {
        this.setterPrefix = setterPrefix;
        return this;
    }

    public PojoMaker<B> usingNoSetterPrefix() {
        this.setterPrefix = "";
        return this;
    }

    /**
     * Populates the bean property fields with the suppliers provided or default suppliers of not otherwise specified.
     * The NoArgs Constructor has to be public accessable or a {@link RuntimeException} may be thrown.
     * Invokes all Setters per Reflection.
     *
     * @see Class#getDeclaredConstructor(Class[])
     * @see java.lang.reflect.Constructor#newInstance(Object...)
     * @see Method#invoke(Object, Object...)
     */
    public B make() {
        setters.addAll(getAllSetters(beanClass));
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
        if (usingDefaultSuppliers) {
            //Set only adds if no default Supplier is already provided
            propertySuppliers.addAll(createDefaultPropertySuppliers());
        }
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

    /**
     * Search for a matching property supplier.
     * If a supplier matching the class & propertyname, then this one will be used.
     * If a supplier only matching the class is found, then this one will be used.
     * If not supplier matching is found, the Optional returned is empty.
     */
    private Optional<PropertySupplier<?>> getPropertySupplier(Setter setter) {
        List<PropertySupplier<?>> equalClassSuppliers = propertySuppliers.stream()
                .filter(s -> s.type == setter.type)
                .toList();
        Optional<PropertySupplier<?>> equalFieldNameSupplier = equalClassSuppliers.stream().
                filter(p -> p.propertyName.equals(toPropertyName(setter))).findAny();
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
        return method.getName().startsWith(setterPrefix) && method.getParameterCount() == 1;
    }

    private Set<PropertySupplier<?>> createDefaultPropertySuppliers() {
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

    /**
     * Tries to find the porpertyname from the name of its setter method name.
     * Presumes, that the setter method has a SETTER_PREFIX following by an upper case letter followed by the name of the property.
     */
    String toPropertyName(Setter setter) {
        String nameWithoutSetPrefix = setter.setter.getName().replaceFirst(setterPrefix, "");
        return Character.toLowerCase(nameWithoutSetPrefix.charAt(0)) + nameWithoutSetPrefix.substring(1);
    }


    private record Setter(Type type, Method setter) {

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
