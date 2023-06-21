package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.primitives.Primitives;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public final class NumberTypes {

    private static final Map<Class<?>, NumberType<?>> NUMBER_TYPES_BY_CLASS = collectAllNumberTypesByClass();

    private NumberTypes() {
    }

    @Nullable
    public static <T> NumberType<T> from(@Nullable Class<T> clazz) {
        return (NumberType<T>) NUMBER_TYPES_BY_CLASS.get(clazz);
    }

    public static Stream<NumberType<?>> streamThroughAll() {
        return Stream.concat(
            StandardNumberType.streamThroughAll(),
            Stream.of(MoreNumberTypes.CHARACTER, MoreNumberTypes.ATOMIC_INTEGER, MoreNumberTypes.ATOMIC_LONG));
    }

    /**
     * Unwraps the object to a basic number type and returns the value if the given object is a {@code Number} or
     * {@code Character}.
     * <p>
     * Specifically, this method converts {@link Character} to an int, and it unwraps the number types
     * {@link AtomicInteger}, {@link AtomicLong}, {@link LongAccumulator}, {@link LongAdder}, {@link DoubleAccumulator}
     * and {@link DoubleAdder} to their respective underlying type.
     *
     * @param object the object to unwrap
     * @return the number value the object could be unwrapped to
     */
    @Nullable
    public static Number unwrapToStandardNumberType(@Nullable Object object) {
        if (object instanceof Character) {
            return (int) (Character) object;
        } else if (object instanceof Number) {
            return unwrapToStandardNumberType((Number) object);
        }
        return null;
    }

    /**
     * Unwraps the number to a basic number type, if applicable.
     * <p>
     * Specifically, this method unwraps the number types {@link AtomicInteger}, {@link AtomicLong},
     * {@link LongAccumulator}, {@link LongAdder}, {@link DoubleAccumulator} and {@link DoubleAdder} to their respective
     * underlying type.
     *
     * @param number the object to unwrap
     * @return the number value the object could be unwrapped to
     */
    public static Number unwrapToStandardNumberType(Number number) {
        if (number instanceof AtomicInteger) {
            return number.intValue();
        } else if (number instanceof AtomicLong || number instanceof LongAccumulator
                   || number instanceof LongAdder) {
            return number.longValue();
        } else if (number instanceof DoubleAccumulator || number instanceof DoubleAdder) {
            return number.doubleValue();
        }
        return number;
    }

    private static Map<Class<?>, NumberType<?>> collectAllNumberTypesByClass() {
        Map<Class<?>, NumberType<?>> numberTypeByClass = new HashMap<>();
        streamThroughAll().forEach(numberType -> {
            numberTypeByClass.put(numberType.getType(), numberType);
            Class<?> primitiveType = Primitives.toPrimitiveType(numberType.getType());
            numberTypeByClass.put(primitiveType, numberType);
        });
        return Collections.unmodifiableMap(numberTypeByClass);
    }
}
