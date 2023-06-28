package ch.jalu.typeresolver;

import org.junit.jupiter.api.Test;

import java.awt.font.NumericShaper;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link EnumUtil}.
 */
class EnumUtilTest {

    /**
     * Tests the Javadoc on {@link EnumUtil#isEnumOrEnumEntryType}.
     */
    @Test
    void shouldHaveValidEnumExtensionExample() {
        Class<?> class1 = NumericShaper.Range.class;
        Class<?> class2 = NumericShaper.Range.ETHIOPIC.getClass(); // NumericShaper$Range$1.class

        boolean r1 = EnumUtil.isEnumOrEnumEntryType(class1);    // = true
        boolean r2 = EnumUtil.isEnumOrEnumEntryType(class2);    // = true
        boolean r3 = EnumUtil.isEnumOrEnumEntryType(null);      // = false
        boolean r4 = EnumUtil.isEnumOrEnumEntryType(int.class); // = false

        assertTrue(r1);
        assertTrue(r2);
        assertFalse(r3);
        assertFalse(r4);
    }

    /**
     * Test the Javadoc on {@link EnumUtil#asEnumType}.
     */
    @Test
    void shouldHaveValidJavadocExampleOnAsEnumType() {
        Class<?> class1 = NumericShaper.Range.class;
        Class<?> class2 = NumericShaper.Range.ETHIOPIC.getClass(); // NumericShaper$Range$1.class

        Optional<Class<? extends Enum<?>>> r1 = EnumUtil.asEnumType(class1);   // = Optional.of(NumericShaper.Range.class)
        Optional<Class<? extends Enum<?>>> r2 = EnumUtil.asEnumType(class2);   // = Optional.of(NumericShaper.Range.class)
        Optional<Class<? extends Enum<?>>> r3 = EnumUtil.asEnumType(null);// = Optional.empty()
        Optional<Class<? extends Enum<?>>> r4 = EnumUtil.asEnumType(int.class);// = Optional.empty()

        assertThat(r1, equalTo(Optional.of(NumericShaper.Range.class)));
        assertThat(r2, equalTo(Optional.of(NumericShaper.Range.class)));
        assertThat(r3, equalTo(Optional.empty()));
        assertThat(r4, equalTo(Optional.empty()));
    }

