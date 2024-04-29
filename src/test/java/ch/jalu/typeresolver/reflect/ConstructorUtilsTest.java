package ch.jalu.typeresolver.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ConstructorUtils}.
 */
class ConstructorUtilsTest {

    @Test
    void shouldReturnConstructorIfApplicable() throws NoSuchMethodException {
        // given / when
        Optional<Constructor<Sample>> constr1 = ConstructorUtils.tryFindConstructor(Sample.class, int.class, String.class);
        Optional<Constructor<Sample>> constr2 = ConstructorUtils.tryFindConstructor(Sample.class, int.class, long.class);

        // then
        assertThat(constr1, equalTo(Optional.of(Sample.class.getDeclaredConstructor(int.class, String.class))));
        assertThat(constr2, equalTo(Optional.empty()));
    }

    @Test
    void shouldReturnConstructor() throws NoSuchMethodException {
        // given / when
        Constructor<Sample> constr = ConstructorUtils.getConstructorOrThrow(Sample.class, int.class, String.class);

        // then
        assertThat(constr, equalTo(Sample.class.getDeclaredConstructor(int.class, String.class)));
    }

    @Test
    void shouldThrowForNonExistentConstructor() {
        // given / when
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> ConstructorUtils.getConstructorOrThrow(Sample.class, String.class, int.class));

        // then
        assertThat(ex.getMessage(), equalTo("No constructor on 'class ch.jalu.typeresolver.reflect.ConstructorUtilsTest$Sample' matches the parameter types: [String, int]"));
    }

    @Test
    void shouldInvokeConstructor() throws NoSuchMethodException {
        // given
        Constructor<Sample> constr = Sample.class.getDeclaredConstructor(int.class, String.class);

        // when
        Sample result = ConstructorUtils.invokeConstructor(constr, 3, "test");

        // then
        assertThat(result.size, equalTo(3));
        assertThat(result.str, equalTo("test"));
    }

    @Test
    void shouldWrapIllegalArgumentException() throws NoSuchMethodException {
        // given
        Constructor<Sample> constr = Sample.class.getDeclaredConstructor(int.class, String.class);

        // when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ConstructorUtils.invokeConstructor(constr, true, "test"));

        // then
        assertThat(ex.getMessage(), equalTo("Failed to call constructor for 'class ch.jalu.typeresolver.reflect.ConstructorUtilsTest$Sample'"));
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
    }

    @Test
    void shouldWrapReflectiveOperationException() throws NoSuchMethodException {
        // given
        Constructor<Sample> constr = Sample.class.getDeclaredConstructor(int[].class);

        // when
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> ConstructorUtils.invokeConstructor(constr, new int[0]));

        // then
        assertThat(ex.getMessage(), equalTo("Failed to call constructor for 'class ch.jalu.typeresolver.reflect.ConstructorUtilsTest$Sample'"));
        assertThat(ex.getCause(), instanceOf(IllegalAccessException.class));
    }

    @Test
    void shouldCreateObjectFromZeroArgsConstructor() {
        // given / when
        NoArgsBean noArgsBean = ConstructorUtils.newInstanceFromZeroArgsConstructor(NoArgsBean.class);
        ArrayList arrayList = ConstructorUtils.newInstanceFromZeroArgsConstructor(ArrayList.class);

        // then
        assertThat(noArgsBean, notNullValue());
        assertThat(arrayList, notNullValue());
    }

    @Test
    void shouldCreateObjectFromPrivateConstructor() {
        // given / when / then
        assertThat(ConstructorUtils.newInstanceFromZeroArgsConstructor(ConstructorUtils.class), notNullValue());
    }

    @Test
    void shouldThrowForMissingZeroArgsConstructor() {
        // given / when
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> ConstructorUtils.newInstanceFromZeroArgsConstructor(Integer.class));

        // then
        assertThat(ex.getMessage(), equalTo("Expected class 'java.lang.Integer' to have a zero-args constructor"));
    }

    @Test
    void shouldCreateToStringForConstructor() throws NoSuchMethodException {
        // given
        Constructor<?> constr1 = NoArgsBean.class.getDeclaredConstructor();
        Constructor<?> constr2 = Integer.class.getDeclaredConstructor(int.class);
        Constructor<?> constr3 = Sample.class.getDeclaredConstructor(int.class, String.class);

        // when / then
        assertThat(ConstructorUtils.simpleToString(constr1), equalTo("NoArgsBean()"));
        assertThat(ConstructorUtils.simpleToString(constr2), equalTo("Integer(int)"));
        assertThat(ConstructorUtils.simpleToString(constr3), equalTo("Sample(int, String)"));
    }

    static final class Sample {

        int size;
        String str;

        Sample(int size, String str) {
            this.size = size;
            this.str = str;
        }

        private Sample(int[] array) {
        }
    }

    static final class NoArgsBean {

        public NoArgsBean() {
        }
    }
}