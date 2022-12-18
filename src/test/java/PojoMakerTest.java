import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PojoMakerTest {

    @Test
    void beanShouldBePopulatedWithDefaultValues() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class).withValue(Integer.class, () -> 1).make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getId, Employee::getFirstname)
                .containsExactly(1, PojoMaker.DEFAULT_STRING);
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
                .containsExactly("firstname1", "lastname1", PojoMaker.DEFAULT_STRING);
    }

    @Test
    void dateInBeanShouldBePopulated() {
        //given
        //when
        Employee employee = new PojoMaker<>(Employee.class)
                .withValue(Integer.class, () -> 1)
                .make();
        //then
        Assertions.assertThat(employee)
                .extracting(Employee::getStartTime)
                .isEqualTo(PojoMaker.DEFAULT_LOCALDATETIME);
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
}