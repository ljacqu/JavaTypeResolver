package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Test for {@link ParameterizedTypeImpl}.
 */
class ParameterizedTypeImplTest {

    @Test
    public void shouldBeEqualToOtherImplementations() {
        // given
        Type doubleListType = new TypeReference<List<Double>>() { }.getType();
        Type mapType = new TypeReference<Map<String, Set<Short[]>>>() { }.getType();

        ParameterizedTypeImpl listPt = new ParameterizedTypeImpl(List.class, null, new Type[]{ Double.class });
        ParameterizedTypeImpl setPt = new ParameterizedTypeImpl(Set.class, null, new Type[]{ Short[].class });
        ParameterizedTypeImpl mapPt = new ParameterizedTypeImpl(Map.class, null, new Type[]{ String.class, setPt });

        // when / then
        assertThat(doubleListType, equalTo(listPt));
        assertThat(listPt, equalTo(doubleListType));
        assertThat(listPt, equalTo(listPt));
        assertThat(listPt.hashCode(), equalTo(doubleListType.hashCode()));

        assertThat(mapType, equalTo(mapPt));
        assertThat(mapPt, equalTo(mapType));
        assertThat(mapPt, equalTo(mapPt));
        assertThat(mapPt.hashCode(), equalTo(mapType.hashCode()));

        assertThat(doubleListType, not(mapPt));
        assertThat(mapPt, not(doubleListType));
        assertThat(listPt, not(mapType));
        assertThat(mapType, not(listPt));
        assertThat(mapPt, not(listPt));
    }

    @Test
    void shouldDefineUsableToString() {
        // given
        Type doubleListType = new TypeReference<List<Double>>() { }.getType();
        Type mapType = new TypeReference<Map<String, Set<Short[]>>>() { }.getType();

        ParameterizedTypeImpl listPt = new ParameterizedTypeImpl(List.class, null, new Type[]{ Double.class });
        ParameterizedTypeImpl setPt = new ParameterizedTypeImpl(Set.class, null, new Type[]{ Short[].class });
        ParameterizedTypeImpl mapPt = new ParameterizedTypeImpl(Map.class, null, new Type[]{ String.class, setPt });

        // when / then
        assertThat(listPt.toString(), equalTo(doubleListType.toString()));
        assertThat(mapPt.toString(), equalTo(mapType.toString()));
    }
}