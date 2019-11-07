package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.samples.nestedclasses.InnerParameterizedClassesContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
}