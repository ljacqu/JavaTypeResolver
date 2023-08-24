package ch.jalu.typeresolver.classutil;

import ch.jalu.typeresolver.TypeInfo;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.font.NumericShaper;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static ch.jalu.typeresolver.classutil.ClassUtils.getSemanticType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link ClassUtils}.
 */
class ClassUtilsTest {

    /** Tests the Javadoc on {@link ClassUtils#getSemanticType}. */
    @Test
    void shouldHaveValidJavadocExample() {
        Class<?> r1 = getSemanticType(null);
        assertThat(r1, nullValue());

        Class<?> r2a = NumericShaper.Range.ETHIOPIC.getClass(); //  = NumericShaper$Range$1.class // or similar
        Class<?> r2b = getSemanticType(NumericShaper.Range.ETHIOPIC); // = NumericShaper$Range.class
        assertThat(r2a.getName(), matchesRegex("java\\.awt\\.font\\.NumericShaper\\$Range\\$\\d+"));
        assertThat(r2b, equalTo(NumericShaper.Range.class));

        FunctionalInterface fiAnnotation = Runnable.class.getAnnotation(FunctionalInterface.class);
        Class<?> r3a = fiAnnotation.getClass(); // = $Proxy12.class // or similar
        Class<?> r3b = getSemanticType(fiAnnotation); //  = FunctionalInterface.class
        assertThat(r3a.getSimpleName(), matchesRegex("\\$Proxy\\d+"));
        assertThat(r3b, equalTo(FunctionalInterface.class));
    }

    @Nested
    class ClassLoadingUtilities {

        @Test
        void shouldCheckWhetherClassExists() {
            // given / when / then
            assertThat(ClassUtils.classExists("java.lang.String"), equalTo(true));
            assertThat(ClassUtils.classExists("ch.jalu.typeresolver.TypeInfo"), equalTo(true));
            assertThat(ClassUtils.classExists(Test.class.getCanonicalName()), equalTo(true));

            assertThat(ClassUtils.classExists("java.lang.DoesNotExist"), equalTo(false));
            assertThat(ClassUtils.classExists("ch.jalu.typeresolver.Bogus"), equalTo(false));
        }

        @Test
        void shouldLoadClassIfPossible() {
            // given / when / then
            assertThat(ClassUtils.tryLoadClass("java.lang.String"), equalTo(Optional.of(String.class)));
            assertThat(ClassUtils.tryLoadClass("ch.jalu.typeresolver.TypeInfo"), equalTo(Optional.of(TypeInfo.class)));
            assertThat(ClassUtils.tryLoadClass(Test.class.getCanonicalName()), equalTo(Optional.of(Test.class)));

            assertThat(ClassUtils.tryLoadClass("java.lang.DoesNotExist"), equalTo(Optional.empty()));
            assertThat(ClassUtils.tryLoadClass("ch.jalu.typeresolver.Bogus"), equalTo(Optional.empty()));
        }

        @Test
        void shouldLoadClassOrThrowException() {
            // given / when / then
            assertThat(ClassUtils.loadClassOrThrow("java.lang.String"), equalTo(String.class));
            assertThat(ClassUtils.loadClassOrThrow("ch.jalu.typeresolver.TypeInfo"), equalTo(TypeInfo.class));
            assertThat(ClassUtils.loadClassOrThrow(Test.class.getCanonicalName()), equalTo(Test.class));

            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> ClassUtils.loadClassOrThrow("java.lang.DoesNotExist"));
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> ClassUtils.loadClassOrThrow("ch.jalu.typeresolver.Bogus"));

            assertThat(ex1.getMessage(), equalTo("Class 'java.lang.DoesNotExist' could not be loaded"));
            assertThat(ex1.getCause(), instanceOf(ClassNotFoundException.class));
            assertThat(ex2.getMessage(), equalTo("Class 'ch.jalu.typeresolver.Bogus' could not be loaded"));
            assertThat(ex2.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }

    @Nested
    class SubClassAndCastingUtils {

        @Test
        void shouldCastToTargetType() {
            // given / when
            Optional<Number> result1 = ClassUtils.tryCast(3, Number.class);
            Optional<String> result2 = ClassUtils.tryCast("str", String.class);
            Optional<Serializable> result3 = ClassUtils.tryCast("str", Serializable.class);
            Integer[] integerArray = {1, 2, 3};
            Optional<Number[]> result4 = ClassUtils.tryCast(integerArray, Number[].class);

            // then
            assertThat(result1, equalTo(Optional.of(3)));
            assertThat(result2, equalTo(Optional.of("str")));
            assertThat(result3, equalTo(Optional.of("str")));
            assertThat(result4, equalTo(Optional.of(integerArray)));
        }

