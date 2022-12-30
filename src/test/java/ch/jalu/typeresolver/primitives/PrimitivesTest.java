package ch.jalu.typeresolver.primitives;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test for {@link Primitives}.
 */
class PrimitivesTest {

    @Test
    void shouldConvertToPrimitiveClass() {
        // given / when / then
        assertThat(Primitives.toPrimitiveType(Boolean.class), equalTo(boolean.class));
        assertThat(Primitives.toPrimitiveType(Byte.class), equalTo(byte.class));
        assertThat(Primitives.toPrimitiveType(Character.class), equalTo(char.class));
        assertThat(Primitives.toPrimitiveType(Short.class), equalTo(short.class));
        assertThat(Primitives.toPrimitiveType(Integer.class), equalTo(int.class));
        assertThat(Primitives.toPrimitiveType(Long.class), equalTo(long.class));
        assertThat(Primitives.toPrimitiveType(Double.class), equalTo(double.class));
        assertThat(Primitives.toPrimitiveType(Float.class), equalTo(float.class));

        assertThat(Primitives.toPrimitiveType(null), nullValue());
        assertThat(Primitives.toPrimitiveType(Void.class), equalTo(Void.class));
        assertThat(Primitives.toPrimitiveType(String.class), equalTo(String.class));
        assertThat(Primitives.toPrimitiveType(List.class), equalTo(List.class));
    }

    @Test
    void shouldConvertToReferenceClass() {
        // given / when / then
        assertThat(Primitives.toReferenceType(boolean.class), equalTo(Boolean.class));
        assertThat(Primitives.toReferenceType(byte.class), equalTo(Byte.class));
        assertThat(Primitives.toReferenceType(char.class), equalTo(Character.class));
        assertThat(Primitives.toReferenceType(short.class), equalTo(Short.class));
        assertThat(Primitives.toReferenceType(int.class), equalTo(Integer.class));
        assertThat(Primitives.toReferenceType(long.class), equalTo(Long.class));
        assertThat(Primitives.toReferenceType(double.class), equalTo(Double.class));
        assertThat(Primitives.toReferenceType(float.class), equalTo(Float.class));

        assertThat(Primitives.toPrimitiveType(null), nullValue());
        assertThat(Primitives.toPrimitiveType(Void.class), equalTo(Void.class));
        assertThat(Primitives.toPrimitiveType(String.class), equalTo(String.class));
        assertThat(Primitives.toPrimitiveType(List.class), equalTo(List.class));
    }

    @Test
    void shouldHaveMatchingPrimitiveAndReferenceClasses() {
        // given / when / then
        for (Primitives primType : Primitives.values()) {
            assertThat(primType.getPrimitiveType().isPrimitive(), equalTo(true));
            assertThat(primType.getReferenceType().isPrimitive(), equalTo(false));
            assertThat(Primitives.toPrimitiveType(primType.getReferenceType()), equalTo(primType.getPrimitiveType()));
        }
    }

    @Test
    void shouldHaveDefaultValueMatchingType() {
        // given / when / then
        for (Primitives primType : Primitives.values()) {
            Object defaultValue = primType.getDefaultValue();
            assertThat(defaultValue, instanceOf(primType.getReferenceType()));

            switch (primType) {
                case BOOLEAN:
                    assertThat(defaultValue, equalTo(Boolean.FALSE));
                    break;
                case CHARACTER:
                    assertThat(defaultValue, equalTo((char) 0));
                    break;
                default:
                    assertThat(((Number) defaultValue).intValue(), equalTo(0));
            }
        }
    }

    @Test
    void shouldReturnAccordingEntry() {
        // given / when / then
        for (Primitives primType : Primitives.values()) {
            assertThat(Primitives.from(primType.getPrimitiveType()), equalTo(primType));
            assertThat(Primitives.from(primType.getReferenceType()), equalTo(primType));
        }

        assertThat(Primitives.from(null), nullValue());
        assertThat(Primitives.from(void.class), nullValue());
        assertThat(Primitives.from(Void.class), nullValue());
        assertThat(Primitives.from(String.class), nullValue());
        assertThat(Primitives.from(List.class), nullValue());
    }

    @Test
    void shouldReturnIfClassIsPrimitive() {
        // given / when / then
        assertThat(Primitives.isRealPrimitive(boolean.class), equalTo(true));
        assertThat(Primitives.isRealPrimitive(double.class), equalTo(true));
        assertThat(Primitives.isRealPrimitive(float.class), equalTo(true));

        assertThat(Primitives.isRealPrimitive(null), equalTo(false));
        assertThat(Primitives.isRealPrimitive(void.class), equalTo(false));
        assertThat(Primitives.isRealPrimitive(String.class), equalTo(false));
        assertThat(Primitives.isRealPrimitive(List.class), equalTo(false));
    }

    @Test
    void shouldReturnIfClassMatches() {
        // given / when / then
        assertThat(Primitives.BOOLEAN.matches(boolean.class), equalTo(true));
        assertThat(Primitives.BOOLEAN.matches(Boolean.class), equalTo(true));
        assertThat(Primitives.BOOLEAN.matches(int.class), equalTo(false));
        assertThat(Primitives.BOOLEAN.matches(null), equalTo(false));
        assertThat(Primitives.BOOLEAN.matches(String.class), equalTo(false));

        assertThat(Primitives.INTEGER.matches(int.class), equalTo(true));
        assertThat(Primitives.INTEGER.matches(Integer.class), equalTo(true));
        assertThat(Primitives.INTEGER.matches(boolean.class), equalTo(false));
        assertThat(Primitives.INTEGER.matches(Boolean.class), equalTo(false));
        assertThat(Primitives.INTEGER.matches(String.class), equalTo(false));
    }
}