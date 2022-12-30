package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.CommonTypeUtil;
import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import org.junit.jupiter.api.Test;

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
        assertThat(result.getOwnerType(), equalTo(SP1.SP2.class));

        Field field = clazz.getDeclaredField("selfTyped");
        Type type = field.getGenericType();
        assertThat(result, equalTo(type));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass() throws NoSuchFieldException {
        // given
        Class<?> clazz = SP1.NP1.NP2.NP3.class;

        // when
        ParameterizedTypeImpl result = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz);

        // then
        Type type = clazz.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type, result.getOwnerType(),
            pt(SP1.NP1.NP2.class),
            pt(SP1.NP1.class),
            pt(SP1.class),
            cls(getClass()));

        assertThat(result, equalTo(type));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass2() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = SP.N.NP1.NP2.class;
        Class<?> clazz2 = SP.SP2.NP1.NP2.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            pt(SP.N.NP1.class),
            cls(SP.N.class));
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(SP.SP2.NP1.class),
            pt(SP.SP2.class),
            cls(SP.class));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass3() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = SP.NP1.N.NLP1.class;
        Class<?> clazz2 = SP.NP1.N.NLP1.NLP2.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            pt(SP.NP1.N.class),
            pt(SP.NP1.class),
            pt(SP.class),
            cls(getClass()));

        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(SP.NP1.N.NLP1.class),
            pt(SP.NP1.N.class),
            pt(SP.NP1.class),
            pt(SP.class),
            cls(getClass()));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
    }

    @Test
    void shouldCreateParameterizedTypeForNestedClass4() throws NoSuchFieldException {
        // given
        Class<?> clazz1 = SP.SP2.NP4.N.NLP5.class;
        Class<?> clazz2 = SP.SP2.NP4.N.NLP5.NLP6.class;

        // when
        ParameterizedTypeImpl result1 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz1);
        ParameterizedTypeImpl result2 = ParameterizedTypeImpl.newTypeWithTypeParameters(clazz2);

        // then
        Type type1 = clazz1.getDeclaredField("selfTyped").getGenericType();
        Type type2 = clazz2.getDeclaredField("selfTyped").getGenericType();

        verifyOwnerTypeConsistsOfTypes(type1, result1.getOwnerType(),
            pt(SP.SP2.NP4.N.class),
            pt(SP.SP2.NP4.class),
            pt(SP.SP2.class),
            cls(SP.class));
        verifyOwnerTypeConsistsOfTypes(type2, result2.getOwnerType(),
            pt(SP.SP2.NP4.N.NLP5.class),
            pt(SP.SP2.NP4.N.class),
            pt(SP.SP2.NP4.class),
            pt(SP.SP2.class),
            cls(SP.class));

        assertThat(result1, equalTo(type1));
        assertThat(result2, equalTo(type2));
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
        String errorOnActualType = verifyOwnerTypesAndReturnErrorIfApplicable(((ParameterizedType) actualType).getOwnerType(), expectedTypes);
        if (errorOnActualType != null) {
            throw new IllegalStateException("Error on actual type. Are the expectations wrong? --> \n" + errorOnActualType);
        }

        String error = verifyOwnerTypesAndReturnErrorIfApplicable(ownerType, expectedTypes);
        if (error != null) {
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
     * Leaf classes:
     *   - SP1<A>.SP2<B>.SP3<C>
     *   - SP1<A>.NP1<N>.NP2<O>.NP3<P>
     * where s = static and n = not static (-> nested)
     */
    private static final class SP1<A> {

        private static final class SP2<B> {

            private static class SP3<C> {
                private SP3<C> selfTyped;
            }
        }

        private class NP1<N> {

            private class NP2<O> {

                private class NP3<P> {
                    private NP3<P> selfTyped;

                }
            }
        }
    }

    /*
     * Leaf classes:
     *   - SP<A, B>.N     .NP1<N>.NP2<O>
     *   - SP<A, B>.SP2<F>.NP1<N>.NP2<O>
     *   - SP<A, B>.SP2<F>.NP4<C>.N.NLP5<K>.NLP6<L>
     *   - SP<A, B>.NP1<C>.N.NLP1<K>.NLP2<L>
     */
    private static final class SP<A, B> {

        private static class N {

            private class NP1<N> {

                private class NP2<O> {
                    private NP2<O> selfTyped;

                }
            }
        }

        private static final class SP2<F> {

            private class NP1<N> {

                private class NP2<O> {
                    private NP2<O> selfTyped;

                }
            }

            private class NP4<C> {

                private class N {

                    private class NLP5<K> {
                        private NLP5<K> selfTyped;

                        private class NLP6<L> {
                            private NLP6<L> selfTyped;
                        }
                    }
                }
            }
        }

        private class NP1<C> {

            private class N {

                private class NLP1<K> {
                    private NLP1<K> selfTyped;

                    private class NLP2<L> {
                        private NLP2<L> selfTyped;
                    }
                }
            }
        }
    }
}