        @Test
        void shouldNotCastObjectWhenNotPossible() {
            // given / when / then
            assertThat(ClassUtils.tryCast(3, String.class), equalTo(Optional.empty()));
            assertThat(ClassUtils.tryCast("str", Character[].class), equalTo(Optional.empty()));
            assertThat(ClassUtils.tryCast(null, Number.class), equalTo(Optional.empty()));

            // Special case Integer <-> int: Integer is not considered an int.class
            assertThat(ClassUtils.tryCast(3, int.class), equalTo(Optional.empty()));
        }

        @Test
        void shouldCastClassAsSubclass() {
            // given / when
            Optional<Class<? extends CharSequence>> result1 = ClassUtils.asSubclassIfPossible(String.class, CharSequence.class);
            Optional<Class<? extends Number>> result2 = ClassUtils.asSubclassIfPossible(Integer.class, Number.class);
            Optional<Class<? extends Serializable>> result3 = ClassUtils.asSubclassIfPossible(Integer.class, Serializable.class);
            Optional<Class<? extends Number[]>> result4 = ClassUtils.asSubclassIfPossible(Integer[].class, Number[].class);
            Optional<Class<? extends Double>> result5 = ClassUtils.asSubclassIfPossible(Double.class, Double.class);
            Optional<Class<? extends Integer>> result6 = ClassUtils.asSubclassIfPossible(int.class, int.class);

            // then
            assertThat(result1, equalTo(Optional.of(String.class)));
            assertThat(result2, equalTo(Optional.of(Integer.class)));
            assertThat(result3, equalTo(Optional.of(Integer.class)));
            assertThat(result4, equalTo(Optional.of(Integer[].class)));
            assertThat(result5, equalTo(Optional.of(Double.class)));
            assertThat(result6, equalTo(Optional.of(int.class)));
        }

        @Test
        void shouldNotCastAsSubclassWhenNotPossible() {
            // given / when / then
            assertThat(ClassUtils.asSubclassIfPossible(String.class, Number.class), equalTo(Optional.empty()));
            assertThat(ClassUtils.asSubclassIfPossible(Number.class, Integer.class), equalTo(Optional.empty()));
            assertThat(ClassUtils.asSubclassIfPossible(Integer[].class, CharSequence[].class), equalTo(Optional.empty()));

            assertThat(ClassUtils.asSubclassIfPossible(int.class, Integer.class), equalTo(Optional.empty()));
            assertThat(ClassUtils.asSubclassIfPossible(Integer.class, int.class), equalTo(Optional.empty()));
        }
    }

    @Test
    void shouldReturnClassNameNullSafely() {
        // given / when / then
        assertThat(ClassUtils.getClassName(null), equalTo("null"));
        assertThat(ClassUtils.getClassName(3), equalTo("java.lang.Integer"));
        assertThat(ClassUtils.getClassName(DayOfWeek.MONDAY), equalTo("java.time.DayOfWeek"));
        assertThat(ClassUtils.getClassName(new String[0]), equalTo("[Ljava.lang.String;"));
        assertThat(ClassUtils.getClassName(EnumSample.DIRECT), equalTo("ch.jalu.typeresolver.classutil.ClassUtilsTest$EnumSample"));
        assertThat(ClassUtils.getClassName(EnumSample.EXTENDING), matchesRegex("ch\\.jalu\\.typeresolver\\.classutil\\.ClassUtilsTest\\$EnumSample\\$\\d+"));
    }

    @Nested
    class SemanticTypeAndName {

