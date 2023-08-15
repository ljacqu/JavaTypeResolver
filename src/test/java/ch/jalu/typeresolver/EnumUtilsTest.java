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
 * Test for {@link EnumUtils}.
 */
class EnumUtilsTest {

    /**
     * Tests the Javadoc on {@link EnumUtils#isEnumOrEnumEntryType}.
     */
    @Test
    void shouldHaveValidEnumExtensionExample() {
        Class<?> class1 = NumericShaper.Range.class;
        Class<?> class2 = NumericShaper.Range.ETHIOPIC.getClass(); // NumericShaper$Range$1.class

        boolean r1 = EnumUtils.isEnumOrEnumEntryType(class1);    // = true
        boolean r2 = EnumUtils.isEnumOrEnumEntryType(class2);    // = true
        boolean r3 = EnumUtils.isEnumOrEnumEntryType(null);      // = false
        boolean r4 = EnumUtils.isEnumOrEnumEntryType(int.class); // = false

        assertTrue(r1);
        assertTrue(r2);
        assertFalse(r3);
        assertFalse(r4);
    }

    /**
     * Test the Javadoc on {@link EnumUtils#getAssociatedEnumType}.
     */
    @Test
    void shouldHaveValidJavadocExampleOnGetAssociatedEnumType() {
        Class<?> class1 = NumericShaper.Range.class;
        Class<?> class2 = NumericShaper.Range.ETHIOPIC.getClass(); // NumericShaper$Range$1.class

        Optional<Class<? extends Enum<?>>> r1 = EnumUtils.getAssociatedEnumType(class1);   // = Optional.of(NumericShaper.Range.class)
        Optional<Class<? extends Enum<?>>> r2 = EnumUtils.getAssociatedEnumType(class2);   // = Optional.of(NumericShaper.Range.class)
        Optional<Class<? extends Enum<?>>> r3 = EnumUtils.getAssociatedEnumType(null);     // = Optional.empty()
        Optional<Class<? extends Enum<?>>> r4 = EnumUtils.getAssociatedEnumType(int.class);// = Optional.empty()

        assertThat(r1, equalTo(Optional.of(NumericShaper.Range.class)));
        assertThat(r2, equalTo(Optional.of(NumericShaper.Range.class)));
        assertThat(r3, equalTo(Optional.empty()));
        assertThat(r4, equalTo(Optional.empty()));
    }

    @Test
    void shouldTryToResolveNameToEntry() {
        // given / when / then
        assertThat(EnumUtils.tryValueOf(TestEnum.class, "FIRST"), equalTo(Optional.of(TestEnum.FIRST)));
        assertThat(EnumUtils.tryValueOf(TestEnum.class, "SECOND"), equalTo(Optional.of(TestEnum.SECOND)));
        assertThat(EnumUtils.tryValueOf(TestEnum.class, "THIRD"), equalTo(Optional.of(TestEnum.THIRD)));
        assertThat(EnumUtils.tryValueOf(TimeUnit.class, "MINUTES"), equalTo(Optional.of(TimeUnit.MINUTES)));
        assertThat(EnumUtils.tryValueOf(TestEnum.NestedEnum.class, "SECOND"), equalTo(Optional.of(TestEnum.NestedEnum.SECOND)));

        assertThat(EnumUtils.tryValueOf(null, null), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOf(TestEnum.class, null), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOf(null, "ENTRY"), equalTo(Optional.empty()));

        assertThat(EnumUtils.tryValueOf(TestEnum.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOf(TestEnum.NestedEnum.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOf(HashMap.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOf(TimeUnit.class, "WRONG"), equalTo(Optional.empty()));
    }

    @Test
    void shouldTryToResolveNameToEntryCaseInsensitively() {
        // given / when / then
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.class, "FIRST"), equalTo(Optional.of(TestEnum.FIRST)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.class, "Second"), equalTo(Optional.of(TestEnum.SECOND)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.class, "third"), equalTo(Optional.of(TestEnum.THIRD)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TimeUnit.class, "MINUTES"), equalTo(Optional.of(TimeUnit.MINUTES)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.NestedEnum.class, "SECOND"), equalTo(Optional.of(TestEnum.NestedEnum.SECOND)));

        assertThat(EnumUtils.tryValueOfCaseInsensitive(EnumWithNonStandardEntryNames.class, "FIRST"), equalTo(Optional.of(EnumWithNonStandardEntryNames.first)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(EnumWithNonStandardEntryNames.class, "second"), equalTo(Optional.of(EnumWithNonStandardEntryNames.Second)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(EnumWithNonStandardEntryNames.class, "third"), equalTo(Optional.of(EnumWithNonStandardEntryNames.Third)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(EnumWithNonStandardEntryNames.class, "tHird"), equalTo(Optional.of(EnumWithNonStandardEntryNames.Third)));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(EnumWithNonStandardEntryNames.class, "other"), equalTo(Optional.empty()));

        assertThat(EnumUtils.tryValueOfCaseInsensitive(null, null), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.class, null), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(null, "ENTRY"), equalTo(Optional.empty()));

        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TestEnum.NestedEnum.class, "WRONG"), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(HashMap.class, "wrong"), equalTo(Optional.empty()));
        assertThat(EnumUtils.tryValueOfCaseInsensitive(TimeUnit.class, "WRONG"), equalTo(Optional.empty()));
    }

