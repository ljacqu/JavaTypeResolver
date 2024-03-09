package ch.jalu.typeresolver.primitives;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test for {@link PrimitiveType}.
 */
class PrimitiveTypeTest {

    @Test
    void shouldConvertToPrimitiveClass() {
        // given / when / then
        assertThat(PrimitiveType.toPrimitiveType(Boolean.class), equalTo(boolean.class));
        assertThat(PrimitiveType.toPrimitiveType(Byte.class), equalTo(byte.class));
        assertThat(PrimitiveType.toPrimitiveType(Character.class), equalTo(char.class));
        assertThat(PrimitiveType.toPrimitiveType(Short.class), equalTo(short.class));
        assertThat(PrimitiveType.toPrimitiveType(Integer.class), equalTo(int.class));
        assertThat(PrimitiveType.toPrimitiveType(Long.class), equalTo(long.class));
        assertThat(PrimitiveType.toPrimitiveType(Double.class), equalTo(double.class));
        assertThat(PrimitiveType.toPrimitiveType(Float.class), equalTo(float.class));

        assertThat(PrimitiveType.toPrimitiveType(null), nullValue());
        assertThat(PrimitiveType.toPrimitiveType(Void.class), equalTo(Void.class));
        assertThat(PrimitiveType.toPrimitiveType(String.class), equalTo(String.class));
        assertThat(PrimitiveType.toPrimitiveType(List.class), equalTo(List.class));
    }

    @Test
    void shouldConvertToReferenceClass() {
        // given / when / then
        assertThat(PrimitiveType.toReferenceType(boolean.class), equalTo(Boolean.class));
        assertThat(PrimitiveType.toReferenceType(byte.class), equalTo(Byte.class));
        assertThat(PrimitiveType.toReferenceType(char.class), equalTo(Character.class));
        assertThat(PrimitiveType.toReferenceType(short.class), equalTo(Short.class));
        assertThat(PrimitiveType.toReferenceType(int.class), equalTo(Integer.class));
        assertThat(PrimitiveType.toReferenceType(long.class), equalTo(Long.class));
        assertThat(PrimitiveType.toReferenceType(double.class), equalTo(Double.class));
        assertThat(PrimitiveType.toReferenceType(float.class), equalTo(Float.class));

        assertThat(PrimitiveType.toReferenceType(null), nullValue());
        assertThat(PrimitiveType.toReferenceType(void.class), equalTo(void.class));
        assertThat(PrimitiveType.toReferenceType(String.class), equalTo(String.class));
        assertThat(PrimitiveType.toReferenceType(List.class), equalTo(List.class));
    }

    @Test
    void shouldHaveMatchingPrimitiveAndReferenceClasses() {
        // given / when / then
        for (PrimitiveType primType : PrimitiveType.values()) {
            assertThat(primType.getPrimitiveType().isPrimitive(), equalTo(true));
            assertThat(primType.getReferenceType().isPrimitive(), equalTo(false));
            assertThat(PrimitiveType.toPrimitiveType(primType.getReferenceType()), equalTo(primType.getPrimitiveType()));
        }
    }

    @Test
    void shouldHaveDefaultValueMatchingType() {
        // given / when / then
        for (PrimitiveType primType : PrimitiveType.values()) {
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
        for (PrimitiveType primType : PrimitiveType.values()) {
            assertThat(PrimitiveType.from(primType.getPrimitiveType()), equalTo(primType));
            assertThat(PrimitiveType.from(primType.getReferenceType()), equalTo(primType));
        }

        assertThat(PrimitiveType.from(null), nullValue());
        assertThat(PrimitiveType.from(void.class), nullValue());
        assertThat(PrimitiveType.from(Void.class), nullValue());
        assertThat(PrimitiveType.from(String.class), nullValue());
        assertThat(PrimitiveType.from(List.class), nullValue());
    }

    @Test
    void shouldReturnIfClassIsPrimitive() {
        // given / when / then
        assertThat(PrimitiveType.isRealPrimitive(boolean.class), equalTo(true));
        assertThat(PrimitiveType.isRealPrimitive(double.class), equalTo(true));
        assertThat(PrimitiveType.isRealPrimitive(float.class), equalTo(true));

        assertThat(PrimitiveType.isRealPrimitive(null), equalTo(false));
        assertThat(PrimitiveType.isRealPrimitive(void.class), equalTo(false));
        assertThat(PrimitiveType.isRealPrimitive(String.class), equalTo(false));
        assertThat(PrimitiveType.isRealPrimitive(List.class), equalTo(false));
    }

    @Test
    void shouldReturnIfClassMatches() {
        // given / when / then
        assertThat(PrimitiveType.BOOLEAN.matches(boolean.class), equalTo(true));
        assertThat(PrimitiveType.BOOLEAN.matches(Boolean.class), equalTo(true));
        assertThat(PrimitiveType.BOOLEAN.matches(int.class), equalTo(false));
        assertThat(PrimitiveType.BOOLEAN.matches(null), equalTo(false));
        assertThat(PrimitiveType.BOOLEAN.matches(String.class), equalTo(false));

        assertThat(PrimitiveType.INTEGER.matches(int.class), equalTo(true));
        assertThat(PrimitiveType.INTEGER.matches(Integer.class), equalTo(true));
        assertThat(PrimitiveType.INTEGER.matches(boolean.class), equalTo(false));
        assertThat(PrimitiveType.INTEGER.matches(Boolean.class), equalTo(false));
        assertThat(PrimitiveType.INTEGER.matches(String.class), equalTo(false));
    }
}