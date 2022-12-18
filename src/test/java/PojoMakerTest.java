import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class PojoMakerTest {

    @Test
    void beanShouldBePopulatedWithDefaultValues() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class).withValue(Integer.class, () -> 1).make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getId, Employee::getFirstname)
                .containsExactly(1, "string");
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
                .containsExactly("firstname1", "lastname1", "string");
    }

    @Test
    void dateInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withValue(Integer.class, () -> 1)
                .make();
        //then
        Assertions.assertThat(employee.getStartTime())
                .isEqualToIgnoringMinutes(LocalDateTime.now());
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
    void assigningTheSameClassTwiceShouldUseSecond() {
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

    @Disabled("NotYetImplemented")
    @Test
    void primitiveInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withValue(Integer.class, () -> 1)
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getSalary)
                .isEqualTo(3.0);
    }

}