    @Test
    void shouldDefineWhetherIsEnumRelatedClass() {
        // given / when / then
        assertTrue(EnumUtils.isEnumOrEnumEntryType(TestEnum.class));
        assertTrue(EnumUtils.isEnumOrEnumEntryType(TestEnum.SECOND.getClass()));
        assertTrue(EnumUtils.isEnumOrEnumEntryType(TestEnum.THIRD.getClass()));

        assertFalse(EnumUtils.isEnumOrEnumEntryType(null));
        assertFalse(EnumUtils.isEnumOrEnumEntryType(Object.class));
        assertFalse(EnumUtils.isEnumOrEnumEntryType(boolean[].class));
        assertFalse(EnumUtils.isEnumOrEnumEntryType(Map.Entry.class));
        assertFalse(EnumUtils.isEnumOrEnumEntryType(TestEnum.Inner.class));
        assertFalse(EnumUtils.isEnumOrEnumEntryType(TestEnum.Inner.Inner2d.class));
        assertFalse(EnumUtils.isEnumOrEnumEntryType(TestEnum.SECOND.getClass().getDeclaredClasses()[0]));
    }

    @Test
    void shouldCastClassAsEnumExtensionIfApplicable() {
        // given
        Class<?> clazz1 = TestEnum.class;
        Class<?> clazz2 = TestEnum.SECOND.getClass();
        Class<?> clazz3 = TestEnum.Inner.class;
        Class<?> clazz4 = TestEnum.SECOND.getClass().getDeclaredClasses()[0];
        Class<?> clazz5 = TestEnum.NestedEnum.class;
        Class<?> clazz6 = TestEnum.NestedEnum.SECOND.getClass();

        // when
        Optional<Class<? extends Enum<?>>> result1 = EnumUtils.asEnumClassIfPossible(clazz1);
        Optional<Class<? extends Enum<?>>> result2 = EnumUtils.asEnumClassIfPossible(clazz2);
        Optional<Class<? extends Enum<?>>> result3 = EnumUtils.asEnumClassIfPossible(clazz3);
        Optional<Class<? extends Enum<?>>> result4 = EnumUtils.asEnumClassIfPossible(clazz4);
        Optional<Class<? extends Enum<?>>> result5 = EnumUtils.asEnumClassIfPossible(clazz5);
        Optional<Class<? extends Enum<?>>> result6 = EnumUtils.asEnumClassIfPossible(clazz6);

        // then
        assertThat(result1, equalTo(Optional.of(TestEnum.class)));
        assertThat(result2, equalTo(Optional.empty()));
        assertThat(result3, equalTo(Optional.empty()));
        assertThat(result4, equalTo(Optional.empty()));
        assertThat(result5, equalTo(Optional.of(TestEnum.NestedEnum.class)));
        assertThat(result6, equalTo(Optional.empty()));

        assertThat(EnumUtils.getAssociatedEnumType(null), equalTo(Optional.empty()));
        assertThat(EnumUtils.getAssociatedEnumType(String.class), equalTo(Optional.empty()));
        assertThat(EnumUtils.getAssociatedEnumType(double[][].class), equalTo(Optional.empty()));
        assertThat(EnumUtils.getAssociatedEnumType(List.class), equalTo(Optional.empty()));
    }