        @Test
        void shouldDetermineWhetherIsRegularClass() {
            // given / when / then
            assertTrue(ClassUtils.isRegularJavaClass(Object.class));
            assertTrue(ClassUtils.isRegularJavaClass(String.class));
            assertTrue(ClassUtils.isRegularJavaClass(Double.class));
            assertTrue(ClassUtils.isRegularJavaClass(HashMap.class));
            assertTrue(ClassUtils.isRegularJavaClass(getClass()));
            assertTrue(ClassUtils.isRegularJavaClass(FakeAnnotationType.class));
            assertTrue(ClassUtils.isRegularJavaClass(EnumSample.InnerClassOfEnum.class));
            assertTrue(ClassUtils.isRegularJavaClass(EnumSample.EXTENDING.getObject().getClass()));

            assertFalse(ClassUtils.isRegularJavaClass(null));
            assertFalse(ClassUtils.isRegularJavaClass(EnumSample.class)); // enum
            assertFalse(ClassUtils.isRegularJavaClass(EnumSample.EXTENDING.getClass())); // enum entry
            assertFalse(ClassUtils.isRegularJavaClass(Test.class)); // annotation
            assertFalse(ClassUtils.isRegularJavaClass(int.class)); // primitive
            assertFalse(ClassUtils.isRegularJavaClass(void.class)); // primitive
            assertFalse(ClassUtils.isRegularJavaClass(Object[].class)); // array
            assertFalse(ClassUtils.isRegularJavaClass(double[].class)); // array
            assertFalse(ClassUtils.isRegularJavaClass(Map.class)); // interface
            assertFalse(ClassUtils.isRegularJavaClass(Map.Entry.class)); // interface
            assertFalse(ClassUtils.isRegularJavaClass(getClass().getAnnotation(Nested.class).getClass())); // proxy
        }

        @Test
        void shouldReturnSimpleClassNameOrNull() {
            // given / when / then
            Stream.of(
                    new SemanticTypeAndNameTestCase(new Object(), Object.class, "Object"),
                    new SemanticTypeAndNameTestCase(null, null, "null"),
                    new SemanticTypeAndNameTestCase(new byte[0][0], byte[][].class, "byte[][]"),
                    new SemanticTypeAndNameTestCase(new ArrayList<>(), ArrayList.class, "ArrayList"),
                    new SemanticTypeAndNameTestCase(new int[0], int[].class, "int[]"),
                    new SemanticTypeAndNameTestCase(new Integer[0][0][0][0], Integer[][][][].class, "Integer[][][][]"))
                .forEach(SemanticTypeAndNameTestCase::verify);
        }

        @Test
        void shouldReturnTypeNamesForEnums() {
            // given
            Object arrayOfExtendingType = Array.newInstance(EnumSample.EXTENDING.getClass(), 0);
            Object localClassInEnumEntry = EnumSample.EXTENDING.getObject();
            EnumSample.InnerClassOfEnum innerClassOfEnum = new EnumSample.InnerClassOfEnum();

            // when / then
            Stream.of(
                    new SemanticTypeAndNameTestCase(EnumSample.DIRECT, EnumSample.class, "ClassUtilsTest$EnumSample"),
                    new SemanticTypeAndNameTestCase(EnumSample.EXTENDING, EnumSample.class, "ClassUtilsTest$EnumSample"),
                    new SemanticTypeAndNameTestCase(new EnumSample[0], EnumSample[].class, "ClassUtilsTest$EnumSample[]"),
                    new SemanticTypeAndNameTestCase(arrayOfExtendingType, Self.class, "ClassUtilsTest$EnumSample$1[]"),
                    new SemanticTypeAndNameTestCase(localClassInEnumEntry, Self.class, "ClassUtilsTest$EnumSample$1$Local"),
                    new SemanticTypeAndNameTestCase(innerClassOfEnum, EnumSample.InnerClassOfEnum.class, "ClassUtilsTest$EnumSample$InnerClassOfEnum"),
                    new SemanticTypeAndNameTestCase(EnumSample.NestedEnum.FIRST, EnumSample.NestedEnum.class, "ClassUtilsTest$EnumSample$NestedEnum"),
                    new SemanticTypeAndNameTestCase(EnumSample.NestedEnum.SECOND, EnumSample.NestedEnum.class, "ClassUtilsTest$EnumSample$NestedEnum"))
                .forEach(SemanticTypeAndNameTestCase::verify);
        }

        @Test
        void shouldReturnTypeNamesForAnnotations() throws NoSuchMethodException {
            // given
            Test testAnnotation = getClass().getDeclaredMethod("shouldReturnTypeNamesForAnnotations").getAnnotation(Test.class);
            String testAnnProxyClassName = testAnnotation.getClass().getSimpleName();
            Object proxyClassArray = Array.newInstance(testAnnotation.getClass(), 0);
            Test[][] testArray2d = new Test[0][0];
            Annotation fakeAnnotationType = new FakeAnnotationType();

            // when / then
            Stream.of(
                    new SemanticTypeAndNameTestCase(testAnnotation, Test.class, testAnnProxyClassName, "@Test"),
                    new SemanticTypeAndNameTestCase(proxyClassArray, Self.class, testAnnProxyClassName + "[]"),
                    new SemanticTypeAndNameTestCase(testArray2d, Test[][].class, "Test[][]"),
                    new SemanticTypeAndNameTestCase(fakeAnnotationType, Self.class, "ClassUtilsTest$FakeAnnotationType"),
                    new SemanticTypeAndNameTestCase(new FakeAnnotationType[0], FakeAnnotationType[].class, "ClassUtilsTest$FakeAnnotationType[]"))
                .forEach(SemanticTypeAndNameTestCase::verify);
        }
    }

