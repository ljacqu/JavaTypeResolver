package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BIG_DECIMAL;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BIG_INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BYTE;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.DOUBLE;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.FLOAT;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.LONG;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.SHORT;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.getEnumToReadValueOrThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link StandardNumberTypeEnum}.
 */
class StandardNumberTypeEnumTest {

    // Note: StandardNumberType tests conversions among number types, which uses the enum implementation in the
    // background, so StandardNumberTypeTest covers all conversions of the enum class.

    @Test
    void shouldReturnAppropriateEnumEntry() {
        // given / when / then
        assertThat(StandardNumberTypeEnum.getEnumToReadValueOrThrow((short) 2), equalTo(StandardNumberTypeEnum.SHORT));
        assertThat(StandardNumberTypeEnum.getEnumToReadValueOrThrow(30), equalTo(StandardNumberTypeEnum.INTEGER));
        assertThat(StandardNumberTypeEnum.getEnumToReadValueOrThrow(45f), equalTo(StandardNumberTypeEnum.FLOAT));
        assertThat(StandardNumberTypeEnum.getEnumToReadValueOrThrow(BigDecimal.TEN), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));

        BigInteger bigInteger = new BigInteger(new byte[]{ 20 }) { };
        assertThat(StandardNumberTypeEnum.getEnumToReadValueOrThrow(bigInteger), equalTo(StandardNumberTypeEnum.BIG_INTEGER));

        BigDecimal bigDecimalExt = new BigDecimal("20") { };
        assertThat(StandardNumberTypeEnum.getEnumToReadValueOrThrow(bigDecimalExt), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));
    }

    @Test
    void shouldThrowForUnknownType() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> getEnumToReadValueOrThrow(new AtomicLong(32)));

        // then
        assertThat(ex.getMessage(), equalTo("Unsupported number argument: class java.util.concurrent.atomic.AtomicLong"));
    }

    @Test
    void shouldConvertToStandardNumberType() {
        // given
        List<StandardNumberType<?>> allNumberTypes = StandardNumberType.streamThroughAll().collect(Collectors.toList());
        List<StandardNumberType<?>> seenNumberTypes = new ArrayList<>();

        for (StandardNumberTypeEnum enumEntry : StandardNumberTypeEnum.values()) {

            // when
            StandardNumberType<?> numberType = enumEntry.asNumberType();

            // then
            assertThat(enumEntry.getType(), equalTo(numberType.getType()));
            assertThat(numberType.asEnum(), equalTo(enumEntry));
            seenNumberTypes.add(numberType);
        }

        assertThat(seenNumberTypes, equalTo(allNumberTypes));
    }

    @ParameterizedTest
    @EnumSource(StandardNumberTypeEnum.class)
    void shouldReturnWhetherItSupportsAllValuesOfOtherType(StandardNumberTypeEnum type) {
        // given / when
        Set<StandardNumberTypeEnum> fullySupportedTypes = Arrays.stream(StandardNumberTypeEnum.values())
            .filter(type::supportsAllValuesOf)
            .collect(Collectors.toSet());

        // then
        assertThat(fullySupportedTypes, equalTo(getExpectedFullySupportedTypes(type)));
    }

    private static EnumSet<StandardNumberTypeEnum> getExpectedFullySupportedTypes(StandardNumberTypeEnum type) {
        switch (type) {
            case BYTE:
                return EnumSet.of(BYTE);
            case SHORT:
                return EnumSet.of(BYTE, SHORT);
            case INTEGER:
                return EnumSet.of(BYTE, SHORT, INTEGER);
            case LONG:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG);
            case FLOAT:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG, FLOAT);
            case DOUBLE:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}