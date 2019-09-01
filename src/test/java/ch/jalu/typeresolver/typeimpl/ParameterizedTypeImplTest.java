package ch.jalu.typeresolver.typeimpl;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test for {@link ParameterizedTypeImpl}.
 */
class ParameterizedTypeImplTest {

    @Test
    public void shouldBeEqualToOtherImplementations() {
        // given
        Type doubleListType = new TypeToken<List<Double>>() { }.getType();
        Type mapType = new TypeToken<Map<String, Set<Short[]>>>() { }.getType();

        ParameterizedTypeImpl listPt = new ParameterizedTypeImpl(List.class, null, new Type[]{ Double.class });
        ParameterizedTypeImpl setPt = new ParameterizedTypeImpl(Set.class, null, new Type[]{ Short[].class });
        ParameterizedTypeImpl mapPt = new ParameterizedTypeImpl(Map.class, null, new Type[]{ String.class, setPt });

        // when / then
        assertEquals(doubleListType, listPt);
        assertEquals(listPt, doubleListType);
        assertEquals(listPt, listPt);
        assertEquals(listPt.hashCode(), doubleListType.hashCode());

        assertEquals(mapType, mapPt);
        assertEquals(mapPt, mapType);
        assertEquals(mapPt, mapPt);
        assertEquals(mapPt.hashCode(), mapType.hashCode());

        assertNotEquals(doubleListType, mapPt);
        assertNotEquals(mapPt, doubleListType);
        assertNotEquals(listPt, mapType);
        assertNotEquals(mapType, listPt);
        assertNotEquals(mapPt, listPt);
    }

    @Test
    void shouldDefineUsableToString() {
        // given
        Type doubleListType = new TypeToken<List<Double>>() { }.getType();
        Type mapType = new TypeToken<Map<String, Set<Short[]>>>() { }.getType();

        ParameterizedTypeImpl listPt = new ParameterizedTypeImpl(List.class, null, new Type[]{ Double.class });
        ParameterizedTypeImpl setPt = new ParameterizedTypeImpl(Set.class, null, new Type[]{ Short[].class });
        ParameterizedTypeImpl mapPt = new ParameterizedTypeImpl(Map.class, null, new Type[]{ String.class, setPt });

        // when / then
        assertEquals(listPt.toString(), doubleListType.toString());
        assertEquals(mapPt.toString(), mapType.toString());
    }
}