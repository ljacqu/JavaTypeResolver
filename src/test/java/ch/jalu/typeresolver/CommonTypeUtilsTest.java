package ch.jalu.typeresolver;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test for {@link CommonTypeUtils}.
 */
class CommonTypeUtilsTest {

    @Test
    void shouldReturnRawTypeAsClass() {
        // given
        ParameterizedType list = (ParameterizedType) new TypeReference<List<String>>() { }.getType();
        ParameterizedType map = (ParameterizedType) new TypeReference<Map<Integer, ?>>() { }.getType();

        // when / then
        assertThat(CommonTypeUtils.getRawType(list), equalTo(List.class));
        assertThat(CommonTypeUtils.getRawType(map), equalTo(Map.class));
    }

    @Test
    void shouldSpecifyIfHasUpperBounds() {
        // given
        WildcardTypeImpl wildcardWithObject = new WildcardTypeImpl(new Type[]{ Object.class }, new Type[0]);
        WildcardTypeImpl wildcardWithString = new WildcardTypeImpl(new Type[]{ String.class }, new Type[0]);

        // The following are never returned by the JRE (upperBounds always has 1 entry)
        WildcardTypeImpl emptyWildcard = new WildcardTypeImpl(new Type[0], new Type[0]);
        WildcardTypeImpl wildcardWithObjectAndString = new WildcardTypeImpl(new Type[]{ Object.class, String.class }, new Type[0]);

        // when / then
        assertThat(CommonTypeUtils.hasExplicitUpperBound(wildcardWithObject), equalTo(false));
        assertThat(CommonTypeUtils.hasExplicitUpperBound(wildcardWithString), equalTo(true));

        assertThat(CommonTypeUtils.hasExplicitUpperBound(emptyWildcard), equalTo(false));
        assertThat(CommonTypeUtils.hasExplicitUpperBound(wildcardWithObjectAndString), equalTo(true));
    }

    @Test
    void shouldReturnDefinitiveClassAsType() {
        // given
        Type stringList = new TypeReference<List<String>>() { }.getType();
        Type stringListArr = new TypeReference<List<String>[][]>() { }.getType();

        // when / then
        assertThat(CommonTypeUtils.getDefinitiveClass(String.class), equalTo(String.class));
        assertThat(CommonTypeUtils.getDefinitiveClass(stringList), equalTo(List.class));
        assertThat(CommonTypeUtils.getDefinitiveClass(stringListArr), equalTo(List[][].class));
    }

    @Test
    void shouldReturnNullIfNoDefinitiveClassIsApplicable() {
        // given
        Type wildcard = WildcardTypeImpl.newWildcardExtends(String.class);
        Type wildcardArray = new GenericArrayTypeImpl(wildcard);

        // when / then
        assertThat(CommonTypeUtils.getDefinitiveClass(wildcard), nullValue());
        assertThat(CommonTypeUtils.getDefinitiveClass(wildcardArray), nullValue());
    }
}