package ch.jalu.typeresolver.typeimpl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Test for {@link GenericArrayTypeImpl}.
 */
class GenericArrayTypeImplTest {

    @Test
    void shouldBeEqualsToOtherImplementations() throws NoSuchFieldException {
        // given
        Type jvm1dArrayType = GenericArrayTypes.class.getDeclaredField("tArray1d").getGenericType();
        Type jvm3dArrayType = GenericArrayTypes.class.getDeclaredField("tArray3d").getGenericType();
        Type impl1dArrayType = new GenericArrayTypeImpl(GenericArrayTypes.class.getTypeParameters()[0]);
        Type impl3dArrayType = new GenericArrayTypeImpl(new GenericArrayTypeImpl(impl1dArrayType));

        // when / then
        assertThat(jvm1dArrayType, equalTo(impl1dArrayType));
        assertThat(impl1dArrayType, equalTo(jvm1dArrayType));
        assertThat(jvm1dArrayType.hashCode(), equalTo(impl1dArrayType.hashCode()));

        assertThat(jvm3dArrayType, equalTo(impl3dArrayType));
        assertThat(impl3dArrayType, equalTo(jvm3dArrayType));
        assertThat(jvm3dArrayType.hashCode(), equalTo(jvm1dArrayType.hashCode()));

        assertThat(jvm1dArrayType, not(impl3dArrayType));
        assertThat(impl3dArrayType, not(jvm1dArrayType));
        assertThat(impl1dArrayType, not(jvm3dArrayType));
        assertThat(jvm3dArrayType, not(impl1dArrayType));
    }

    @Test
    void shouldDefineToString() {
        // given
        Type impl1dArrayType = new GenericArrayTypeImpl(GenericArrayTypes.class.getTypeParameters()[0]);
        Type impl3dArrayType = new GenericArrayTypeImpl(new GenericArrayTypeImpl(impl1dArrayType));

        // when / then
        assertThat(impl1dArrayType.toString(), equalTo("T[]"));
        assertThat(impl3dArrayType.toString(), equalTo("T[][][]"));
    }

    private static final class GenericArrayTypes<T> {

        private T[] tArray1d;
        private T[][][] tArray3d;

    }
}