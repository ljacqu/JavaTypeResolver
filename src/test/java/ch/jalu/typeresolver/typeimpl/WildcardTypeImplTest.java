package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.NestedTypeReference;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link WildcardTypeImpl}.
 */
class WildcardTypeImplTest {

    @Test
    void shouldReturnWildcardImplementationsInSyncWithJre() {
        // given
        WildcardType jreExtendsSerializable = new NestedTypeReference<List<? extends Serializable>>() { }.wildcardType();
        WildcardType jreSuperString = new NestedTypeReference<List<? super String>>() { }.wildcardType();
        WildcardType jreEmptyWildcard = new NestedTypeReference<List<?>>() { }.wildcardType();

        // when
        WildcardType extendsSerializable = WildcardTypeImpl.newWildcardExtends(Serializable.class);
        WildcardType superString = WildcardTypeImpl.newWildcardSuper(String.class);
        WildcardType emptyWildcard = WildcardTypeImpl.newUnboundedWildcard();

        // then
        assertEquals(extendsSerializable, jreExtendsSerializable);
        assertArrayEquals(extendsSerializable.getUpperBounds(), jreExtendsSerializable.getUpperBounds());
        assertArrayEquals(extendsSerializable.getLowerBounds(), jreExtendsSerializable.getLowerBounds());

        assertEquals(superString, jreSuperString);
        assertArrayEquals(superString.getUpperBounds(), jreSuperString.getUpperBounds());
        assertArrayEquals(superString.getLowerBounds(), jreSuperString.getLowerBounds());

        assertEquals(emptyWildcard, jreEmptyWildcard);
        assertArrayEquals(emptyWildcard.getUpperBounds(), jreEmptyWildcard.getUpperBounds());
        assertArrayEquals(emptyWildcard.getLowerBounds(), jreEmptyWildcard.getLowerBounds());
    }

    @Test
    void shouldDefineEqualsLikeOtherImplementations() {
        // given
        WildcardTypeImpl type1 = new WildcardTypeImpl(new Type[]{ Object.class }, new Type[]{});
        WildcardTypeImpl type2 = new WildcardTypeImpl(new Type[]{ Serializable.class }, new Type[]{});
        WildcardTypeImpl type3 = new WildcardTypeImpl(new Type[]{ Object.class }, new Type[]{ String.class });
        Type[] givenTypes = {type1, type2, type3};

        Type givenType1 = new NestedTypeReference<List<?>>() { }.getType();
        Type givenType2 = new NestedTypeReference<List<? extends Serializable>>() { }.getType();
        Type givenType3 = new NestedTypeReference<List<? super String>>() { }.getType();
        Type[] jreTypes = {givenType1, givenType2, givenType3};

        // when / then
        for (int i = 0; i < givenTypes.length; ++i) {
            for (int j = 0; j < jreTypes.length; ++j) {
                Type givenType = givenTypes[i];
                Type jdkType = jreTypes[j];

                boolean shouldMatch = (i == j);
                assertEquals(givenType.equals(jdkType), shouldMatch, i + "," + j);
                assertEquals(jdkType.equals(givenType), shouldMatch);
                assertEquals(givenType.hashCode() == jdkType.hashCode(), shouldMatch);
            }
        }
    }

    @Test
    void shouldHaveStringRepresentation() {
        // given
        WildcardTypeImpl type1 = new WildcardTypeImpl(new Type[]{ Object.class }, new Type[]{});
        WildcardTypeImpl type2 = new WildcardTypeImpl(new Type[]{ Serializable.class }, new Type[]{});
        WildcardTypeImpl type3 = new WildcardTypeImpl(new Type[]{ Object.class }, new Type[]{ String.class });

        // when / then
        assertEquals(type1.toString(), "?");
        assertEquals(type2.toString(), "? extends java.io.Serializable");
        assertEquals(type3.toString(), "? super java.lang.String");
    }
}