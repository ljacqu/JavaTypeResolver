package ch.jalu.typeresolver.typeimpl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertEquals(jvm1dArrayType, impl1dArrayType);
        assertEquals(impl1dArrayType, jvm1dArrayType);
        assertEquals(jvm1dArrayType.hashCode(), impl1dArrayType.hashCode());

        assertEquals(jvm3dArrayType, impl3dArrayType);
        assertEquals(impl3dArrayType, jvm3dArrayType);
        assertEquals(jvm3dArrayType.hashCode(), jvm1dArrayType.hashCode());

        assertNotEquals(jvm1dArrayType, impl3dArrayType);
        assertNotEquals(impl3dArrayType, jvm1dArrayType);
        assertNotEquals(impl1dArrayType, jvm3dArrayType);
        assertNotEquals(jvm3dArrayType, impl1dArrayType);
    }

    @Test
    void shouldDefineToString() {
        // given
        Type impl1dArrayType = new GenericArrayTypeImpl(GenericArrayTypes.class.getTypeParameters()[0]);
        Type impl3dArrayType = new GenericArrayTypeImpl(new GenericArrayTypeImpl(impl1dArrayType));

        // when / then
        assertEquals(impl1dArrayType.toString(), "T[]");
        assertEquals(impl3dArrayType.toString(), "T[][][]");
    }

    private static final class GenericArrayTypes<T> {

        private T[] tArray1d;
        private T[][][] tArray3d;


    }

}