    @Test
    void shouldReturnAssociatedEnumClassIfApplicable() {
        // given
        Class<?> clazz1 = TestEnum.class;
        Class<?> clazz2 = TestEnum.SECOND.getClass();
        Class<?> clazz3 = TestEnum.Inner.class;
        Class<?> clazz4 = TestEnum.SECOND.getClass().getDeclaredClasses()[0];
        Class<?> clazz5 = TestEnum.NestedEnum.class;
        Class<?> clazz6 = TestEnum.NestedEnum.SECOND.getClass();

        // when
        Optional<Class<? extends Enum<?>>> result1 = EnumUtils.getAssociatedEnumType(clazz1);
        Optional<Class<? extends Enum<?>>> result2 = EnumUtils.getAssociatedEnumType(clazz2);
        Optional<Class<? extends Enum<?>>> result3 = EnumUtils.getAssociatedEnumType(clazz3);
        Optional<Class<? extends Enum<?>>> result4 = EnumUtils.getAssociatedEnumType(clazz4);
        Optional<Class<? extends Enum<?>>> result5 = EnumUtils.getAssociatedEnumType(clazz5);
        Optional<Class<? extends Enum<?>>> result6 = EnumUtils.getAssociatedEnumType(clazz6);

        // then
        assertThat(result1, equalTo(Optional.of(TestEnum.class)));
        assertThat(result2, equalTo(Optional.of(TestEnum.class)));
        assertThat(result3, equalTo(Optional.empty()));
        assertThat(result4, equalTo(Optional.empty()));
        assertThat(result5, equalTo(Optional.of(TestEnum.NestedEnum.class)));
        assertThat(result6, equalTo(Optional.of(TestEnum.NestedEnum.class)));

        assertThat(EnumUtils.getAssociatedEnumType(null), equalTo(Optional.empty()));
        assertThat(EnumUtils.getAssociatedEnumType(String.class), equalTo(Optional.empty()));
        assertThat(EnumUtils.getAssociatedEnumType(double[][].class), equalTo(Optional.empty()));
        assertThat(EnumUtils.getAssociatedEnumType(List.class), equalTo(Optional.empty()));
    }

    @Test
    void shouldProvideCollectorToEnumSet() {
        // given / when
        EnumSet<TestEnum> result1 = Stream.of(TestEnum.FIRST, TestEnum.THIRD)
            .collect(EnumUtils.toEnumSet(TestEnum.class));
        EnumSet<TestEnum> result2 = Stream.of(TestEnum.THIRD, TestEnum.FIRST, TestEnum.THIRD)
            .collect(EnumUtils.toEnumSet(TestEnum.class));
        EnumSet<TestEnum> result3 = Stream.<TestEnum>empty()
            .collect(EnumUtils.toEnumSet(TestEnum.class));

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

            SECOND() {
            }

        }

        static class Inner {

            static class Inner2d {

            }
        }
    }

    private enum EnumWithNonStandardEntryNames {

        first,
        Second,
        Third, // 1
        tHird // 2
    }
}