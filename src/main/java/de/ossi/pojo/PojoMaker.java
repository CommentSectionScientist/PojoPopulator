package de.ossi.pojo;

import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * TODO Inheritance isn't supported
 */
public class PojoMaker<B> {

    public static final String DEFAULT_STRING = "string";
    public static final Number DEFAULT_NUMBER = 1;
    public static final LocalDateTime DEFAULT_LOCALDATETIME = LocalDateTime.of(1970, 1, 1, 1, 1);
    private static final String DEFAULT_SETTER_PREFIX = "set";

    private final Set<PropertySupplier<?>> propertySuppliers = new HashSet<>();
    private final Class<B> beanClass;
    private boolean usingDefaultSuppliers = true;
    private String setterPrefix = DEFAULT_SETTER_PREFIX;

    public PojoMaker(Class<B> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Sets a supplier to be used to populate <b>>all</b properties of the specified type in the bean.
     * But only if no other property supplier specified by {@link PojoMaker#withValue(String, Supplier)}.
     *
     * @param propertyClass the Type of the property fields be populated
     * @param supplier      the supplier to be used to populate the property fields
     */
    public <T> PojoMaker<B> withValue(@NonNull Class<T> propertyClass, Supplier<T> supplier) {
        PropertySupplier<T> newSupplier = new PropertySupplier<>(propertyClass, null, supplier);
        addSupplier(newSupplier);
        return this;
    }

    /**
     * Sets a supplier to be used to populate the specified field in the bean with the property name.
     * The type of the object provided by the supplier must be the same as the property field.
     * Will be used prior to the supplier provided by {@link PojoMaker#withValue(Class, Supplier)}.
     *
     * @param propertyName <b>cannot be empty!</b>
     * @param supplier     the supplier to be used to populate the property field
     */
    public <T> PojoMaker<B> withValue(@NonNull String propertyName, Supplier<T> supplier) {
        checkPropertyName(propertyName);
        PropertySupplier<T> newSupplier = new PropertySupplier<>(null, propertyName, supplier);
        addSupplier(newSupplier);
        return this;
    }

    private <T> void addSupplier(PropertySupplier<T> newSupplier) {
        propertySuppliers.remove(newSupplier);
        propertySuppliers.add(newSupplier);
    }

    private void checkPropertyName(String propertyName) {
        if ("".equals(propertyName)) {
            throw new IllegalArgumentException("The property name can not be empty.");
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
     * The NoArgs Constructor has to be public accessible or a {@link RuntimeException} may be thrown.
     * Invokes all Setters per Reflection.
     *
     * @see Class#getDeclaredConstructor(Class[])
     * @see java.lang.reflect.Constructor#newInstance(Object...)
     * @see Method#invoke(Object, Object...)
     */
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
        if (usingDefaultSuppliers) {
            //Set only adds if no other supplier is already present
            propertySuppliers.addAll(createDefaultPropertySuppliers());
        }
        Map<Setter, Optional<PropertySupplier<?>>> settersToPopulate = getAllSetters(beanClass).stream()
                //cant have duplicate keys, because a class can only have on methode with the same name
                .collect(Collectors.toMap(Function.identity(), this::getPropertySupplier));
        settersToPopulate.entrySet()
                .forEach(UncheckedUtil.unchecked(e -> {
                    if (e.getValue().isPresent()) {
                        Object suppliedValue = e.getValue().get().supplier.get();
                        e.getKey().setter.invoke(bean, suppliedValue);
                    }
                }));
    }

    /**
     * Search for a matching property supplier.
     * If a supplier matching the property name, then this one will be used.
     * If a supplier matching the class is found, then this one will be used.
     * If not supplier matching is found, the Optional returned is empty.
     */
    private Optional<PropertySupplier<?>> getPropertySupplier(Setter setter) {
        Optional<PropertySupplier<?>> equalFieldNameSupplier = propertySuppliers.stream()
                //the type of the field is irrelevant, because there can only be one field with the same name in the class
                .filter(s -> s.type == null)
                .filter(p -> p.propertyName.equals(toPropertyName(setter)))
                .findAny();
        if (equalFieldNameSupplier.isPresent()) {
            return equalFieldNameSupplier;
        }
        //get default PropertySupplier
        return propertySuppliers.stream()
                .filter(s -> s.type == setter.type)
                .filter(p -> p.propertyName == null)
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

        defaultPropertySupplier.add(new PropertySupplier<>(Integer.class, null, DEFAULT_NUMBER::intValue));
        defaultPropertySupplier.add(new PropertySupplier<>(Long.class, null, DEFAULT_NUMBER::longValue));
        defaultPropertySupplier.add(new PropertySupplier<>(Double.class, null, DEFAULT_NUMBER::doubleValue));
        defaultPropertySupplier.add(new PropertySupplier<>(Float.class, null, DEFAULT_NUMBER::floatValue));

        defaultPropertySupplier.add(new PropertySupplier<>(Boolean.class, null, () -> false));

        defaultPropertySupplier.add(new PropertySupplier<>(String.class, null, () -> DEFAULT_STRING));
        defaultPropertySupplier.add(new PropertySupplier<>(LocalDateTime.class, null, () -> DEFAULT_LOCALDATETIME));
        return defaultPropertySupplier;
    }

    /**
     * Tries to find the property name from the name of its setter method name.
     * Presumes, that the setter method has a SETTER_PREFIX followed by an upper case letter followed by the name of the property.
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