    @Nested
    class ClassTypeAndCallback {

        @Test
        void shouldDetermineProperClassType() {
            // given
            Class<?> annotationProxyClass = getClass().getAnnotation(Nested.class).getClass();

            // when / then
            assertThat(ClassUtils.getType(null), nullValue());
            assertThat(ClassUtils.getType(TimeUnit.class), equalTo(ClassType.ENUM));
            assertThat(ClassUtils.getType(EnumSample.class), equalTo(ClassType.ENUM));
            assertThat(ClassUtils.getType(EnumSample.EXTENDING.getClass()), equalTo(ClassType.ENUM_ENTRY));
            assertThat(ClassUtils.getType(Override.class), equalTo(ClassType.ANNOTATION));
            assertThat(ClassUtils.getType(int.class), equalTo(ClassType.PRIMITIVE));
            assertThat(ClassUtils.getType(void.class), equalTo(ClassType.PRIMITIVE));
            assertThat(ClassUtils.getType(Runnable.class), equalTo(ClassType.INTERFACE));
            assertThat(ClassUtils.getType(int[].class), equalTo(ClassType.ARRAY));
            assertThat(ClassUtils.getType(String[][].class), equalTo(ClassType.ARRAY));
            assertThat(ClassUtils.getType(annotationProxyClass), equalTo(ClassType.PROXY_CLASS));
            assertThat(ClassUtils.getType(String.class), equalTo(ClassType.REGULAR_CLASS));
        }

