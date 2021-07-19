package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.reference.NestedTypeReference;
import ch.jalu.typeresolver.reference.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link GenericArrayTypeImpl}.
 */
class GenericArrayTypeImplTest extends AbstractTypeImplTest {

    GenericArrayTypeImplTest() throws NoSuchFieldException {
        super(
            new GenericArrayTypeImpl(GenericArrayTypes.class.getTypeParameters()[0]), // T[]
            GenericArrayTypeImpl.create(GenericArrayTypes.class.getTypeParameters()[0], 3), // T[][][]
            GenericArrayTypeImpl.create(new TypeReference<List<String>>(){ }.getType(), 2), // List<String>[][]
            GenericArrayTypes.class.getDeclaredField("tArray1d").getGenericType(),
            GenericArrayTypes.class.getDeclaredField("tArray3d").getGenericType(),
            GenericArrayTypes.class.getDeclaredField("stringListArray").getGenericType());
    }

    @Test
    @Override
    void shouldHaveSameHashCodeAsImplementation() {
        // given / when / then
        assertThat(types[0].hashCode(), equalTo(jreTypes[0].hashCode()));
        assertThat(types[1].hashCode(), equalTo(jreTypes[1].hashCode()));
        assertThat(types[2].hashCode(), equalTo(jreTypes[2].hashCode()));
    }

    @Test
    void shouldOutputClassNameInToString() {
        // given
        Type genericArrayType = GenericArrayTypeImpl.create(TimeUnit.class, 2);

        // when / then
        assertThat(genericArrayType.toString(), equalTo("java.util.concurrent.TimeUnit[][]"));
    }

    @Test
    void shouldReturnSameTypeForZeroDimension() {
        // given
        Type type1 = new TypeReference<Optional<String>>() { }.getType();
        Type type2 = new NestedTypeReference<List<? extends TimeUnit>>() { }.getType();

        // when / then
        assertThat(GenericArrayTypeImpl.create(type1, 0), equalTo(type1));
        assertThat(GenericArrayTypeImpl.create(type2, 0), equalTo(type2));
    }

    @Test
    void shouldThrowForNegativeDimensions() {
        // given
        Type type = new TypeReference<List<double[]>>() { }.getType();

        // when / then
        assertThrows(IllegalArgumentException.class, () -> GenericArrayTypeImpl.create(type, -1));
        assertThrows(IllegalArgumentException.class, () -> GenericArrayTypeImpl.create(type, -5));
    }

    private static final class GenericArrayTypes<T> {

        private T[] tArray1d;
        private T[][][] tArray3d;
        private List<String>[][] stringListArray;

    }
}