package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.NestedTypeReference;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
        assertThat(extendsSerializable, equalTo(jreExtendsSerializable));
        assertThat(extendsSerializable.getUpperBounds(), equalTo(jreExtendsSerializable.getUpperBounds()));
        assertThat(extendsSerializable.getLowerBounds(), equalTo(jreExtendsSerializable.getLowerBounds()));

        assertThat(superString, equalTo(jreSuperString));
        assertThat(superString.getUpperBounds(), equalTo(jreSuperString.getUpperBounds()));
        assertThat(superString.getLowerBounds(), equalTo(jreSuperString.getLowerBounds()));

        assertThat(emptyWildcard, equalTo(jreEmptyWildcard));
        assertThat(emptyWildcard.getUpperBounds(), equalTo(jreEmptyWildcard.getUpperBounds()));
        assertThat(emptyWildcard.getLowerBounds(), equalTo(jreEmptyWildcard.getLowerBounds()));
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
                assertThat(i + "," + j, givenType.equals(jdkType), equalTo(shouldMatch));
                assertThat(i + "," + j, jdkType.equals(givenType), equalTo(shouldMatch));
                assertThat(i + "," + j, givenType.hashCode() == jdkType.hashCode(), equalTo(shouldMatch));
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
        assertThat(type1.toString(), equalTo("?"));
        assertThat(type2.toString(), equalTo("? extends java.io.Serializable"));
        assertThat(type3.toString(), equalTo("? super java.lang.String"));
    }
}