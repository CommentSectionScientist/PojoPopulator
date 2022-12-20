package de.ossi.pojo;

import org.junit.jupiter.api.Test;

import static de.ossi.pojo.PojoPopulator.*;
import static org.assertj.core.api.Assertions.*;

class PojoPopulatorTest {

    @Test
    void beanShouldBePopulatedWithDefaultValues() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class).make();
        //then
        assertThat(employee)
                .extracting(Employee::getId, Employee::getFirstname, Employee::getStartTime)
                .containsExactly(DEFAULT_NUMBER, DEFAULT_STRING, DEFAULT_LOCALDATETIME);
    }

    @Test
    void beanShouldBePopulatedWithSpecifiedValue() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("firstname", () -> "firstname1")
                .withValue("lastname", () -> "lastname1")
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::getFirstname, Employee::getLastname, Employee::getCity)
                .containsExactly("firstname1", "lastname1", DEFAULT_STRING);
    }

    @Test
    void propertyStartingWithSetInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("setProperty", () -> "asd")
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::getSetProperty)
                .isEqualTo("asd");
    }

    @Test
    void assigningTheSameClassTwiceShouldThrowException() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("firstname", () -> "1")
                .withValue("firstname", () -> "2")
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::getFirstname)
                .isEqualTo("2");
    }

    @Test
    void primitiveInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue(double.class, () -> 2.0)
                .withValue("active", () -> true)
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::getSalary, Employee::isActive)
                .containsExactly(2.0, true);
    }

    @Test
    void emptyPropertyNameShouldThrowException() {
        //given
        //when
        //then
        assertThatIllegalArgumentException().isThrownBy(() ->
                        new PojoPopulator<>(Employee.class).withValue("", () -> "asd"))
                .withMessageContaining("property name");
    }

    @Test
    void nullPropertyNameShouldThrowException() {
        //given
        //when
        //then
        assertThatNullPointerException().isThrownBy(() ->
                        new PojoPopulator<>(Employee.class).withValue((String) null, () -> "asd"))
                .withMessageContaining("propertyName");
    }

    @Test
    void emptyClassShouldThrowException() {
        //given
        //when
        //then
        assertThatNullPointerException().isThrownBy(() ->
                        new PojoPopulator<>(Employee.class).withValue((Class<String>) null, () -> "asd"))
                .withMessageContaining("propertyClass");
    }

    @Test
    void whenUsingNoDefaultSuppliersShouldNotPopulateBean() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("lastname", () -> "lastname1")
                .usingNoDefaultSuppliers()
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::getFirstname, Employee::getLastname)
                .containsExactly(null, "lastname1");
    }

    @Test
    void setterMethodWithoutPrefixShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("noPrefix", () -> "asd")
                .usingNoSetterPrefix()
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::noPrefix)
                .isEqualTo("asd");
    }

    @Test
    void setterMethodWithoutPrefixShouldBePopulatedByDefault() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .usingNoSetterPrefix()
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::noPrefix)
                .isEqualTo(DEFAULT_STRING);
    }

    @Test
    void setterMethodWithDifferentPrefixShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("differentPrefix", () -> "asd")
                .usingSetterPrefix("setze")
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::holeDifferentPrefix)
                .isEqualTo("asd");
    }

    @Test
    void setterMethodWithDifferentPrefixShouldBePopulatedByDefault() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .usingSetterPrefix("setze")
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::holeDifferentPrefix)
                .isEqualTo(DEFAULT_STRING);
    }


    @Test
    void pojoFieldInPojoShouldBePopulated() {
        //given
        Employee supervisor = new Employee();
        supervisor.setFirstname("supervisor");
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .withValue("supervisor", () -> supervisor)
                .make();
        //then
        assertThat(employee)
                .extracting(e -> e.getSupervisor().getFirstname())
                .isEqualTo("supervisor");
    }

    @Test
    void pojoFieldInPojoShouldNotBePopulatedByDefault() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .make();
        //then
        assertThat(employee)
                .extracting(Employee::getSupervisor)
                .isNull();
    }

    @Test
    void whenUsingRandomValuesShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoPopulator<>(Employee.class)
                .usingRandomDefaultValues()
                .make();
        //then
        //Testing of Random Values is not possible
        assertThat(employee)
                .extracting(Employee::getStartDay, Employee::getStartTime, Employee::getId, Employee::getTechnical_id, Employee::getSalary, Employee::getSalaryf)
                .doesNotContainNull();
    }

}