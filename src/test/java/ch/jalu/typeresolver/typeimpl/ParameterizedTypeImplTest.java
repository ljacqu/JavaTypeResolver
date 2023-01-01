package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.TypeToClassUtil;
import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link ParameterizedTypeImpl}.
 */
class ParameterizedTypeImplTest extends AbstractTypeImplTest {

    ParameterizedTypeImplTest() {
        super(
            new ParameterizedTypeImpl(List.class, null, Double.class),
            new ParameterizedTypeImpl(Set.class, null, Short[].class),
            new ParameterizedTypeImpl(Map.class, null, String.class,
            new ParameterizedTypeImpl(Set.class, null, Short[].class)),
            new TypeReference<List<Double>>() { }.getType(),
            new TypeReference<Set<Short[]>>() { }.getType(),
            new TypeReference<Map<String, Set<Short[]>>>() { }.getType());
    }

    @Test
    void shouldIncludeOwnerTypeInToString() {
        // given
        ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(
            InnerParameterizedClassesContainer.TypedNestedClass.class, InnerParameterizedClassesContainer.class, BigDecimal.class);
        Type jreType = new TypeReference<InnerParameterizedClassesContainer.TypedNestedClass<BigDecimal>>() { }.getType();

        // when / then
        assertThat(parameterizedType.toString(), equalTo("ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer$TypedNestedClass<java.math.BigDecimal>"));
        assertThat(parameterizedType.toString(), equalTo(jreType.toString()));
    }

    @Test
    void shouldCreateParameterizedTypeFromClass() {
        // given / when
        ParameterizedTypeImpl comparablePt = ParameterizedTypeImpl.newTypeWithTypeParameters(Comparable.class);
        ParameterizedTypeImpl mapPt = ParameterizedTypeImpl.newTypeWithTypeParameters(Map.class);
        ParameterizedTypeImpl listPt = ParameterizedTypeImpl.newTypeWithTypeParameters(List.class);

        // then
        assertThat(comparablePt.getActualTypeArguments(), arrayContaining(Comparable.class.getTypeParameters()[0]));
        assertThat(comparablePt.getRawType(), equalTo(Comparable.class));
        assertThat(comparablePt.getOwnerType(), nullValue());

        assertEqualToCreationViaGenericInterface(mapPt, Map.class, HashMap.class);
        assertEqualToCreationViaGenericInterface(listPt, List.class, ArrayList.class);
    }

    @Test
    void shouldThrowForRawTypeWithNoTypeParameters() {
        // given / when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> ParameterizedTypeImpl.newTypeWithTypeParameters(String.class));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> ParameterizedTypeImpl.newTypeWithTypeParameters(int.class));
        assertThrows(NullPointerException.class,
            () -> ParameterizedTypeImpl.newTypeWithTypeParameters(null));

        // then
        assertThat(ex1.getMessage(), equalTo("Class 'class java.lang.String' has no type arguments"));
        assertThat(ex2.getMessage(), equalTo("Class 'int' has no type arguments"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createCasesForOwnerTypeTests")
    void shouldCreateParameterizedTypeWithProperOwner(Class<?> rawType, Field fieldOfType,
                                                      OwnerTypeToken[] ownerTypeExpectations) {
        // given / when
        ParameterizedTypeImpl result = ParameterizedTypeImpl.newTypeWithTypeParameters(rawType);

        // then
        Type expectedType = fieldOfType.getGenericType();
        verifyOwnerTypeConsistsOfTypes(expectedType, result.getOwnerType(), ownerTypeExpectations);
        assertThat(result, equalTo(expectedType));
    }

    static List<Arguments> createCasesForOwnerTypeTests() throws NoSuchFieldException {
        List<Arguments> arguments = new ArrayList<>();
        final Class<?> thisTestClass = ParameterizedTypeImplTest.class;

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

    private static Arguments newArgs(Class<?> clazz, Field field, OwnerTypeToken... ownerTypeExpectations) {
        return Arguments.of(clazz, field, ownerTypeExpectations);
    }

    private void assertEqualToCreationViaGenericInterface(ParameterizedTypeImpl typeToCheck,
                                                          Class<?> rawType, Class<?> extendingType) {
        ParameterizedType expectedType = Arrays.stream(extendingType.getGenericInterfaces())
            .filter(intf -> intf instanceof ParameterizedType && ((ParameterizedType) intf).getRawType().equals(rawType))
            .map(intf -> (ParameterizedType) intf)
            .findFirst().get();

        assertThat(typeToCheck.getRawType(), equalTo(expectedType.getRawType()));
        assertThat(typeToCheck.getOwnerType(), equalTo(expectedType.getOwnerType()));

        Type[] expectedArgs = expectedType.getActualTypeArguments();
        Type[] actualArgs = typeToCheck.getActualTypeArguments();
        assertThat(actualArgs.length, equalTo(expectedArgs.length));

        for (int i = 0; i < actualArgs.length; i++) {
            TypeVariable<?> actualArg = (TypeVariable<?>) actualArgs[i];
            TypeVariable<?> expectedArg = (TypeVariable<?>) expectedArgs[i];

            assertThat(actualArg.getName(), equalTo(expectedArg.getName()));
            assertThat(actualArg.getBounds(), equalTo(expectedArg.getBounds()));
            // Annotated bounds not checked for now
            assertThat(actualArg.getGenericDeclaration(), equalTo(rawType));
        }
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
    private static final class SP1<A> {

        private static final class SP2<B> {

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