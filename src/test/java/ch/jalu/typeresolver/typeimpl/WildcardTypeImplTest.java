package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.NestedTypeReference;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link WildcardTypeImpl}.
 */
class WildcardTypeImplTest extends AbstractTypeImplTest {

    WildcardTypeImplTest() {
        super(
            WildcardTypeImpl.newWildcardExtends(Serializable.class),
            WildcardTypeImpl.newWildcardSuper(String.class),
            WildcardTypeImpl.newUnboundedWildcard(),
            new NestedTypeReference<List<? extends Serializable>>() { }.getType(),
            new NestedTypeReference<List<? super String>>() { }.getType(),
            new NestedTypeReference<List<?>>() { }.getType());
    }

    @Test
    void shouldIncludeAllBoundsInToString() {
        // given
        WildcardTypeImpl wildcardType = new WildcardTypeImpl(new Type[]{String.class, Serializable.class}, new Type[0]);

        // when / then
        assertThat(wildcardType.toString(), equalTo("? extends java.lang.String & java.io.Serializable"));
    }
}