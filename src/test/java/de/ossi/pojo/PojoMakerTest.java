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
                .withValue(String.class, "firstname", () -> "firstname1")
                .withValue(String.class, "lastname", () -> "lastname1")
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
                .withValue(String.class, "setProperty", () -> "asd")
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
                .withValue(String.class, "firstname", () -> "1")
                .withValue(String.class, "firstname", () -> "2")
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
                .withValue(double.class, () -> 2.0)
                .withValue(boolean.class, "active", () -> true)
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
                        new PojoMaker<>(Employee.class).withValue(String.class, "", () -> "asd"))
                .isInstanceOf(PropertyNameException.class);
    }

    @Test
    void whenUsingNoDefaultSuppliersShouldNotPopulateBean() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withValue(String.class, "lastname", () -> "lastname1")
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
                .withValue(String.class, "lastname", () -> "lastname1")
                .usingDefaultSuppliers()
                .usingNoDefaultSuppliers()
                .usingDefaultSuppliers()
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getFirstname, Employee::getLastname)
                .containsExactly(DEFAULT_STRING, "lastname1");
    }
}