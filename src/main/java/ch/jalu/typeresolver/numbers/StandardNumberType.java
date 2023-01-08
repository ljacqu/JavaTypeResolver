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

/**
 * {@link NumberType} implementations for standard Java number types: byte, short, integer, long, float, double,
 * BigInteger and BigDecimal.
 * <p>
 * If you wish to enumerate over these types, consider using the equivalent enum, {@link StandardNumberTypeEnum}. Using
 * this class is more convenient if you want to use a concrete number type and want to benefit from type safety.
 * You can convert to/from this class and the enum using {@link #asEnum()} and
 * {@link StandardNumberTypeEnum#asNumberType()}.
 *
 * @param <N> the number type
 */
public final class StandardNumberType<N extends Number> implements NumberType<N> {

    /** Byte: [-128, 127]. */
    public static final StandardNumberType<Byte> BYTE =
        new StandardNumberType<>(Byte.class, StandardNumberTypeEnum.BYTE);

    /** Short: [-32768, 32767]. */
    public static final StandardNumberType<Short> SHORT =
        new StandardNumberType<>(Short.class, StandardNumberTypeEnum.SHORT);

    /** Integer: [-2147483648, 2147483647]. */
    public static final StandardNumberType<Integer> INTEGER =
        new StandardNumberType<>(Integer.class, StandardNumberTypeEnum.INTEGER);

    /** Long: [-9223372036854775808, 9223372036854775807]. */
    public static final StandardNumberType<Long> LONG =
        new StandardNumberType<>(Long.class, StandardNumberTypeEnum.LONG);

    /** Float: [-3.4028235E38, 3.4028235E38].  */
    public static final StandardNumberType<Float> FLOAT =
        new StandardNumberType<>(Float.class, StandardNumberTypeEnum.FLOAT);

    /** Double: [-1.7976931348623157E308, 1.7976931348623157E308]. */
    public static final StandardNumberType<Double> DOUBLE =
        new StandardNumberType<>(Double.class, StandardNumberTypeEnum.DOUBLE);

    /** Big integer: integer with a theoretically infinite range of supported values. */
    public static final StandardNumberType<BigInteger> BIG_INTEGER =
        new StandardNumberType<>(BigInteger.class, StandardNumberTypeEnum.BIG_INTEGER);

    /** Big decimal: supports decimals and has a theoretically infinite range of supported values. */
    public static final StandardNumberType<BigDecimal> BIG_DECIMAL =
        new StandardNumberType<>(BigDecimal.class, StandardNumberTypeEnum.BIG_DECIMAL);


    private static final Map<Class<?>, StandardNumberType<?>> REFERENCE_TYPE_TO_NUMBER_TYPE =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<N> type;
    private final StandardNumberTypeEnum enumEntry;

    private StandardNumberType(Class<N> type, StandardNumberTypeEnum enumEntry) {
        this.type = type;
        this.enumEntry = enumEntry;
    }

    @Override
    public Class<N> getType() {
        return type;
    }

    @Override
    public N convertUnsafe(Number number) {
        return (N) enumEntry.convertUnsafe(number);
    }

    @Override
    public Optional<N> convertIfNoLossOfMagnitude(Number number) {
        return (Optional<N>) enumEntry.convertIfNoLossOfMagnitude(number);
    }

    @Override
    public N convertToBounds(Number number) {
        return (N) enumEntry.convertToBounds(number);
    }

    @Override
    public ValueRange<N> getValueRange() {
        return (ValueRange<N>) enumEntry.getValueRange();
    }

    @Override
    public boolean supportsAllValuesOf(NumberType<?> other) {
        if (NumberType.super.supportsAllValuesOf(other)) {
            if (other == DOUBLE || other == FLOAT) {
                return this == DOUBLE || this == FLOAT;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "NumberType[" + type.getSimpleName() + "]";
    }

    @Nullable
    public Primitives asPrimitiveType() {
        return Primitives.from(type);
    }

    public StandardNumberTypeEnum asEnum() {
        return enumEntry;
    }

    /**
     * Returns the instance that corresponds to the given class, or null if not applicable.
     * <p>
     * If you know that the class is a Number and can benefit from type safety, prefer {@link #fromNumberClass}.
     *
     * @param clazz the class to find the number type for
     * @return the instance matching the desired type, or null if not applicable
     * @see NumberTypes#from
     */
    @Nullable
    public static StandardNumberType<?> from(@Nullable Class<?> clazz) {
        return REFERENCE_TYPE_TO_NUMBER_TYPE.get(clazz);
    }

    /**
     * Returns the instance that corresponds to this class, or null if not applicable.
     *
     * @param clazz the class to find the number type for
     * @param <T> the number type
     * @return the instance matching the desired type, or null if not applicable
     * @see NumberTypes#from
     */
    @Nullable
    public static <T extends Number> StandardNumberType<T> fromNumberClass(@Nullable Class<T> clazz) {
        return (StandardNumberType<T>) from(clazz);
    }

    public static Stream<StandardNumberType<?>> streamThroughAll() {
        return Stream.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BIG_INTEGER, BIG_DECIMAL);
    }

    /**
     * Creates a stream of the number type instances that represent one of the six Java number types that
     * are associated with a primitive type: byte, short, int, long, float, and double.
     *
     * @return stream of the number types for the six primitive number types
     */
    public static Stream<StandardNumberType<?>> streamThroughPrimitiveTypes() {
        return Stream.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE);
    }

    private static Map<Class<?>, StandardNumberType<?>> initReferenceTypeToStandardNumberTypeMap() {
        Map<Class<?>, StandardNumberType<?>> referenceTypeToNumberType = new HashMap<>();
        referenceTypeToNumberType.put(byte.class, BYTE);
        referenceTypeToNumberType.put(Byte.class, BYTE);
        referenceTypeToNumberType.put(short.class, SHORT);
        referenceTypeToNumberType.put(Short.class, SHORT);
        referenceTypeToNumberType.put(int.class, INTEGER);
        referenceTypeToNumberType.put(Integer.class, INTEGER);
        referenceTypeToNumberType.put(long.class, LONG);
        referenceTypeToNumberType.put(Long.class, LONG);
        referenceTypeToNumberType.put(float.class, FLOAT);
        referenceTypeToNumberType.put(Float.class, FLOAT);
        referenceTypeToNumberType.put(double.class, DOUBLE);
        referenceTypeToNumberType.put(Double.class, DOUBLE);
        referenceTypeToNumberType.put(BigInteger.class, BIG_INTEGER);
        referenceTypeToNumberType.put(BigDecimal.class, BIG_DECIMAL);
        return Collections.unmodifiableMap(referenceTypeToNumberType);
    }
}
