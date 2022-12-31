package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.CommonTypeUtil;
import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Test
    void shouldCreateParameterizedTypeForEnclosedClass() throws NoSuchFieldException {
        // given
        Class<?> clazz = SP1.SP2.SP3.class;

        // when
        ParameterizedTypeImpl result = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz);

        // then
        // The owner type is just the Class because all enclosing classes are static -> the type params do not matter
        Type type = clazz.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type, result.getOwnerType(),
            cls(SP1.SP2.class));
        assertThat(result, equalTo(type));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass() throws NoSuchFieldException {
        // given
        Class<?> clazz = SP1.NP2.NP3.NP4.class;

        // when
        ParameterizedTypeImpl result = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz);

        // then
        Type type = clazz.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type, result.getOwnerType(),
            pt(SP1.NP2.NP3.class),
            pt(SP1.NP2.class),
            pt(SP1.class),
            cls(getClass()));

        assertThat(result, equalTo(type));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass2() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = SP1.S2.NP3.NP4.class;
        Class<?> clazz2 = SP1.SP2.NP3.NP4.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            pt(SP1.S2.NP3.class),
            cls(SP1.S2.class));
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(SP1.SP2.NP3.class),
            pt(SP1.SP2.class),
            cls(SP1.class));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass3() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = SP1.NP2.N3.NP4.class;
        Class<?> clazz2 = SP1.NP2.N3.NP4.NP5.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            pt(SP1.NP2.N3.class),
            pt(SP1.NP2.class),
            pt(SP1.class),
            cls(getClass()));

        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(SP1.NP2.N3.NP4.class),
            pt(SP1.NP2.N3.class),
            pt(SP1.NP2.class),
            pt(SP1.class),
            cls(getClass()));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass4() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = SP1.SP2.NP3.N4.NP5.class;
        Class<?> clazz2 = SP1.SP2.NP3.N4.NP5.NP6.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            pt(SP1.SP2.NP3.N4.class),
            pt(SP1.SP2.NP3.class),
            pt(SP1.SP2.class),
            cls(SP1.class));
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(SP1.SP2.NP3.N4.NP5.class),
            pt(SP1.SP2.NP3.N4.class),
            pt(SP1.SP2.NP3.class),
            pt(SP1.SP2.class),
            cls(SP1.class));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldCreateParameterizedTypeWithClassIfTypeParamsAreNotRelevant() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = ClassWithTypeParamEnclosingOthers.SP1.class;
        Class<?> clazz2 = ClassWithTypeParamEnclosingOthers.NP1.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            cls(ClassWithTypeParamEnclosingOthers.class));

        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(ClassWithTypeParamEnclosingOthers.class));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldCreateParameterizedTypesForClassesNestedInClassWithTypeParam() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = ClassWithTypeParamEnclosingOthers.SP1.SP2.class;
        Class<?> clazz2 = ClassWithTypeParamEnclosingOthers.SP1.N2.NP3.class;
        Class<?> clazz3 = ClassWithTypeParamEnclosingOthers.S1.N2.NP3.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);
        ParameterizedTypeImpl result3 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz3);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();
        Type type3 = clazz3.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            cls(ClassWithTypeParamEnclosingOthers.SP1.class));
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(ClassWithTypeParamEnclosingOthers.SP1.N2.class),
            pt(ClassWithTypeParamEnclosingOthers.SP1.class),
            cls(ClassWithTypeParamEnclosingOthers.class));
        verifyOwnerTypeConsistsOfTypes(type3, result3.getOwnerType(),
            cls(ClassWithTypeParamEnclosingOthers.S1.N2.class));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
        assertThat(result3, equalTo(type3));
    }

    /*
     * Checks the creation of a ParameterizedType for a class that is nested in multiple non-static classes.
     */
    @Test
    void shouldCreateParameterizedTypeWithOwnerForMultipleNestedNonStaticClasses() throws NoSuchFieldException {
        // given
        Class<?> clazz = ClassWithTypeParamEnclosingOthers.N1.N2.NP3.class;

        // when
        ParameterizedTypeImpl result = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz);

        // then
        Type type = clazz.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type, result.getOwnerType(),
            pt(ClassWithTypeParamEnclosingOthers.N1.N2.class),
            pt(ClassWithTypeParamEnclosingOthers.N1.class),
            pt(ClassWithTypeParamEnclosingOthers.class));
        assertThat(result, equalTo(type));
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
            String typeName = currentOwner instanceof Class<?>
                ? ((Class<?>) currentOwner).getSimpleName()
                : CommonTypeUtil.getRawType((ParameterizedType) currentOwner).getSimpleName();
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
            throw new IllegalStateException("Error of verifications on expected type. Are the expectations wrong?\n -->" + errorOnExpectedType);
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
                String actualType = currentOwnerLevel == null ? null
                    : (currentOwnerLevel instanceof Class<?>
                       ? "cls(" + ((Class<?>) currentOwnerLevel).getSimpleName() + ")"
                       : "pt(" + CommonTypeUtil.getRawType((ParameterizedType) currentOwnerLevel).getSimpleName()) + ")";
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

                        private class NP6<L> {
                            public NP6<L> selfTyped;
                        }
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