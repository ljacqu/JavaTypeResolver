package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.primitives.Primitives;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class StandardNumberType<N extends Number> implements NumberType<N> {

    /** Byte: [-128, 127]. */
    public static final StandardNumberType<Byte> BYTE = new StandardNumberType<>(Byte.class,
        NonDecimalNumberRange.BYTE);

    /** Short: [-32768, 32767]. */
    public static final StandardNumberType<Short> SHORT = new StandardNumberType<>(Short.class,
        NonDecimalNumberRange.SHORT);

    /** Integer: [-2147483648, 2147483647]. */
    public static final StandardNumberType<Integer> INTEGER = new StandardNumberType<>(Integer.class,
        NonDecimalNumberRange.INTEGER);

    /** Long: [-9223372036854775808, 9223372036854775807]. */
    public static final StandardNumberType<Long> LONG = new StandardNumberType<>(Long.class,
        NonDecimalNumberRange.LONG);

    /** Float. */
    public static final StandardNumberType<Float> FLOAT = new StandardNumberType<>(Float.class,
        DecimalNumberRange.FLOAT);

    /** Double. */
    public static final StandardNumberType<Double> DOUBLE = new StandardNumberType<>(Double.class,
        DecimalNumberRange.DOUBLE);

    /** Big integer: integer with a theoretically infinite range of supported values. */
    public static final StandardNumberType<BigInteger> BIG_INTEGER = new StandardNumberType<>(BigInteger.class,
        InfiniteNumberRange.BIG_INTEGER);

    /** Big decimal: supports decimals and has a theoretically infinite range of supported values. */
    public static final StandardNumberType<BigDecimal> BIG_DECIMAL = new StandardNumberType<>(BigDecimal.class,
        InfiniteNumberRange.BIG_DECIMAL);


    private static final Map<Class<?>, StandardNumberType<?>> REFERENCE_TYPE_TO_NUMBER_TYPE =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<N> type;
    private final ConvertingValueRange valueRange;

    public StandardNumberType(Class<N> type, ConvertingValueRange valueRange) {
        this.type = type;
        this.valueRange = valueRange;
    }

    @Override
    public Class<N> getType() {
        return type;
    }

    @Override
    public N convertUnsafe(Number number) {
        return (N) valueRange.convertUnsafe(number);
    }

    @Override
    public Optional<N> convertIfNoLossOfMagnitude(Number number) {
        return (Optional) valueRange.convertIfNoLossOfMagnitude(number);
    }

    @Override
    public N convertToBounds(Number number) {
        return (N) valueRange.convertToBounds(number);
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
        return Stream.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BIG_INTEGER, BIG_DECIMAL);
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
        return Collections.unmodifiableMap(referenceTypeToNumberType);
    }
}
