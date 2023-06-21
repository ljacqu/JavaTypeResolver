package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.primitives.Primitives;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BIG_DECIMAL;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BIG_INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BYTE;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.DOUBLE;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.FLOAT;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.LONG;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.SHORT;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.findEntryForReadingValueOrThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link StandardNumberTypeEnum}.
 */
class StandardNumberTypeEnumTest {

    /**
     * Tests the code sample in the JavaDoc of {@link StandardNumberTypeEnum#findEntryForReadingValueOrThrow}. This test
     * attempts to ensure that the code in the Javadoc is correct and runnable.
     */
    @Test
    void shouldHaveRunnableExampleCodeInJavadoc() {
        BigDecimal bigDecimalExtension = new BigDecimal("20") { }; // anonymous extension
        StandardNumberTypeEnum entry = StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigDecimalExtension);
        // System.out.println(entry.convertUnsafe(0).getClass().equals(bigDecimalExtension.getClass())); // false

        boolean output = entry.convertUnsafe(0).getClass().equals(bigDecimalExtension.getClass());
        assertThat(output, equalTo(false));
    }

    /**
     * Tests {@link StandardNumberTypeEnum#fromNumberClass},
     * {@link StandardNumberTypeEnum#fromClass},
     * {@link StandardNumberTypeEnum#findEntryForReadingValueOrThrow},
     * and various streaming methods.
     */
    @Nested
    class EntryRetrievalTests {

        @Test
        void shouldReturnTypeForNumberClass() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.fromNumberClass(int.class), equalTo(StandardNumberTypeEnum.T_INTEGER));
            assertThat(StandardNumberTypeEnum.fromNumberClass(Double.class), equalTo(StandardNumberTypeEnum.T_DOUBLE));
            assertThat(StandardNumberTypeEnum.fromNumberClass(BigDecimal.class), equalTo(StandardNumberTypeEnum.T_BIG_DECIMAL));

            assertThat(StandardNumberTypeEnum.fromNumberClass(Number.class), nullValue());
            assertThat(StandardNumberTypeEnum.fromNumberClass(NumberTestImpl.class), nullValue());
            assertThat(StandardNumberTypeEnum.fromNumberClass(null), nullValue());
        }

        @Test
        void shouldReturnTypeForClass() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.fromClass(int.class), equalTo(StandardNumberTypeEnum.INTEGER));
            assertThat(StandardNumberTypeEnum.fromClass(Double.class), equalTo(StandardNumberTypeEnum.DOUBLE));
            assertThat(StandardNumberTypeEnum.fromClass(BigDecimal.class), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));

            assertThat(StandardNumberTypeEnum.fromClass(String.class), nullValue());
            assertThat(StandardNumberTypeEnum.fromClass(NumberTestImpl.class), nullValue());
            assertThat(StandardNumberTypeEnum.fromClass(null), nullValue());

            StandardNumberTypeEnum.streamThroughAll().forEach(numberType -> {
                assertThat(StandardNumberTypeEnum.fromClass(numberType.getType()), sameInstance(numberType));

                Class<?> primitiveType = Primitives.toPrimitiveType(numberType.getType());
                if (!primitiveType.equals(numberType.getType())) {
                    assertThat(StandardNumberTypeEnum.fromClass(primitiveType), sameInstance(numberType));
                }
            });
        }

        @Test
        void shouldReturnAppropriateEnumEntry() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow((short) 2), equalTo(StandardNumberTypeEnum.SHORT));
            assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(30), equalTo(StandardNumberTypeEnum.INTEGER));
            assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(45f), equalTo(StandardNumberTypeEnum.FLOAT));
            assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(BigDecimal.TEN), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));

            BigInteger bigInteger = new BigInteger(new byte[]{ 20 }) { };
            assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigInteger), equalTo(StandardNumberTypeEnum.BIG_INTEGER));

            BigDecimal bigDecimalExt = new BigDecimal("20") { };
            assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigDecimalExt), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));
        }

        @Test
        void shouldThrowForUnknownType() {
            // given / when
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> findEntryForReadingValueOrThrow(new AtomicLong(32)));

            // then
            assertThat(ex.getMessage(), equalTo("Unsupported number type: class java.util.concurrent.atomic.AtomicLong"));
        }

        @Test
        void shouldReturnAllNumberTypesAsNumberTypeInterfaces() {
            // given / when
            List<NumberType<? extends Number>> allValues = StandardNumberTypeEnum.streamThroughAll()
                .collect(Collectors.toList());

            // then
            assertThat(allValues, contains(StandardNumberTypeEnum.values()));
        }

        @Test
        void shouldReturnPrimitiveTypesAndBeInSyncWithPrimitiveTypes() {
            // given / when
            List<NumberType<?>> primitiveNumberTypes = StandardNumberTypeEnum.streamThroughPrimitiveTypes()
                .collect(Collectors.toList());

            // then
            assertThat(primitiveNumberTypes, contains(StandardNumberTypeEnum.BYTE, StandardNumberTypeEnum.SHORT,
                StandardNumberTypeEnum.INTEGER, StandardNumberTypeEnum.LONG,
                StandardNumberTypeEnum.FLOAT, StandardNumberTypeEnum.DOUBLE));

            Set<NumberType<?>> allTypesWithPrimitiveEquivalent = StandardNumberTypeEnum.streamThroughAll()
                .filter(type -> Primitives.from(type.getType()) != null)
                .collect(Collectors.toSet());
            assertThat(new HashSet<>(primitiveNumberTypes), equalTo(allTypesWithPrimitiveEquivalent));
        }
    }

    /**
     * Tests {@link StandardNumberTypeEnum#supportsAllValuesOf},
     * {@link StandardNumberTypeEnum#getValueRange},
     * {@link StandardNumberTypeEnum#compareToValueRange}.
     */
    @Nested
    class RangeTests {

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

        @Test
        void shouldReturnWhetherTypeSupportsMoreNumberTypeValues() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.T_BYTE.supportsAllValuesOf(MoreNumberTypes.CHARACTER), equalTo(false));
            assertThat(StandardNumberTypeEnum.T_BYTE.supportsAllValuesOf(MoreNumberTypes.ATOMIC_INTEGER), equalTo(false));
            assertThat(StandardNumberTypeEnum.T_BYTE.supportsAllValuesOf(MoreNumberTypes.ATOMIC_LONG), equalTo(false));

            assertThat(StandardNumberTypeEnum.T_SHORT.supportsAllValuesOf(MoreNumberTypes.CHARACTER), equalTo(false));
            assertThat(StandardNumberTypeEnum.T_SHORT.supportsAllValuesOf(MoreNumberTypes.ATOMIC_INTEGER), equalTo(false));
            assertThat(StandardNumberTypeEnum.T_SHORT.supportsAllValuesOf(MoreNumberTypes.ATOMIC_LONG), equalTo(false));

            assertThat(StandardNumberTypeEnum.T_INTEGER.supportsAllValuesOf(MoreNumberTypes.CHARACTER), equalTo(true));
            assertThat(StandardNumberTypeEnum.T_INTEGER.supportsAllValuesOf(MoreNumberTypes.ATOMIC_INTEGER), equalTo(true));
            assertThat(StandardNumberTypeEnum.T_INTEGER.supportsAllValuesOf(MoreNumberTypes.ATOMIC_LONG), equalTo(false));

            Stream.of(StandardNumberTypeEnum.T_LONG, StandardNumberTypeEnum.T_FLOAT, StandardNumberTypeEnum.T_DOUBLE,
                    StandardNumberTypeEnum.T_BIG_INTEGER, StandardNumberTypeEnum.T_BIG_DECIMAL)
                .forEach(numberType -> {
                    assertThat(numberType.supportsAllValuesOf(MoreNumberTypes.CHARACTER), equalTo(true));
                    assertThat(numberType.supportsAllValuesOf(MoreNumberTypes.ATOMIC_INTEGER), equalTo(true));
                    assertThat(numberType.supportsAllValuesOf(MoreNumberTypes.ATOMIC_LONG), equalTo(true));
                });
        }

        @Test
        void shouldReturnValueRanges() {
            // given
            Set<NumberType<?>> typesWithDecimals = new HashSet<>();
            typesWithDecimals.add(StandardNumberTypeEnum.FLOAT);
            typesWithDecimals.add(StandardNumberTypeEnum.DOUBLE);
            typesWithDecimals.add(StandardNumberTypeEnum.BIG_DECIMAL);

            StandardNumberTypeEnum.streamThroughAll().forEach(numberType -> {
                // when
                ValueRange<? extends Number> valueRange = numberType.getValueRange();

                // then
                assertThat(valueRange.supportsDecimals(), equalTo(typesWithDecimals.contains(numberType)));
                if (numberType == StandardNumberTypeEnum.BIG_INTEGER || numberType == StandardNumberTypeEnum.BIG_DECIMAL) {
                    assertThat(valueRange.getMinValue(), nullValue());
                    assertThat(valueRange.getMaxValue(), nullValue());
                } else {
                    assertThat(valueRange.getMinValue(), lessThan(valueRange.getMaxValue()));
                }

                if (valueRange.getMinInOwnType() != null || valueRange.getMinValue() != null) {
                    assertThat(valueRange.getMinInOwnType(), instanceOf(numberType.getType()));
                    assertThat(BIG_DECIMAL.convertUnsafe(valueRange.getMinInOwnType()), equalTo(valueRange.getMinValue()));
                }
                if (valueRange.getMaxInOwnType() != null || valueRange.getMaxValue() != null) {
                    assertThat(valueRange.getMaxInOwnType(), instanceOf(numberType.getType()));
                    assertThat(BIG_DECIMAL.convertUnsafe(valueRange.getMaxInOwnType()), equalTo(valueRange.getMaxValue()));
                }

                if (numberType == StandardNumberTypeEnum.FLOAT || numberType == StandardNumberTypeEnum.DOUBLE) {
                    assertThat(valueRange.hasInfinityAndNaN(), equalTo(true));
                } else {
                    assertThat(valueRange.hasInfinityAndNaN(), equalTo(false));
                }
            });
        }

        @Test
        void shouldHaveExpectedValueRangeSupersetsOrEquivalencies() {
            // given
            Map<StandardNumberTypeEnum, ValueRange> rangeByNumberType = Arrays.stream(StandardNumberTypeEnum.values())
                .collect(Collectors.toMap(Function.identity(), NumberType::getValueRange));

            // when
            Arrays.stream(StandardNumberTypeEnum.values()).forEach(numberType -> {
                ValueRange<?> testedRange = numberType.getValueRange();

                Set<StandardNumberTypeEnum> rangesNumberTypeIsGreaterOrEqualTo = rangeByNumberType.entrySet().stream()
                    .filter(range -> testedRange.isEqualOrSupersetOf(range.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
                Set<StandardNumberTypeEnum> rangesNumberTypeFullySupports = Arrays.stream(StandardNumberTypeEnum.values())
                    .filter(numberType::supportsAllValuesOf)
                    .collect(Collectors.toSet());

                // then
                assertThat(numberType.toString(),
                    rangesNumberTypeIsGreaterOrEqualTo, containsInAnyOrder(getExpectedTypesIsSupersetOf(numberType)));
                assertThat(numberType.toString(),
                    rangesNumberTypeFullySupports, containsInAnyOrder(getExpectedTypesItCanFullyHandle(numberType)));
            });
        }

        @Test
        void shouldCompareValuesToShortRange() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Double.NEGATIVE_INFINITY), equalTo(ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(-4400000L), equalTo(ValueRangeComparison.BELOW_MINIMUM));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Short.MIN_VALUE - 1), equalTo(ValueRangeComparison.BELOW_MINIMUM));

            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Short.MIN_VALUE), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange((short) -31894), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(0L), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(new BigDecimal("12223.45")), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Short.MAX_VALUE), equalTo(ValueRangeComparison.WITHIN_RANGE));

            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Short.MAX_VALUE + 1), equalTo(ValueRangeComparison.ABOVE_MAXIMUM));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(33333.0d), equalTo(ValueRangeComparison.ABOVE_MAXIMUM));
            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Float.POSITIVE_INFINITY), equalTo(ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY));

            assertThat(StandardNumberTypeEnum.T_SHORT.compareToValueRange(Double.NaN), equalTo(ValueRangeComparison.UNSUPPORTED_NAN));
        }

        @Test
        void shouldCompareValuesToFloatRange() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(Double.NEGATIVE_INFINITY), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(Double.POSITIVE_INFINITY), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(Float.POSITIVE_INFINITY), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(Double.NaN), equalTo(ValueRangeComparison.WITHIN_RANGE));

            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(new BigInteger("-999999999999999999999999999999999999999999")), equalTo(ValueRangeComparison.BELOW_MINIMUM));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(-Double.MAX_VALUE), equalTo(ValueRangeComparison.BELOW_MINIMUM));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(-3.4028237E38), equalTo(ValueRangeComparison.BELOW_MINIMUM));

            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange((double) Float.MAX_VALUE), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(444_666_888), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(new BigDecimal("444298347982347.0123456789")), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(Float.MAX_VALUE), equalTo(ValueRangeComparison.WITHIN_RANGE));

            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(3.4028237E38), equalTo(ValueRangeComparison.ABOVE_MAXIMUM));
            assertThat(StandardNumberTypeEnum.T_FLOAT.compareToValueRange(new BigDecimal("999999999999999999999999999999999999999999.243240")), equalTo(ValueRangeComparison.ABOVE_MAXIMUM));
        }

        @Test
        void shouldCompareValuesToBigIntegerRange() {
            // given / when / then
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(new BigDecimal("-99999999999999999999999999999999999E1000")), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(-4400000L), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange((byte) -27), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(0L), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(Double.MAX_VALUE), equalTo(ValueRangeComparison.WITHIN_RANGE));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(new BigDecimal("9999999999999999999999999999999999999999999E234").toBigInteger()), equalTo(ValueRangeComparison.WITHIN_RANGE));

            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(Double.NEGATIVE_INFINITY), equalTo(ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(Float.POSITIVE_INFINITY), equalTo(ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(Float.NaN), equalTo(ValueRangeComparison.UNSUPPORTED_NAN));
            assertThat(StandardNumberTypeEnum.T_BIG_INTEGER.compareToValueRange(Double.NaN), equalTo(ValueRangeComparison.UNSUPPORTED_NAN));
        }

        private EnumSet<StandardNumberTypeEnum> getExpectedFullySupportedTypes(StandardNumberTypeEnum type) {
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

        private StandardNumberTypeEnum[] getExpectedTypesIsSupersetOf(StandardNumberTypeEnum numberType) {
            switch (numberType) {
                case BYTE:
                    return new StandardNumberTypeEnum[]{ BYTE };
                case SHORT:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT };
                case INTEGER:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER };
                case LONG:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG };
                case FLOAT:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG, FLOAT };
                case DOUBLE:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE };
                case BIG_INTEGER:
                case BIG_DECIMAL:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BIG_INTEGER, BIG_DECIMAL };
                default:
                    throw new IllegalStateException("Unhandled value: " + numberType);
            }
        }

        private StandardNumberTypeEnum[] getExpectedTypesItCanFullyHandle(StandardNumberTypeEnum numberType) {
            switch (numberType) {
                case BYTE:
                    return new StandardNumberTypeEnum[]{ BYTE };
                case SHORT:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT };
                case INTEGER:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER };
                case LONG:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG };
                case FLOAT:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG, FLOAT };
                case DOUBLE:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE };
                case BIG_INTEGER:
                case BIG_DECIMAL:
                    return new StandardNumberTypeEnum[]{ BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL };
                default:
                    throw new IllegalStateException("Unhandled value: " + numberType);
            }
        }
    }

    /**
     * Tests {@link StandardNumberTypeEnum#convertUnsafe},
     * {@link StandardNumberTypeEnum#convertToBounds},
     * {@link StandardNumberTypeEnum#convertIfNoLossOfMagnitude}.
     */
    @Nested
    class ConvertTests {

        @Test
        void shouldConvertToByte() {
            // given
            Map<Number, Byte> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, Byte.MIN_VALUE);
            expectations.put(Byte.MAX_VALUE, Byte.MAX_VALUE);

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_BYTE, expectations);
        }

        @Test
        void shouldConvertToShort() {
            // given
            Map<Number, Short> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, (short) -128);
            expectations.put(Byte.MAX_VALUE, (short) 127);
            expectations.put(Short.MIN_VALUE, Short.MIN_VALUE);
            expectations.put((short) 15000, (short) 15000);
            expectations.put(Short.MAX_VALUE, Short.MAX_VALUE);
            expectations.put(404L, (short) 404);
            expectations.put(-3323.45f, (short) -3323);
            expectations.put(new BigDecimal("32767"), (short) 32767);

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_SHORT, expectations);
        }

        @Test
        void shouldConvertToInteger() {
            // given
            Map<Number, Integer> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, -128);
            expectations.put(Byte.MAX_VALUE, 127);
            expectations.put(Short.MIN_VALUE, (int) Short.MIN_VALUE);
            expectations.put((short) 15000, 15000);
            expectations.put(Short.MAX_VALUE, (int) Short.MAX_VALUE);
            expectations.put(Integer.MIN_VALUE, Integer.MIN_VALUE);
            expectations.put(Integer.MAX_VALUE, Integer.MAX_VALUE);
            expectations.put(404L, 404);
            expectations.put(-3323.45f, -3323);
            expectations.put(222222222d, 222222222);
            expectations.put(BigInteger.valueOf(1234567890), 1234567890);
            expectations.put(new BigDecimal("32767"), 32767);

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_INTEGER, expectations);
        }

        @Test
        void shouldConvertToLong() {
            // given
            Map<Number, Long> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, -128L);
            expectations.put(Byte.MAX_VALUE, 127L);
            expectations.put(Short.MIN_VALUE, (long) Short.MIN_VALUE);
            expectations.put((short) 15000, 15000L);
            expectations.put(Short.MAX_VALUE, (long) Short.MAX_VALUE);
            expectations.put(Integer.MIN_VALUE, (long) Integer.MIN_VALUE);
            expectations.put(Integer.MAX_VALUE, (long) Integer.MAX_VALUE);
            expectations.put(Long.MIN_VALUE, Long.MIN_VALUE);
            expectations.put(404L, 404L);
            expectations.put(Long.MAX_VALUE, Long.MAX_VALUE);
            expectations.put(-3323.45f, -3323L);
            expectations.put(222222222d, 222222222L);
            expectations.put(BigInteger.valueOf(1234567890), 1234567890L);
            expectations.put(new BigDecimal("32767"), 32767L);

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_LONG, expectations);
        }

        @Test
        void shouldConvertToFloat() {
            // given
            Map<Number, Float> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, -128f);
            expectations.put(Byte.MAX_VALUE, 127f);
            expectations.put(Short.MIN_VALUE, (float) Short.MIN_VALUE);
            expectations.put((short) 15000, 15000f);
            expectations.put(Short.MAX_VALUE, (float) Short.MAX_VALUE);
            expectations.put(Integer.MIN_VALUE, (float) Integer.MIN_VALUE);
            expectations.put(Integer.MAX_VALUE, (float) Integer.MAX_VALUE);
            expectations.put(Long.MIN_VALUE, (float) Long.MIN_VALUE);
            expectations.put(404L, 404f);
            expectations.put(Long.MAX_VALUE, (float) Long.MAX_VALUE);
            expectations.put(-Float.MAX_VALUE, -Float.MAX_VALUE);
            expectations.put(-3323.45f, -3323.45f);
            expectations.put(Float.MAX_VALUE, Float.MAX_VALUE);
            expectations.put(222222222d, 222222222f);
            expectations.put(BigInteger.valueOf(1234567890), 1234567890f);
            expectations.put(new BigDecimal("32767"), 32767f);

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_FLOAT, expectations);
        }

        @Test
        void shouldConvertToDouble() {
            // given
            Map<Number, Double> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, -128d);
            expectations.put(Byte.MAX_VALUE, 127d);
            expectations.put(Short.MIN_VALUE, (double) Short.MIN_VALUE);
            expectations.put((short) 15000, 15000d);
            expectations.put(Short.MAX_VALUE, (double) Short.MAX_VALUE);
            expectations.put(Integer.MIN_VALUE, (double) Integer.MIN_VALUE);
            expectations.put(Integer.MAX_VALUE, (double) Integer.MAX_VALUE);
            expectations.put(Long.MIN_VALUE, (double) Long.MIN_VALUE);
            expectations.put(404L, 404d);
            expectations.put(Long.MAX_VALUE, (double) Long.MAX_VALUE);
            expectations.put(-Float.MAX_VALUE, (double) -Float.MAX_VALUE);
            expectations.put(-3323.45f, (double) -3323.45f);
            expectations.put(Float.MAX_VALUE, (double) Float.MAX_VALUE);
            expectations.put(-Double.MAX_VALUE, -Double.MAX_VALUE);
            expectations.put(222222222d, 222222222d);
            expectations.put(Double.MAX_VALUE, Double.MAX_VALUE);
            expectations.put(BigInteger.valueOf(1234567890), 1234567890d);
            expectations.put(new BigDecimal("32767"), 32767d);

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_DOUBLE, expectations);
        }

        @Test
        void shouldConvertToBigInteger() {
            // given
            Map<Number, BigInteger> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, BigInteger.valueOf(-128));
            expectations.put(Byte.MAX_VALUE, BigInteger.valueOf(127));
            expectations.put(Short.MIN_VALUE, BigInteger.valueOf(Short.MIN_VALUE));
            expectations.put((short) 15000, BigInteger.valueOf(15000));
            expectations.put(Short.MAX_VALUE, BigInteger.valueOf(Short.MAX_VALUE));
            expectations.put(Integer.MIN_VALUE, BigInteger.valueOf(Integer.MIN_VALUE));
            expectations.put(Integer.MAX_VALUE, BigInteger.valueOf(Integer.MAX_VALUE));
            expectations.put(Long.MIN_VALUE, BigInteger.valueOf(Long.MIN_VALUE));
            expectations.put(404L, BigInteger.valueOf(404));
            expectations.put(Long.MAX_VALUE, BigInteger.valueOf(Long.MAX_VALUE));
            expectations.put(-Float.MAX_VALUE, BigDecimal.valueOf(-Float.MAX_VALUE).toBigInteger());
            expectations.put(-3323.45f, BigInteger.valueOf(-3323));
            expectations.put(Float.MAX_VALUE, BigDecimal.valueOf(Float.MAX_VALUE).toBigInteger());
            expectations.put(-Double.MAX_VALUE, BigDecimal.valueOf(-Double.MAX_VALUE).toBigInteger());
            expectations.put(222222222d, BigInteger.valueOf(222222222));
            expectations.put(Double.MAX_VALUE, BigDecimal.valueOf(Double.MAX_VALUE).toBigInteger());
            expectations.put(new BigDecimal("-1E309").toBigInteger(), new BigDecimal("-1E309").toBigInteger());
            expectations.put(BigInteger.valueOf(1234567890), BigInteger.valueOf(1234567890));
            expectations.put(new BigDecimal("1.5678E312").toBigInteger(), new BigDecimal("1.5678E312").toBigInteger());
            expectations.put(new BigDecimal("-3E320"), new BigDecimal("-3E320").toBigInteger());
            expectations.put(new BigDecimal("32767"), BigInteger.valueOf(32767));
            expectations.put(new BigDecimal("9.414141414E308"), new BigDecimal("9.414141414E308").toBigInteger());

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_BIG_INTEGER, expectations);
        }

        @Test
        void shouldConvertToBigDecimal() {
            // given
            Map<Number, BigDecimal> expectations = new HashMap<>();
            expectations.put(Byte.MIN_VALUE, BigDecimal.valueOf(-128));
            expectations.put(Byte.MAX_VALUE, BigDecimal.valueOf(127));
            expectations.put(Short.MIN_VALUE, BigDecimal.valueOf(Short.MIN_VALUE));
            expectations.put((short) 15000, BigDecimal.valueOf(15000));
            expectations.put(Short.MAX_VALUE, BigDecimal.valueOf(Short.MAX_VALUE));
            expectations.put(Integer.MIN_VALUE, BigDecimal.valueOf(Integer.MIN_VALUE));
            expectations.put(Integer.MAX_VALUE, BigDecimal.valueOf(Integer.MAX_VALUE));
            expectations.put(Long.MIN_VALUE, BigDecimal.valueOf(Long.MIN_VALUE));
            expectations.put(404L, BigDecimal.valueOf(404));
            expectations.put(Long.MAX_VALUE, BigDecimal.valueOf(Long.MAX_VALUE));
            expectations.put(-Float.MAX_VALUE, BigDecimal.valueOf(-Float.MAX_VALUE));
            expectations.put(-3323.45f, BigDecimal.valueOf(-3323.45f));
            expectations.put(Float.MAX_VALUE, BigDecimal.valueOf(Float.MAX_VALUE));
            expectations.put(-Double.MAX_VALUE, BigDecimal.valueOf(-Double.MAX_VALUE));
            expectations.put(222222222d, BigDecimal.valueOf(222222222));
            expectations.put(Double.MAX_VALUE, BigDecimal.valueOf(Double.MAX_VALUE));
            expectations.put(new BigDecimal("-1E309").toBigInteger(), new BigDecimal("-1E309").setScale(0));
            expectations.put(BigInteger.valueOf(1234567890), BigDecimal.valueOf(1234567890));
            expectations.put(new BigDecimal("1.5678E312").toBigInteger(), new BigDecimal("1.5678E312").setScale(0));
            expectations.put(new BigDecimal("-3E320"), new BigDecimal("-3E320"));
            expectations.put(new BigDecimal("32767"), BigDecimal.valueOf(32767));
            expectations.put(new BigDecimal("9.414141414E308"), new BigDecimal("9.414141414E308"));

            // when / then
            verifyConversions(StandardNumberTypeEnum.T_BIG_DECIMAL, expectations);
        }

        @Test
        void shouldHandleNaN() {
            // given
            float fNaN = Float.NaN;
            double dNaN = Double.NaN;

            // when / then
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BYTE, fNaN, (byte) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BYTE, dNaN, (byte) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_SHORT, fNaN, (short) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_SHORT, dNaN, (short) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_INTEGER, fNaN, 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_INTEGER, dNaN, 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_LONG, fNaN, 0L, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_LONG, dNaN, 0L, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_FLOAT, fNaN, fNaN, Optional.of(fNaN));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_FLOAT, dNaN, fNaN, Optional.of(fNaN));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_DOUBLE, fNaN, dNaN, Optional.of(dNaN));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_DOUBLE, dNaN, dNaN, Optional.of(dNaN));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_INTEGER, fNaN, BigInteger.ZERO, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_INTEGER, dNaN, BigInteger.ZERO, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_DECIMAL, fNaN, BigDecimal.ZERO, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_DECIMAL, dNaN, BigDecimal.ZERO, Optional.empty());
        }

        @Test
        void shouldHandleInfinities() {
            // given
            float fPosInf = Float.POSITIVE_INFINITY;
            float fNegInf = Float.NEGATIVE_INFINITY;
            double dPosInf = Double.POSITIVE_INFINITY;
            double dNegInf = Double.NEGATIVE_INFINITY;

            // when / then
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BYTE, fPosInf, (byte) -1, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BYTE, fNegInf, (byte) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BYTE, dPosInf, (byte) -1, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BYTE, dNegInf, (byte) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_SHORT, fPosInf, (short) -1, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_SHORT, fNegInf, (short) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_SHORT, dPosInf, (short) -1, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_SHORT, dNegInf, (short) 0, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_INTEGER, fPosInf, Integer.MAX_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_INTEGER, fNegInf, Integer.MIN_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_INTEGER, dPosInf, Integer.MAX_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_INTEGER, dNegInf, Integer.MIN_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_LONG, fPosInf, Long.MAX_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_LONG, fNegInf, Long.MIN_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_LONG, dPosInf, Long.MAX_VALUE, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_LONG, dNegInf, Long.MIN_VALUE, Optional.empty());

            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_FLOAT, fPosInf, fPosInf, Optional.of(fPosInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_FLOAT, fNegInf, fNegInf, Optional.of(fNegInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_FLOAT, dPosInf, fPosInf, Optional.of(fPosInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_FLOAT, dNegInf, fNegInf, Optional.of(fNegInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_DOUBLE, fPosInf, dPosInf, Optional.of(dPosInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_DOUBLE, fNegInf, dNegInf, Optional.of(dNegInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_DOUBLE, dPosInf, dPosInf, Optional.of(dPosInf));
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_DOUBLE, dNegInf, dNegInf, Optional.of(dNegInf));

            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_INTEGER, fPosInf, BigInteger.ZERO, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_INTEGER, fNegInf, BigInteger.ZERO, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_DECIMAL, fPosInf, BigDecimal.ZERO, Optional.empty());
            assertSafeAndUnsafeConversions(StandardNumberTypeEnum.T_BIG_DECIMAL, fNegInf, BigDecimal.ZERO, Optional.empty());
        }

        @Test
        void shouldConvertIfNoLossOfMagnitude() {
            // given
            byte b = (byte) 38;
            long l = 1_234_567_890;
            double d = 9777777777777778000000000000000000000000d;
            BigDecimal bd = new BigDecimal("-32768.5");

            // when / then
            assertThat(SHORT.convertIfNoLossOfMagnitude(b).get(), equalTo((short) 38));
            assertThat(SHORT.convertIfNoLossOfMagnitude(l).isPresent(), equalTo(false));
            assertThat(SHORT.convertIfNoLossOfMagnitude(d).isPresent(), equalTo(false));
            assertThat(SHORT.convertIfNoLossOfMagnitude(bd).get(), equalTo((short) -32768));

            assertThat(INTEGER.convertIfNoLossOfMagnitude(b).get(), equalTo(38));
            assertThat(INTEGER.convertIfNoLossOfMagnitude(l).get(), equalTo(1_234_567_890));
            assertThat(INTEGER.convertIfNoLossOfMagnitude(d).isPresent(), equalTo(false));
            assertThat(INTEGER.convertIfNoLossOfMagnitude(bd).get(), equalTo(-32768));

            assertThat(FLOAT.convertIfNoLossOfMagnitude(b).get(), equalTo(38f));
            assertThat(FLOAT.convertIfNoLossOfMagnitude(l).get(), equalTo(1_234_567_890f));
            assertThat(FLOAT.convertIfNoLossOfMagnitude(d).isPresent(), equalTo(false));
            assertThat(FLOAT.convertIfNoLossOfMagnitude(bd).get(), equalTo(-32768.5f));

            assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(b).get(), equalTo(BigInteger.valueOf(38)));
            assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(l).get(), equalTo(BigInteger.valueOf(1_234_567_890)));
            assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(d).get(), equalTo(new BigInteger("9777777777777778000000000000000000000000")));
            assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(bd).get(), equalTo(BigInteger.valueOf(-32768)));
        }

        @Test
        void shouldConvertFromOtherNumberTypes() {
            // given
            BigDecimal bdExt = new BigDecimal("32789.890") { };
            BigInteger biExt = new BigInteger("9223372036854775807") { };

            DoubleAdder dblAdder = new DoubleAdder();
            dblAdder.add(-200.45);

            LongAdder longAdder = new LongAdder() { };
            longAdder.add(32500);

            AtomicInteger atomicInt = new AtomicInteger(40) { };
            AtomicLong atomicLong = new AtomicLong(-239485723);

            // when / then
            assertThat(BYTE.convertIfNoLossOfMagnitude(bdExt), equalTo(Optional.empty()));
            assertThat(BYTE.convertToBounds(biExt), equalTo(Byte.MAX_VALUE));
            assertThat(BYTE.convertToBounds(dblAdder), equalTo(Byte.MIN_VALUE));
            assertThat(BYTE.convertUnsafe(longAdder), equalTo((byte) -12));
            assertThat(BYTE.convertIfNoLossOfMagnitude(atomicInt).get(), equalTo((byte) 40));
            assertThat(BYTE.convertIfNoLossOfMagnitude(atomicLong), equalTo(Optional.empty()));

            assertThat(SHORT.convertIfNoLossOfMagnitude(bdExt), equalTo(Optional.empty()));
            assertThat(SHORT.convertIfNoLossOfMagnitude(biExt), equalTo(Optional.empty()));
            assertThat(SHORT.convertToBounds(dblAdder), equalTo((short) -200));
            assertThat(SHORT.convertToBounds(longAdder), equalTo((short) 32500));
            assertThat(SHORT.convertUnsafe(atomicInt), equalTo((short) 40));
            assertThat(SHORT.convertUnsafe(atomicLong), equalTo((short) -17179));

            assertThat(LONG.convertUnsafe(bdExt), equalTo(32789L));
            assertThat(LONG.convertToBounds(biExt), equalTo(9223372036854775807L));
            assertThat(LONG.convertIfNoLossOfMagnitude(dblAdder), equalTo(Optional.of(-200L)));
            assertThat(LONG.convertUnsafe(longAdder), equalTo(32500L));
            assertThat(LONG.convertToBounds(atomicInt), equalTo(40L));
            assertThat(LONG.convertIfNoLossOfMagnitude(atomicLong), equalTo(Optional.of(-239485723L)));

            assertThat(DOUBLE.convertToBounds(bdExt), equalTo(32789.890));
            assertThat(DOUBLE.convertUnsafe(biExt), equalTo(9223372036854775807d));
            assertThat(DOUBLE.convertIfNoLossOfMagnitude(dblAdder), equalTo(Optional.of(-200.45)));
            assertThat(DOUBLE.convertIfNoLossOfMagnitude(longAdder), equalTo(Optional.of(32500d)));
            assertThat(DOUBLE.convertUnsafe(atomicInt), equalTo(40d));
            assertThat(DOUBLE.convertToBounds(atomicLong), equalTo(-239485723d));

            assertThat(BIG_INTEGER.convertToBounds(bdExt), equalTo(new BigInteger("32789")));
            assertThat(BIG_INTEGER.convertToBounds(biExt), equalTo(new BigInteger("9223372036854775807")));
            assertThat(BIG_INTEGER.convertUnsafe(dblAdder), equalTo(new BigInteger("-200")));
            assertThat(BIG_INTEGER.convertUnsafe(longAdder), equalTo(new BigInteger("32500")));
            assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(atomicInt).get(), equalTo(new BigInteger("40")));
            assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(atomicLong).get(), equalTo(new BigInteger("-239485723")));
        }

        @Test
        void shouldHandleNonFiniteValuesFromOtherNumberTypes() {
            // given
            DoubleAdder dblAdderNan = new DoubleAdder();
            dblAdderNan.add(Double.NaN);

            DoubleAccumulator dblAccPosInf = new DoubleAccumulator((a, b) -> a * b, 1.0) { };
            dblAccPosInf.accumulate(Double.POSITIVE_INFINITY);

            DoubleAdder dblAdderNegInf = new DoubleAdder() { };
            dblAdderNegInf.add(Double.NEGATIVE_INFINITY);

            // when / then
            assertThat(INTEGER.convertToBounds(dblAdderNan), equalTo(0));
            assertThat(INTEGER.convertToBounds(dblAccPosInf), equalTo(Integer.MAX_VALUE));
            assertThat(INTEGER.convertToBounds(dblAdderNegInf), equalTo(Integer.MIN_VALUE));

            assertThat(LONG.convertIfNoLossOfMagnitude(dblAdderNan), equalTo(Optional.empty()));
            assertThat(LONG.convertIfNoLossOfMagnitude(dblAccPosInf), equalTo(Optional.empty()));
            assertThat(LONG.convertIfNoLossOfMagnitude(dblAdderNegInf), equalTo(Optional.empty()));

            assertThat(LONG.convertUnsafe(dblAdderNan), equalTo(0L));
            assertThat(LONG.convertUnsafe(dblAccPosInf), equalTo(Long.MAX_VALUE));
            assertThat(LONG.convertUnsafe(dblAdderNegInf), equalTo(Long.MIN_VALUE));

            assertThat(FLOAT.convertToBounds(dblAdderNan), equalTo(Float.NaN));
            assertThat(FLOAT.convertToBounds(dblAccPosInf), equalTo(Float.POSITIVE_INFINITY));
            assertThat(FLOAT.convertToBounds(dblAdderNegInf), equalTo(Float.NEGATIVE_INFINITY));

            assertThat(BIG_DECIMAL.convertToBounds(dblAdderNan), equalTo(BigDecimal.ZERO));
            assertThat(BIG_DECIMAL.convertToBounds(dblAccPosInf), equalTo(BigDecimal.ZERO));
            assertThat(BIG_DECIMAL.convertToBounds(dblAdderNegInf), equalTo(BigDecimal.ZERO));
        }

        private <N extends Number> void verifyConversions(NumberType<N> numberType, Map<Number, N> expectations) {
            for (Number number : getNumbersToConvertFrom()) {
                N expectation = expectations.get(number);

                N unsafeConvertResult = numberType.convertUnsafe(number);
                N toBoundsResult = numberType.convertToBounds(number);
                Optional<N> safeConvertResult = numberType.convertIfNoLossOfMagnitude(number);

                boolean unsafeMatches;
                boolean boundMatches;
                boolean safeMatches;
                if (expectation != null) {
                    unsafeMatches = expectation.equals(unsafeConvertResult);
                    boundMatches = expectation.equals(toBoundsResult);
                    safeMatches = Optional.of(expectation).equals(safeConvertResult);
                } else {
                    unsafeMatches = numberType.getType().isInstance(unsafeConvertResult);
                    BigDecimal toBoundsAsBigDecimal = StandardNumberTypeEnum.T_BIG_DECIMAL.convertUnsafe(toBoundsResult);
                    boundMatches = toBoundsAsBigDecimal.compareTo(numberType.getValueRange().getMinValue()) == 0
                        || toBoundsAsBigDecimal.compareTo(numberType.getValueRange().getMaxValue()) == 0;
                    safeMatches = !safeConvertResult.isPresent();
                }

                if (!unsafeMatches || !boundMatches || !safeMatches) {
                    String safeResultString = safeConvertResult
                        .map(n -> "Optional[" + n.toString() + " (class=" + n.getClass().getSimpleName() + ")]")
                        .orElse("Optional.empty");
                    fail("For number '" + number + "' (type " + number.getClass().getSimpleName()
                        + "), expected safe conversion = " + expectation + ", but got " + safeResultString
                        + ". Unsafe result match=" + unsafeMatches + ", within bounds match = " + toBoundsResult + ", safe result match = " + safeMatches);
                }
            }
        }

        private void assertSafeAndUnsafeConversions(NumberType<?> numberType,
                                                    Number numberToConvert,
                                                    Number expectedUnsafeConversion,
                                                    Optional<Number> expectedSafeConversion) {
            assertThat(numberType.convertUnsafe(numberToConvert), equalTo(expectedUnsafeConversion));
            assertThat(numberType.convertIfNoLossOfMagnitude(numberToConvert), equalTo(expectedSafeConversion));
        }

        private List<Number> getNumbersToConvertFrom() {
            return Arrays.asList(
                Byte.MIN_VALUE, // -128
                Byte.MAX_VALUE, // 127
                Short.MIN_VALUE, // -32768
                (short) 15000,
                Short.MAX_VALUE, // 32767
                Integer.MIN_VALUE, // -2147483648
                Integer.MAX_VALUE, // 2147483647
                Long.MIN_VALUE,
                404L,
                Long.MAX_VALUE,
                -Float.MAX_VALUE,
                -3323.45f,
                Float.MAX_VALUE,
                -Double.MAX_VALUE,
                222222222d,
                Double.MAX_VALUE, // 1.7976931348623157e+308
                new BigDecimal("-1E309").toBigInteger(),
                BigInteger.valueOf(1234567890),
                new BigDecimal("1.5678E312").toBigInteger(),
                new BigDecimal("-3E320"),
                new BigDecimal("32767"), // short max value as BigDecimal
                new BigDecimal("9.414141414E308")
            );
        }
    }

    @Test
    void shouldThrowForUnknownNumberType() {
        // given
        Number nbr = new NumberTestImpl();

        // when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> BYTE.convertToBounds(nbr));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> FLOAT.convertIfNoLossOfMagnitude(nbr));
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> BIG_INTEGER.compareToValueRange(nbr));
        // Only BigDecimal and BigInteger throw an exception on #convertUnsafe
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () -> BIG_DECIMAL.convertUnsafe(nbr));

        // then
        Set<String> messages = Stream.of(ex1, ex2, ex3, ex4).map(Exception::getMessage).collect(Collectors.toSet());
        assertThat(messages, contains("Unsupported number type: class ch.jalu.typeresolver.numbers.NumberTestImpl"));
    }
}