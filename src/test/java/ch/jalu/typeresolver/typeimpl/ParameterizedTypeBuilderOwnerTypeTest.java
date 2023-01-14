package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.TypeToClassUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests the creation of the owner type in parameterized types built by {@link ParameterizedTypeBuilder}.
 */
class ParameterizedTypeBuilderOwnerTypeTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("createCasesForOwnerTypeTests")
    void shouldCreateParameterizedTypeWithProperOwnerType(Class<?> rawType, Field fieldOfType,
                                                          OwnerTypeToken[] ownerTypeExpectations) {
        // given / when
        ParameterizedTypeImpl result = ParameterizedTypeBuilder.newTypeFromClass(rawType).build();

        // then
        Type expectedType = fieldOfType.getGenericType();
        verifyOwnerTypeConsistsOfTypes(expectedType, result.getOwnerType(), ownerTypeExpectations);
        assertThat(result, equalTo(expectedType));
    }

    static List<Arguments> createCasesForOwnerTypeTests() throws NoSuchFieldException {
        List<Arguments> arguments = new ArrayList<>();
        final Class<?> thisTestClass = ParameterizedTypeBuilderOwnerTypeTest.class;

        Class<?> clazz = SP1.SP2.SP3.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            cls(SP1.SP2.class)));

        clazz = SP1.NP2.NP3.NP4.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(SP1.NP2.NP3.class),
            pt(SP1.NP2.class),
            pt(SP1.class),
            cls(thisTestClass)));

        clazz = SP1.S2.NP3.NP4.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(SP1.S2.NP3.class),
            cls(SP1.S2.class)));

        clazz = SP1.SP2.NP3.NP4.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(SP1.SP2.NP3.class),
            pt(SP1.SP2.class),
            cls(SP1.class)));

        clazz = SP1.NP2.N3.NP4.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(SP1.NP2.N3.class),
            pt(SP1.NP2.class),
            pt(SP1.class),
            cls(thisTestClass)));

        clazz = SP1.NP2.N3.NP4.NP5.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(SP1.NP2.N3.NP4.class),
            pt(SP1.NP2.N3.class),
            pt(SP1.NP2.class),
            pt(SP1.class),
            cls(thisTestClass)));

        clazz = SP1.SP2.NP3.N4.NP5.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(SP1.SP2.NP3.N4.class),
            pt(SP1.SP2.NP3.class),
            pt(SP1.SP2.class),
            cls(SP1.class)));

        clazz = ClassWithTypeParamEnclosingOthers.SP1.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            cls(ClassWithTypeParamEnclosingOthers.class)));

        clazz = ClassWithTypeParamEnclosingOthers.NP1.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(ClassWithTypeParamEnclosingOthers.class)));

        clazz = ClassWithTypeParamEnclosingOthers.SP1.SP2.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            cls(ClassWithTypeParamEnclosingOthers.SP1.class)));

        clazz = ClassWithTypeParamEnclosingOthers.SP1.N2.NP3.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(ClassWithTypeParamEnclosingOthers.SP1.N2.class),
            pt(ClassWithTypeParamEnclosingOthers.SP1.class),
            cls(ClassWithTypeParamEnclosingOthers.class)));

        clazz = ClassWithTypeParamEnclosingOthers.S1.N2.NP3.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            cls(ClassWithTypeParamEnclosingOthers.S1.N2.class)));

        clazz = ClassWithTypeParamEnclosingOthers.N1.N2.NP3.class;
        arguments.add(newArgs(clazz, clazz.getDeclaredField("selfTyped"),
            pt(ClassWithTypeParamEnclosingOthers.N1.N2.class),
            pt(ClassWithTypeParamEnclosingOthers.N1.class),
            pt(ClassWithTypeParamEnclosingOthers.class)));

        return arguments;
    }

    @Test
    void shouldHandleLocalClassesProperly() throws NoSuchFieldException {
        // given
        class AnonClass<T> {
            public AnonClass<T> selfTyped;

            class AnonClass2<U, V> {
                public AnonClass2<U, V> selfTyped;
            }
        }

        // when
        ParameterizedType result1 = ParameterizedTypeBuilder.newTypeFromClass(AnonClass.class).build();
        ParameterizedType result2 = ParameterizedTypeBuilder.newTypeFromClass(AnonClass.AnonClass2.class).build();

        // then
        Type type1 = AnonClass.class.getDeclaredField("selfTyped").getGenericType();
        Type type2 = AnonClass.AnonClass2.class.getDeclaredField("selfTyped").getGenericType();
        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldHandleAnonymousClassOfInnerClassesProperly() throws NoSuchFieldException {
        // given
        class LocalNp2Ext<V> extends SP1.SP2<String> {
            public LocalNp2Ext<V> selfTyped;
        }

        // when
        ParameterizedType result = ParameterizedTypeBuilder.newTypeFromClass(LocalNp2Ext.class).build();

        // then
        assertThat(result.getOwnerType(), nullValue());
        Type type = LocalNp2Ext.class.getDeclaredField("selfTyped").getGenericType();
        assertThat(result, equalTo(type));
    }

    private static Arguments newArgs(Class<?> clazz, Field field, OwnerTypeToken... ownerTypeExpectations) {
        return Arguments.of(clazz, field, ownerTypeExpectations);
    }

    private void printOwnerTypes(Type ownerType) {
        Type currentOwner = ownerType;
        while (currentOwner != null) {
            String typeName = TypeToClassUtil.getSafeToReadClass(currentOwner).getSimpleName();
            System.out.println(" " + currentOwner.getClass().getSimpleName() + " [" + typeName + "]");
            if (currentOwner instanceof ParameterizedType) {
                currentOwner = ((ParameterizedType) currentOwner).getOwnerType();
            } else {
                currentOwner = null;
            }
        }
    }

    private void verifyOwnerTypeConsistsOfTypes(Type actualType, Type ownerType, OwnerTypeToken... expectedTypes) {
        Type expectedOwnerType = ((ParameterizedType) actualType).getOwnerType();
        String errorOnExpectedType = verifyOwnerTypesAndReturnErrorIfApplicable(expectedOwnerType, expectedTypes);
        if (errorOnExpectedType != null) {
            System.out.println("Actual expected owner type:");
            printOwnerTypes(expectedOwnerType);
            throw new IllegalStateException("Error of verifications on expected type. Are the expectations wrong?\n --> " + errorOnExpectedType);
        }

        String error = verifyOwnerTypesAndReturnErrorIfApplicable(ownerType, expectedTypes);
        if (error != null) {
            System.out.println("Expected owner types:");
            printOwnerTypes(expectedOwnerType);
            System.out.println("Actual owner types:");
            printOwnerTypes(ownerType);
            fail(error);
        }
    }

    @Nullable
    private String verifyOwnerTypesAndReturnErrorIfApplicable(Type ownerType, OwnerTypeToken... expectations) {
        Type currentOwnerLevel = ownerType;
        for (int i = 0; i < expectations.length; i++) {
            OwnerTypeToken expectedType = expectations[i];
            String error = expectedType.getExpectationOnMismatch(currentOwnerLevel);
            if (error != null) {
                String actualType;
                if (currentOwnerLevel instanceof Class<?>) {
                    actualType = "cls(" + ((Class<?>) currentOwnerLevel).getSimpleName() + ")";
                } else if (currentOwnerLevel instanceof ParameterizedType) {
                    actualType = "pt(" + TypeToClassUtil.getSafeToReadClass(currentOwnerLevel).getSimpleName() + ")";
                } else { // currentOwnerLevel == null
                    actualType = Objects.toString(currentOwnerLevel);
                }
                return "Index " + i + ": " + error + ". Actual: " + actualType;
            }

            if (currentOwnerLevel instanceof ParameterizedType) {
                currentOwnerLevel = ((ParameterizedType) currentOwnerLevel).getOwnerType();
            } else {
                currentOwnerLevel = null;
            }
        }
        if (currentOwnerLevel != null) {
            return "Found nested owner type '" + currentOwnerLevel + "' but no more expected types were provided";
        }
        return null;
    }

    private static OwnerTypeToken pt(Class<?> expectedRawType) {
        return typeToVerify -> {
            if (!(typeToVerify instanceof ParameterizedType)
                || !((ParameterizedType) typeToVerify).getRawType().equals(expectedRawType)) {
                return "Expected parameterized type of rawType=" + expectedRawType.getSimpleName();
            }
            return null;
        };
    }

    private static OwnerTypeToken cls(Class<?> expectedClass) {
        return typeToVerify -> {
            if (!expectedClass.equals(typeToVerify)) {
                return "Expected class '" + expectedClass.getSimpleName() + "'";
            }
            return null;
        };
    }

    @FunctionalInterface
    private interface OwnerTypeToken {

        @Nullable
        String getExpectationOnMismatch(@Nullable Type typeToVerify);
    }

    /*
     * Class naming scheme:
     *   S = static, N = non-static (or nested)
     *   P = with type params
     *   Every class ends with a number indicating its level of nesting
     */
    private static class SP1<A> {

        private static class SP2<B> {

            private static class SP3<C> {
                public SP3<C> selfTyped;
            }

            private class NP3<N> {

                private class NP4<O> {
                    public NP4<O> selfTyped;

                }

                private class N4 {

                    private class NP5<K> {
                        public NP5<K> selfTyped;
                    }
                }
            }
        }

        private class NP2<N> {

            private class NP3<O> {

                private class NP4<P> {
                    public NP4<P> selfTyped;

                }
            }

            private class N3 {

                private class NP4<K> {
                    public NP4<K> selfTyped;

                    private class NP5<L> {
                        public NP5<L> selfTyped;
                    }
                }
            }
        }

        private static class S2 {

            private class NP3<N> {

                private class NP4<O> {
                    public NP4<O> selfTyped;

                }
            }
        }
    }
}