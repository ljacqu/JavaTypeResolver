package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.NumberType;
import ch.jalu.typeresolver.primitives.Primitives;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

public final class StandardNumberType<N extends Number> implements NumberType<N> {

    /** Byte: [-128, 127]. */
    public static final StandardNumberType<Byte> BYTE = new StandardNumberType<>(Byte.class,
        NonDecimalNumberRange.BYTE, Number::byteValue);

    /** Short: [-32768, 32767]. */
    public static final StandardNumberType<Short> SHORT = new StandardNumberType<>(Short.class,
        NonDecimalNumberRange.SHORT, Number::shortValue);

    /** Integer: [-2147483648, 2147483647]. */
    public static final StandardNumberType<Integer> INTEGER = new StandardNumberType<>(Integer.class,
        NonDecimalNumberRange.INTEGER, Number::intValue);

    /** Long: [-9223372036854775808, 9223372036854775807]. */
    public static final StandardNumberType<Long> LONG = new StandardNumberType<>(Long.class,
        NonDecimalNumberRange.LONG, Number::longValue);

    /** Float. */
    public static final StandardNumberType<Float> FLOAT = new StandardNumberType<>(Float.class,
        DecimalNumberRange.FLOAT, Number::floatValue);

    /** Double. */
    public static final StandardNumberType<Double> DOUBLE = new StandardNumberType<>(Double.class,
        DecimalNumberRange.DOUBLE, Number::doubleValue);

    /** Big integer: integer with a theoretically infinite range of supported values. */
    public static final StandardNumberType<BigInteger> BIG_INTEGER = new StandardNumberType<>(BigInteger.class,
        InfiniteNumberRange.BIG_INTEGER, num -> InfiniteNumberRange.toBigInteger(num, true));

    /** Big decimal: supports decimals and has a theoretically infinite range of supported values. */
    public static final StandardNumberType<BigDecimal> BIG_DECIMAL = new StandardNumberType<>(BigDecimal.class,
        InfiniteNumberRange.BIG_DECIMAL, num -> InfiniteNumberRange.toBigDecimal(num, true));

    public static final StandardNumberType<AtomicInteger> ATOMIC_INTEGER = new StandardNumberType<>(AtomicInteger.class,
        NonDecimalNumberRange.INTEGER, num -> new AtomicInteger(num.intValue()), true);

    public static final StandardNumberType<AtomicLong> ATOMIC_LONG = new StandardNumberType<>(AtomicLong.class,
        NonDecimalNumberRange.LONG, num -> new AtomicLong(num.longValue()), true);


    private static final Map<Class<?>, StandardNumberType<?>> REFERENCE_TYPE_TO_NUMBER_TYPE =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<N> type;
    private final ConvertingValueRange valueRange;
    private final Function<Number, N> convertUnsafeFn;
    private final Function<Number, Optional<N>> convertSafeFn;

    public StandardNumberType(Class<N> type, ConvertingValueRange valueRange, Function<Number, N> convertUnsafeFn) {
        this.type = type;
        this.valueRange = valueRange;
        this.convertUnsafeFn = convertUnsafeFn;
        this.convertSafeFn = n -> (Optional<N>) valueRange.convertToTypeIfNoLossOfMagnitude(n);
    }

    public StandardNumberType(Class<N> type, ConvertingValueRange valueRange, Function<Number, N> convertUnsafeFn,
                              boolean applyUnsafeFnOnConversionFromValueRange) {
        this.type = type;
        this.valueRange = valueRange;
        this.convertUnsafeFn = convertUnsafeFn;
        this.convertSafeFn = number -> valueRange.convertToTypeIfNoLossOfMagnitude(number).map(convertUnsafeFn);
    }

    @Override
    public Class<N> getType() {
        return type;
    }

    @Override
    public N convertUnsafe(Number number) {
        return convertUnsafeFn.apply(number);
    }

    @Override
    public Optional<N> convertIfNoLossOfMagnitude(Number number) {
        return convertSafeFn.apply(number);
    }

    @Override
    public ValueRange getValueRange() {
        return valueRange;
    }

    @Override
    public String toString() {
        return "NumberType[" + type.getSimpleName() + "]";
    }

    @Nullable
    public Primitives asPrimitiveType() {
        return Primitives.from(type);
    }

    @Nullable
    public static StandardNumberType<?> fromClass(@Nullable Class<?> clazz) {
        Class<?> referenceType = Primitives.toReferenceType(clazz);
        return REFERENCE_TYPE_TO_NUMBER_TYPE.get(referenceType);
    }

    @Nullable
    public static <T extends Number> StandardNumberType<T> fromNumberClass(@Nullable Class<T> clazz) {
        return (StandardNumberType) fromClass(clazz);
    }

    public static Stream<StandardNumberType<?>> streamThroughPrimitiveTypes() {
        return Stream.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE);
    }

    public static Stream<StandardNumberType<?>> streamThroughAll() {
        return Stream.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BIG_INTEGER, BIG_DECIMAL,
            ATOMIC_INTEGER, ATOMIC_LONG);
    }

    private static Map<Class<?>, StandardNumberType<?>> initReferenceTypeToStandardNumberTypeMap() {
        Map<Class<?>, StandardNumberType<?>> referenceTypeToNumberType = new HashMap<>();
        referenceTypeToNumberType.put(Byte.class, BYTE);
        referenceTypeToNumberType.put(Short.class, SHORT);
        referenceTypeToNumberType.put(Integer.class, INTEGER);
        referenceTypeToNumberType.put(Long.class, LONG);
        referenceTypeToNumberType.put(Float.class, FLOAT);
        referenceTypeToNumberType.put(Double.class, DOUBLE);
        referenceTypeToNumberType.put(BigInteger.class, BIG_INTEGER);
        referenceTypeToNumberType.put(BigDecimal.class, BIG_DECIMAL);
        referenceTypeToNumberType.put(AtomicInteger.class, ATOMIC_INTEGER);
        referenceTypeToNumberType.put(AtomicLong.class, ATOMIC_LONG);
        return Collections.unmodifiableMap(referenceTypeToNumberType);
    }
}