    @Test
    void shouldTryToResolveNameToEntry() {
        // given / when / then
        assertThat(EnumUtil.tryValueOf(TestEnum.class, "FIRST"), equalTo(Optional.of(TestEnum.FIRST)));
        assertThat(EnumUtil.tryValueOf(TestEnum.class, "SECOND"), equalTo(Optional.of(TestEnum.SECOND)));
        assertThat(EnumUtil.tryValueOf(TestEnum.class, "THIRD"), equalTo(Optional.of(TestEnum.THIRD)));
        assertThat(EnumUtil.tryValueOf(TimeUnit.class, "MINUTES"), equalTo(Optional.of(TimeUnit.MINUTES)));
        assertThat(EnumUtil.tryValueOf(TestEnum.NestedEnum.class, "SECOND"), equalTo(Optional.of(TestEnum.NestedEnum.SECOND)));

        assertThat(EnumUtil.tryValueOf(null, null), equalTo(Optional.empty()));
        assertThat(EnumUtil.tryValueOf(TestEnum.class, null), equalTo(Optional.empty()));
        assertThat(EnumUtil.tryValueOf(null, "ENTRY"), equalTo(Optional.empty()));

        assertThat(EnumUtil.tryValueOf(TestEnum.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtil.tryValueOf(TestEnum.NestedEnum.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtil.tryValueOf(HashMap.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtil.tryValueOf(TimeUnit.class, "WRONG"), equalTo(Optional.empty()));
    }

    @Test
    void shouldDefineWhetherIsEnumRelatedClass() {
        // given / when / then
        assertTrue(EnumUtil.isEnumOrEnumEntryType(TestEnum.class));
        assertTrue(EnumUtil.isEnumOrEnumEntryType(TestEnum.SECOND.getClass()));
        assertTrue(EnumUtil.isEnumOrEnumEntryType(TestEnum.THIRD.getClass()));

        assertFalse(EnumUtil.isEnumOrEnumEntryType(null));
        assertFalse(EnumUtil.isEnumOrEnumEntryType(Object.class));
        assertFalse(EnumUtil.isEnumOrEnumEntryType(boolean[].class));
        assertFalse(EnumUtil.isEnumOrEnumEntryType(Map.Entry.class));
        assertFalse(EnumUtil.isEnumOrEnumEntryType(TestEnum.Inner.class));
        assertFalse(EnumUtil.isEnumOrEnumEntryType(TestEnum.Inner.Inner2d.class));
        assertFalse(EnumUtil.isEnumOrEnumEntryType(TestEnum.SECOND.getClass().getDeclaredClasses()[0]));
    }

    @Test
    void shouldReturnEnumClassIfApplicable() {
        // given
        Class<?> clazz1 = TestEnum.class;
        Class<?> clazz2 = TestEnum.SECOND.getClass();
        Class<?> clazz3 = TestEnum.Inner.class;
        Class<?> clazz4 = TestEnum.SECOND.getClass().getDeclaredClasses()[0];
        Class<?> clazz5 = TestEnum.NestedEnum.class;
        Class<?> clazz6 = TestEnum.NestedEnum.SECOND.getClass();

        // when
        Optional<Class<? extends Enum<?>>> result1 = EnumUtil.asEnumType(clazz1);
        Optional<Class<? extends Enum<?>>> result2 = EnumUtil.asEnumType(clazz2);
        Optional<Class<? extends Enum<?>>> result3 = EnumUtil.asEnumType(clazz3);
        Optional<Class<? extends Enum<?>>> result4 = EnumUtil.asEnumType(clazz4);
        Optional<Class<? extends Enum<?>>> result5 = EnumUtil.asEnumType(clazz5);
        Optional<Class<? extends Enum<?>>> result6 = EnumUtil.asEnumType(clazz6);

        // then
        assertThat(result1, equalTo(Optional.of(TestEnum.class)));
        assertThat(result2, equalTo(Optional.of(TestEnum.class)));
        assertThat(result3, equalTo(Optional.empty()));
        assertThat(result4, equalTo(Optional.empty()));
        assertThat(result5, equalTo(Optional.of(TestEnum.NestedEnum.class)));
        assertThat(result6, equalTo(Optional.of(TestEnum.NestedEnum.class)));

        assertThat(EnumUtil.asEnumType(null), equalTo(Optional.empty()));
        assertThat(EnumUtil.asEnumType(String.class), equalTo(Optional.empty()));
        assertThat(EnumUtil.asEnumType(double[][].class), equalTo(Optional.empty()));
        assertThat(EnumUtil.asEnumType(List.class), equalTo(Optional.empty()));
    }

    @Test
    void shouldProvideCollectorToEnumSet() {
        // given / when
        EnumSet<TestEnum> result1 = Stream.of(TestEnum.FIRST, TestEnum.THIRD)
            .collect(EnumUtil.toEnumSet(TestEnum.class));
        EnumSet<TestEnum> result2 = Stream.of(TestEnum.THIRD, TestEnum.FIRST, TestEnum.THIRD)
            .collect(EnumUtil.toEnumSet(TestEnum.class));
        EnumSet<TestEnum> result3 = Stream.<TestEnum>empty()
            .collect(EnumUtil.toEnumSet(TestEnum.class));

        // then
        assertThat(result1, contains(TestEnum.FIRST, TestEnum.THIRD));
        assertThat(result2, contains(TestEnum.FIRST, TestEnum.THIRD));
        assertThat(result3, empty());
    }

    private enum TestEnum {

        FIRST,

        SECOND() {
            class InsideSecondClass { // Referenced via TestEnum.SECOND.getClass().getDeclaredClasses()[0];

            }
        },

        THIRD() {

        };

        private enum NestedEnum {

            FIRST,

            SECOND()

        }

        static class Inner {

            static class Inner2d {

            }
        }
    }
}