        @Test
        void shouldUseEnumCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = TimeUnit.class;

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("enum[" + TimeUnit.class.getName() + "]"));
        }

        @Test
        void shouldUseEnumEntryCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = EnumSample.EXTENDING.getClass();

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("enumEntry[" + EnumSample.class.getName() + "]"));
        }

        @Test
        void shouldUseAnnotationCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = Override.class;

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("annotation[" + Override.class.getName() + "]"));
        }

        @Test
        void shouldUsePrimitiveTypeCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = int.class;

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("primitiveType[" + int.class.getName() + "]"));
        }

        @Test
        void shouldUseArrayTypeCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = int[].class;

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("arrayType[" + int[].class.getName() + "]"));
        }

        @Test
        void shouldUseInterfaceCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = Runnable.class;

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("interface[" + Runnable.class.getName() + "]"));
        }

        @Test
        void shouldUseProxyClassCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = getClass().getAnnotation(Nested.class).getClass();

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("proxyClass[" + clazz.getName() + "]"));
        }

        @Test
        void shouldUseRegularClassCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = String.class;

            // when
            String result = ClassUtils.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("regularClass[" + String.class.getName() + "]"));
        }

        @ParameterizedTest
        @EnumSource(ClassType.class)
        void shouldSupportNull(ClassType classType) {
            // given
            Class<?> clazz = getSampleClassForType(classType);
            ClassTypeCallback<Integer> callback = new ClassTypeCallback<Integer>() { };
            assertThat(ClassUtils.getType(clazz), equalTo(classType)); // validate assumption

            // when
            Integer result = ClassUtils.processClassByType(clazz, callback);

            // then
            assertThat(result, nullValue());
        }

        @Test
        void shouldNotUseCallbackIfClassIsNull() {
            // given
            CallbackTestImpl typeCallback = new CallbackTestImpl();

            // when
            String result = ClassUtils.processClassByType(null, typeCallback);

            // then
            assertThat(result, nullValue());
            assertThat(typeCallback.methodCalls, equalTo(0));
        }

        private Class<?> getSampleClassForType(ClassType classType) {
            switch (classType) {
                case ENUM:
                    return EnumSample.class;
                case ENUM_ENTRY:
                    return EnumSample.EXTENDING.getClass();
                case ANNOTATION:
                    return Nullable.class;
                case PRIMITIVE:
                    return int.class;
                case ARRAY:
                    return TimeUnit[].class;
                case INTERFACE:
                    return Iterable.class;
                case PROXY_CLASS:
                    return getClass().getAnnotation(Nested.class).getClass();
                case REGULAR_CLASS:
                    return Math.class;
                default:
                    throw new IllegalStateException("Unexpected value: " + classType);
            }
        }
    }

    // ----------
    // Test helper types
    // ----------

    private static final class SemanticTypeAndNameTestCase {

        private final Object input;
        private final Class<?> expectedSemanticType;
        private final String expectedNameFromClass;
        private final String expectedNameFromObject;

        SemanticTypeAndNameTestCase(@Nullable Object input,
                                    @Nullable Class<?> expectedSemanticType,
                                    String expectedName) {
            this(input, expectedSemanticType, expectedName, expectedName);
        }

        SemanticTypeAndNameTestCase(@Nullable Object input,
                                    @Nullable Class<?> expectedSemanticType,
                                    String expectedNameFromClass,
                                    String expectedNameFromObject) {
            this.input = input;
            if (Self.class.equals(expectedSemanticType)) {
                this.expectedSemanticType = input.getClass();
            } else {
                this.expectedSemanticType = expectedSemanticType;
            }
            this.expectedNameFromClass = expectedNameFromClass;
            this.expectedNameFromObject = expectedNameFromObject;
        }

        void verify() {
            Class<?> inputClass = input == null ? null : input.getClass();

            Class<?> actualSemanticType = getSemanticType(input);
            if (!Objects.equals(actualSemanticType, expectedSemanticType)) {
                fail("For '" + input + "' (" + inputClass + "), expected semantic type '"
                    + expectedSemanticType + "', but got: '" + actualSemanticType + "'");
            }

            String actualSemanticNameFromObj = ClassUtils.getSemanticName(input);
            if (!actualSemanticNameFromObj.equals(expectedNameFromObject)) {
                fail("For '" + input + "' (" + inputClass + "), expected semantic name (from obj) '"
                    + expectedNameFromObject + "', but got: '" + actualSemanticNameFromObj + "'");
            }

            String actualSemanticNameFromClass = ClassUtils.getSemanticName(inputClass);
            if (!actualSemanticNameFromClass.equals(expectedNameFromClass)) {
                fail("For '" + input + "' (" + inputClass + "), expected semantic name (from class) '"
                    + expectedNameFromClass + "', but got: '" + actualSemanticNameFromClass + "'");
            }
        }
    }

    /**
     * Dummy class to set to {@link SemanticTypeAndNameTestCase} that the expected class should be taken from the input
     * object directly. This is required when the input object has a class that cannot be directly referenced (e.g.
     * because of anonymous classes). Otherwise, explicitly referring to the class is preferred.
     */
    private static final class Self { }

    private static final class CallbackTestImpl extends ClassTypeCallback<String> {

        int methodCalls;

        @Override
        public String forEnum(Class<? extends Enum<?>> enumClass) {
            ++methodCalls;
            return "enum[" + enumClass.getName() + "]";
        }

        @Override
        public String forEnumEntry(Class<? extends Enum<?>> enumClass, Class<? extends Enum<?>> enumEntryClass) {
            ++methodCalls;
            return "enumEntry[" + enumClass.getName() + "]";
        }

        @Override
        public String forAnnotation(Class<? extends Annotation> annotationClass) {
            ++methodCalls;
            return "annotation[" + annotationClass.getName() + "]";
        }

        @Override
        public String forPrimitiveType(Class<?> primitiveClass) {
            ++methodCalls;
            return "primitiveType[" + primitiveClass.getName() + "]";
        }

        @Override
        public String forArrayType(Class<?> arrayClass) {
            ++methodCalls;
            return "arrayType[" + arrayClass.getName() + "]";
        }

        @Override
        public String forInterface(Class<?> interfaceType) {
            ++methodCalls;
            return "interface[" + interfaceType.getName() + "]";
        }

        @Override
        public String forProxyClass(Class<?> proxyClass) {
            ++methodCalls;
            return "proxyClass[" + proxyClass.getName() + "]";
        }

        @Override
        public String forRegularClass(Class<?> regularClass) {
            ++methodCalls;
            return "regularClass[" + regularClass.getName() + "]";
        }
    }


    // -------
    // Sample types
    // -------

    private enum EnumSample {

        DIRECT,

        EXTENDING() {
            class Local {

            }

            @Override
            public Object getObject() {
                return new Local();
            }
        };

        EnumSample() {

        }

        public Object getObject() {
            throw new IllegalStateException("Should only be called for entry that overrides it");
        }

        static final class InnerClassOfEnum {

        }

        private enum NestedEnum {

            FIRST,
            SECOND { }

        }
    }

    /** Class which is not an annotation but implements the annotation interface manually. */
    private static class FakeAnnotationType implements Annotation {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Nullable.class;
        }
    }
}