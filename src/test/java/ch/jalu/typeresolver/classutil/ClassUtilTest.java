package ch.jalu.typeresolver.classutil;

import ch.jalu.typeresolver.TypeInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.awt.font.NumericShaper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.time.DayOfWeek;
import java.util.ArrayList;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertThat(ClassUtil.getClassName(EnumSample.CM), equalTo("ch.jalu.typeresolver.classutil.ClassUtilTest$EnumSample"));
        assertThat(ClassUtil.getClassName(EnumSample.M), matchesRegex("ch\\.jalu\\.typeresolver\\.classutil\\.ClassUtilTest\\$EnumSample\\$\\d+"));
    }

    @Nested
    class SemanticTypeAndName {

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
            EnumSample[] units = new EnumSample[0];
            Object mEntries = Array.newInstance(EnumSample.M.getClass(), 2);
            Object localClassInEnumEntry = EnumSample.M.getObject();
            EnumSample.InnerClassOfEnum innerClassOfEnum = new EnumSample.InnerClassOfEnum();

            // when / then
            Stream.of(
                    new SemanticTypeAndNameTestCase(EnumSample.CM, EnumSample.class, "ClassUtilTest$EnumSample"),
                    new SemanticTypeAndNameTestCase(EnumSample.M, EnumSample.class, "ClassUtilTest$EnumSample"),
                    new SemanticTypeAndNameTestCase(units, EnumSample[].class, "ClassUtilTest$EnumSample[]"),
                    new SemanticTypeAndNameTestCase(mEntries, Self.class, "ClassUtilTest$EnumSample$1[]"),
                    new SemanticTypeAndNameTestCase(localClassInEnumEntry, Self.class, "ClassUtilTest$EnumSample$1$Local"),
                    new SemanticTypeAndNameTestCase(innerClassOfEnum, EnumSample.InnerClassOfEnum.class, "ClassUtilTest$EnumSample$InnerClassOfEnum"),
                    new SemanticTypeAndNameTestCase(EnumSample.NestedEnum.FIRST, EnumSample.NestedEnum.class, "ClassUtilTest$EnumSample$NestedEnum"),
                    new SemanticTypeAndNameTestCase(EnumSample.NestedEnum.SECOND, EnumSample.NestedEnum.class, "ClassUtilTest$EnumSample$NestedEnum"))
                .forEach(SemanticTypeAndNameTestCase::verify);
        }

        @Test
        void shouldReturnTypeNamesForAnnotations() throws NoSuchMethodException {
            // given
            Test test = getClass().getDeclaredMethod("shouldReturnTypeNamesForAnnotations").getAnnotation(Test.class);
            String proxyClassName = test.getClass().getSimpleName();
            Object array = Array.newInstance(test.getClass(), 2);
            Test[][] array2d = new Test[0][0];
            Annotation fakeAnnotationType = new FakeAnnotationType();

            // when / then
            Stream.of(
                    new SemanticTypeAndNameTestCase(test, Test.class, proxyClassName, "@Test"),
                    new SemanticTypeAndNameTestCase(array, Self.class, proxyClassName + "[]"),
                    new SemanticTypeAndNameTestCase(array2d, Test[][].class, "Test[][]"),
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
            assertThat(ClassUtil.getType(EnumSample.M.getClass()), equalTo(ClassType.ENUM_ENTRY));
            assertThat(ClassUtil.getType(Override.class), equalTo(ClassType.ANNOTATION));
            assertThat(ClassUtil.getType(int.class), equalTo(ClassType.PRIMITIVE));
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
            Class<?> clazz = EnumSample.M.getClass();

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

        @Override
        public String forEnum(Class<? extends Enum<?>> enumClass) {
            return "enum[" + enumClass.getName() + "]";
        }

        @Override
        public String forEnumEntry(Class<? extends Enum<?>> enumClass, Class<? extends Enum<?>> enumEntryClass) {
            return "enumEntry[" + enumClass.getName() + "]";
        }

        @Override
        public String forAnnotation(Class<? extends Annotation> annotationClass) {
            return "annotation[" + annotationClass.getName() + "]";
        }

        @Override
        public String forPrimitiveType(Class<?> primitiveClass) {
            return "primitiveType[" + primitiveClass.getName() + "]";
        }

        @Override
        public String forArrayType(Class<?> arrayClass) {
            return "arrayType[" + arrayClass.getName() + "]";
        }

        @Override
        public String forInterface(Class<?> interfaceType) {
            return "interface[" + interfaceType.getName() + "]";
        }

        @Override
        public String forProxyClass(Class<?> proxyClass) {
            return "proxyClass[" + proxyClass.getName() + "]";
        }

        @Override
        public String forRegularClass(Class<?> regularClass) {
            return "regularClass[" + regularClass.getName() + "]";
        }
    }


    // -------
    // Sample types
    // -------

    private enum EnumSample {

        CM,

        M() {
            class Local {

            }

            @Override
            public Object getObject() {
                return new Local();
            }
        },

        KM;

        EnumSample() {

        }

        public Object getObject() {
            return null;
        }

        static final class InnerClassOfEnum {

        }

        private enum NestedEnum {

            FIRST,
            SECOND { }

        }
    }

    private static class FakeAnnotationType implements Annotation {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Nullable.class;
        }
    }
}