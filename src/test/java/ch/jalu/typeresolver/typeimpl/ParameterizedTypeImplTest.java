package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import org.junit.jupiter.api.Test;

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
            assertThat(actualArg.getGenericDeclaration(), equalTo(rawType));
        }
    }
}