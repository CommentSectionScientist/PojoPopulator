package de.ossi.pojo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.ossi.pojo.PojoMaker.*;

class PojoMakerTest {

    @Test
    void beanShouldBePopulatedWithDefaultValues() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class).make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getId, Employee::getFirstname, Employee::getStartTime)
                .containsExactly(DEFAULT_NUMBER, DEFAULT_STRING, DEFAULT_LOCALDATETIME);
    }

    @Test
    void beanShouldBePopulatedWithSpecifiedValue() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "firstname", () -> "firstname1")
                .withPropertyValue(String.class, "lastname", () -> "lastname1")
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getFirstname, Employee::getLastname, Employee::getCity)
                .containsExactly("firstname1", "lastname1", DEFAULT_STRING);
    }

    @Test
    void propertyStartingWithSetInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "setProperty", () -> "asd")
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getSetProperty)
                .isEqualTo("asd");
    }

    @Test
    void assigningTheSameClassTwiceShouldThrowException() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "firstname", () -> "1")
                .withPropertyValue(String.class, "firstname", () -> "2")
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getFirstname)
                .isEqualTo("2");
    }

    @Test
    void primitiveInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withDefaultValue(double.class, () -> 2.0)
                .withPropertyValue(boolean.class, "active", () -> true)
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getSalary, Employee::isActive)
                .containsExactly(2.0, true);
    }

    @Test
    void emptyPropertyNameShouldThrowException() {
        //given
        //when
        //then
        Assertions.assertThatException().isThrownBy(() ->
                        new PojoMaker<>(Employee.class).withPropertyValue(String.class, "", () -> "asd"))
                .isInstanceOf(PropertyNameException.class);
    }

    @Test
    void whenUsingNoDefaultSuppliersShouldNotPopulateBean() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "lastname", () -> "lastname1")
                .usingNoDefaultSuppliers()
                .usingDefaultSuppliers()
                .usingNoDefaultSuppliers()
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getFirstname, Employee::getLastname)
                .containsExactly(null, "lastname1");
    }

    @Test
    void whenUsingDefaultSuppliersShouldPopulateBean() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "lastname", () -> "lastname1")
                .usingDefaultSuppliers()
                .usingNoDefaultSuppliers()
                .usingDefaultSuppliers()
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getFirstname, Employee::getLastname)
                .containsExactly(DEFAULT_STRING, "lastname1");
    }

    @Test
    void setterMethodWithoutPrefixShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "noPrefix", () -> "asd")
                .usingNoSetterPrefix()
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::noPrefix)
                .isEqualTo("asd");
    }

    @Test
    void setterMethodWithoutPrefixShouldBePopulatedByDefault() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .usingNoSetterPrefix()
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::noPrefix)
                .isEqualTo(DEFAULT_STRING);
    }

    @Test
    void setterMethodWithDifferentPrefixShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(String.class, "differentPrefix", () -> "asd")
                .usingSetterPrefix("setze")
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::holeDifferentPrefix)
                .isEqualTo("asd");
    }

    @Test
    void setterMethodWithDifferentPrefixShouldBePopulatedByDefault() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .usingSetterPrefix("setze")
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::holeDifferentPrefix)
                .isEqualTo(DEFAULT_STRING);
    }


    @Test
    void pojoFieldInPojoShouldBePopulated() {
        //given
        Employee supervisor = new Employee();
        supervisor.setFirstname("supervisor");
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withPropertyValue(Employee.class, "supervisor", () -> supervisor)
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(e -> e.getSupervisor().getFirstname())
                .isEqualTo("supervisor");
    }

    @Test
    void pojoFieldInPojoShouldNotBePopulatedByDefault() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getSupervisor)
                .isNull();
    }

}