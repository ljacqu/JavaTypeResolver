package ch.jalu.typeresolver.reference;

import ch.jalu.typeresolver.TypeInfo;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.WildcardType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link NestedTypeReference}.
 */
class NestedTypeReferenceTest {

    @Test
    void shouldTakeInnerNestedType() {
        // given / when
        TypeInfo typeInfo = new NestedTypeReference<List<? super String>>() { };

        // then
        assertEquals(typeInfo.getType(), WildcardTypeImpl.newWildcardSuper(String.class));
    }

    @Test
    void shouldThrowForMissingInnerType() {
        // given / when / then
        assertThrows(IllegalStateException.class, () -> new NestedTypeReference() { });
        assertThrows(IllegalStateException.class, () -> new NestedTypeReference<List>() { });
    }

    @Test
    void shouldReturnWildcardTyped() {
        // given
        NestedTypeReference<List<?>> typeInfo = new NestedTypeReference<List<?>>() { };

        // when
        WildcardType wildcard = typeInfo.wildcardType();

        // then
        assertEquals(wildcard, WildcardTypeImpl.newUnboundedWildcard());
    }

    @Test
    void shouldThrowIfTypeIsNotWildcard() {
        // given
        NestedTypeReference<List<String[]>> typeInfo = new NestedTypeReference<List<String[]>>() { };

        // when / then
        assertThrows(IllegalStateException.class, typeInfo::wildcardType);
    }
}