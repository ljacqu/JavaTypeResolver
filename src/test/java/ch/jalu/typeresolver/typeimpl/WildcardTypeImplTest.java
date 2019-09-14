package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.TypeInfo;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link WildcardTypeImpl}.
 */
class WildcardTypeImplTest {

    @Test
    void shouldReturnWildcardImplementationsInSyncWithJre() {
        // given
        Type type1 = new TypeToken<List<? extends Serializable>>() { }.getType();
        WildcardType jreExtendsSerializable = (WildcardType) new TypeInfo(type1).getTypeArgumentInfo(0).getType();
        Type type2 = new TypeToken<List<? super String>>() { }.getType();
        WildcardType jreSuperString = (WildcardType) new TypeInfo(type2).getTypeArgumentInfo(0).getType();
        Type type3 = new TypeToken<List<?>>() { }.getType();
        WildcardType jreEmptyWildcard = (WildcardType) new TypeInfo(type3).getTypeArgumentInfo(0).getType();

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

        Type givenType1 = new TypeToken<List<?>>() { }.getType();
        Type givenType2 = new TypeToken<List<? extends Serializable>>() { }.getType();
        Type givenType3 = new TypeToken<List<? super String>>() { }.getType();
        Type[] jdkTypes = Stream.of(givenType1, givenType2, givenType3)
            .map(type -> new TypeInfo(type).getTypeArgumentInfo(0).getType())
            .toArray(Type[]::new);

        // when / then
        for (int i = 0; i < givenTypes.length; ++i) {
            for (int j = 0; j < jdkTypes.length; ++j) {
                Type givenType = givenTypes[i];
                Type jdkType = jdkTypes[j];

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