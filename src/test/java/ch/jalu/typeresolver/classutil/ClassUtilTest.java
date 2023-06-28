package ch.jalu.typeresolver.classutil;

import ch.jalu.typeresolver.TypeInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nullable;
import java.awt.font.NumericShaper;
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

import static ch.jalu.typeresolver.classutil.ClassUtil.getSemanticType;
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
 * Test for {@link ClassUtil}.
 */
class ClassUtilTest {

    /** Tests the Javadoc on {@link ClassUtil#getSemanticType}. */
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
            assertThat(ClassUtil.classExists("java.lang.String"), equalTo(true));
            assertThat(ClassUtil.classExists("ch.jalu.typeresolver.TypeInfo"), equalTo(true));
            assertThat(ClassUtil.classExists(Test.class.getCanonicalName()), equalTo(true));

            assertThat(ClassUtil.classExists("java.lang.DoesNotExist"), equalTo(false));
            assertThat(ClassUtil.classExists("ch.jalu.typeresolver.Bogus"), equalTo(false));
        }

        @Test
        void shouldLoadClassIfPossible() {
            // given / when / then
            assertThat(ClassUtil.tryLoadClass("java.lang.String"), equalTo(Optional.of(String.class)));
            assertThat(ClassUtil.tryLoadClass("ch.jalu.typeresolver.TypeInfo"), equalTo(Optional.of(TypeInfo.class)));
            assertThat(ClassUtil.tryLoadClass(Test.class.getCanonicalName()), equalTo(Optional.of(Test.class)));

            assertThat(ClassUtil.tryLoadClass("java.lang.DoesNotExist"), equalTo(Optional.empty()));
            assertThat(ClassUtil.tryLoadClass("ch.jalu.typeresolver.Bogus"), equalTo(Optional.empty()));
        }

        @Test
        void shouldLoadClassOrThrowException() {
            // given / when / then
            assertThat(ClassUtil.loadClassOrThrow("java.lang.String"), equalTo(String.class));
            assertThat(ClassUtil.loadClassOrThrow("ch.jalu.typeresolver.TypeInfo"), equalTo(TypeInfo.class));
            assertThat(ClassUtil.loadClassOrThrow(Test.class.getCanonicalName()), equalTo(Test.class));

            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> ClassUtil.loadClassOrThrow("java.lang.DoesNotExist"));
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> ClassUtil.loadClassOrThrow("ch.jalu.typeresolver.Bogus"));

            assertThat(ex1.getMessage(), equalTo("Class 'java.lang.DoesNotExist' could not be loaded"));
            assertThat(ex1.getCause(), instanceOf(ClassNotFoundException.class));
            assertThat(ex2.getMessage(), equalTo("Class 'ch.jalu.typeresolver.Bogus' could not be loaded"));
            assertThat(ex2.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }

    @Test
    void shouldReturnClassNameNullSafely() {
        // given / when / then
        assertThat(ClassUtil.getClassName(null), equalTo("null"));
        assertThat(ClassUtil.getClassName(3), equalTo("java.lang.Integer"));
        assertThat(ClassUtil.getClassName(DayOfWeek.MONDAY), equalTo("java.time.DayOfWeek"));
        assertThat(ClassUtil.getClassName(new String[0]), equalTo("[Ljava.lang.String;"));
        assertThat(ClassUtil.getClassName(EnumSample.DIRECT), equalTo("ch.jalu.typeresolver.classutil.ClassUtilTest$EnumSample"));
        assertThat(ClassUtil.getClassName(EnumSample.EXTENDING), matchesRegex("ch\\.jalu\\.typeresolver\\.classutil\\.ClassUtilTest\\$EnumSample\\$\\d+"));
    }

    @Nested
    class SemanticTypeAndName {

        @Test
        void shouldDetermineWhetherIsRegularClass() {
            // given / when / then
            assertTrue(ClassUtil.isRegularJavaClass(Object.class));
            assertTrue(ClassUtil.isRegularJavaClass(String.class));
            assertTrue(ClassUtil.isRegularJavaClass(Double.class));
            assertTrue(ClassUtil.isRegularJavaClass(HashMap.class));
            assertTrue(ClassUtil.isRegularJavaClass(getClass()));
            assertTrue(ClassUtil.isRegularJavaClass(FakeAnnotationType.class));
            assertTrue(ClassUtil.isRegularJavaClass(EnumSample.InnerClassOfEnum.class));
            assertTrue(ClassUtil.isRegularJavaClass(EnumSample.EXTENDING.getObject().getClass()));

            assertFalse(ClassUtil.isRegularJavaClass(null));
            assertFalse(ClassUtil.isRegularJavaClass(EnumSample.class)); // enum
            assertFalse(ClassUtil.isRegularJavaClass(EnumSample.EXTENDING.getClass())); // enum entry
            assertFalse(ClassUtil.isRegularJavaClass(Test.class)); // annotation
            assertFalse(ClassUtil.isRegularJavaClass(int.class)); // primitive
            assertFalse(ClassUtil.isRegularJavaClass(void.class)); // primitive
            assertFalse(ClassUtil.isRegularJavaClass(Object[].class)); // array
            assertFalse(ClassUtil.isRegularJavaClass(double[].class)); // array
            assertFalse(ClassUtil.isRegularJavaClass(Map.class)); // interface
            assertFalse(ClassUtil.isRegularJavaClass(Map.Entry.class)); // interface
            assertFalse(ClassUtil.isRegularJavaClass(getClass().getAnnotation(Nested.class).getClass())); // proxy
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
                    new SemanticTypeAndNameTestCase(EnumSample.DIRECT, EnumSample.class, "ClassUtilTest$EnumSample"),
                    new SemanticTypeAndNameTestCase(EnumSample.EXTENDING, EnumSample.class, "ClassUtilTest$EnumSample"),
                    new SemanticTypeAndNameTestCase(new EnumSample[0], EnumSample[].class, "ClassUtilTest$EnumSample[]"),
                    new SemanticTypeAndNameTestCase(arrayOfExtendingType, Self.class, "ClassUtilTest$EnumSample$1[]"),
                    new SemanticTypeAndNameTestCase(localClassInEnumEntry, Self.class, "ClassUtilTest$EnumSample$1$Local"),
                    new SemanticTypeAndNameTestCase(innerClassOfEnum, EnumSample.InnerClassOfEnum.class, "ClassUtilTest$EnumSample$InnerClassOfEnum"),
                    new SemanticTypeAndNameTestCase(EnumSample.NestedEnum.FIRST, EnumSample.NestedEnum.class, "ClassUtilTest$EnumSample$NestedEnum"),
                    new SemanticTypeAndNameTestCase(EnumSample.NestedEnum.SECOND, EnumSample.NestedEnum.class, "ClassUtilTest$EnumSample$NestedEnum"))
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
                    new SemanticTypeAndNameTestCase(fakeAnnotationType, Self.class, "ClassUtilTest$FakeAnnotationType"),
                    new SemanticTypeAndNameTestCase(new FakeAnnotationType[0], FakeAnnotationType[].class, "ClassUtilTest$FakeAnnotationType[]"))
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
            assertThat(ClassUtil.getType(null), nullValue());
            assertThat(ClassUtil.getType(TimeUnit.class), equalTo(ClassType.ENUM));
            assertThat(ClassUtil.getType(EnumSample.class), equalTo(ClassType.ENUM));
            assertThat(ClassUtil.getType(EnumSample.EXTENDING.getClass()), equalTo(ClassType.ENUM_ENTRY));
            assertThat(ClassUtil.getType(Override.class), equalTo(ClassType.ANNOTATION));
            assertThat(ClassUtil.getType(int.class), equalTo(ClassType.PRIMITIVE));
            assertThat(ClassUtil.getType(void.class), equalTo(ClassType.PRIMITIVE));
            assertThat(ClassUtil.getType(Runnable.class), equalTo(ClassType.INTERFACE));
            assertThat(ClassUtil.getType(int[].class), equalTo(ClassType.ARRAY));
            assertThat(ClassUtil.getType(String[][].class), equalTo(ClassType.ARRAY));
            assertThat(ClassUtil.getType(annotationProxyClass), equalTo(ClassType.PROXY_CLASS));
            assertThat(ClassUtil.getType(String.class), equalTo(ClassType.REGULAR_CLASS));
        }

        @Test
        void shouldUseEnumCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = TimeUnit.class;

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("enum[" + TimeUnit.class.getName() + "]"));
        }

        @Test
        void shouldUseEnumEntryCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = EnumSample.EXTENDING.getClass();

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("enumEntry[" + EnumSample.class.getName() + "]"));
        }

        @Test
        void shouldUseAnnotationCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = Override.class;

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("annotation[" + Override.class.getName() + "]"));
        }

        @Test
        void shouldUsePrimitiveTypeCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = int.class;

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("primitiveType[" + int.class.getName() + "]"));
        }

        @Test
        void shouldUseArrayTypeCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = int[].class;

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("arrayType[" + int[].class.getName() + "]"));
        }

        @Test
        void shouldUseInterfaceCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = Runnable.class;

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("interface[" + Runnable.class.getName() + "]"));
        }

        @Test
        void shouldUseProxyClassCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = getClass().getAnnotation(Nested.class).getClass();

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("proxyClass[" + clazz.getName() + "]"));
        }

        @Test
        void shouldUseRegularClassCallback() {
            // given
            ClassTypeCallback<String> typeCallback = new CallbackTestImpl();
            Class<?> clazz = String.class;

            // when
            String result = ClassUtil.processClassByType(clazz, typeCallback);

            // then
            assertThat(result, equalTo("regularClass[" + String.class.getName() + "]"));
        }

        @ParameterizedTest
        @EnumSource(ClassType.class)
        void shouldSupportNull(ClassType classType) {
            // given
            Class<?> clazz = getSampleClassForType(classType);
            ClassTypeCallback<Integer> callback = new ClassTypeCallback<Integer>() { };
            assertThat(ClassUtil.getType(clazz), equalTo(classType)); // validate assumption

            // when
            Integer result = ClassUtil.processClassByType(clazz, callback);

            // then
            assertThat(result, nullValue());
        }

        @Test
        void shouldNotUseCallbackIfClassIsNull() {
            // given
            CallbackTestImpl typeCallback = new CallbackTestImpl();

            // when
            String result = ClassUtil.processClassByType(null, typeCallback);

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

            String actualSemanticNameFromObj = ClassUtil.getSemanticName(input);
            if (!actualSemanticNameFromObj.equals(expectedNameFromObject)) {
                fail("For '" + input + "' (" + inputClass + "), expected semantic name (from obj) '"
                    + expectedNameFromObject + "', but got: '" + actualSemanticNameFromObj + "'");
            }

            String actualSemanticNameFromClass = ClassUtil.getSemanticName(inputClass);
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