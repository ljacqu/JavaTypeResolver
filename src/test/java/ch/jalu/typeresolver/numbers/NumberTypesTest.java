package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.classutil.ClassUtils;
import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test for {@link NumberTypes}.
 */
class NumberTypesTest {

    @Test
    void shouldUnwrapToNumberType() {
        // given
        DoubleAccumulator dblAcc = new DoubleAccumulator((a, b) -> a * b, 1.0);
        dblAcc.accumulate(6);
        dblAcc.accumulate(15_000_000_000d); // 90_000_000_000d

        DoubleAdder dblAdd = new DoubleAdder();
        dblAdd.add(200_000_000_000d);
        dblAdd.add(100_000_000_000d); // 300_000_000_000d

        LongAccumulator longAcc = new LongAccumulator((a, b) -> a * b, 100_000_000_000L);
        longAcc.accumulate(4L); // 400_000_000_000L

        LongAdder longAdd = new LongAdder();
        longAdd.add(23_456L);
        longAdd.add(100_000_000_000L); // 100_000_023_456L

        // when / then
        // Note: Using large numbers to ensure we don't perform an accidental conversion to a type with a smaller range
        assertThat(NumberTypes.unwrapToStandardNumberType(new AtomicInteger(2147483645)), equalTo(2147483645));
        assertThat(NumberTypes.unwrapToStandardNumberType(new AtomicLong(3337773337L)), equalTo(3337773337L));

        assertThat(NumberTypes.unwrapToStandardNumberType(dblAcc), equalTo(90_000_000_000d));
        assertThat(NumberTypes.unwrapToStandardNumberType(dblAdd), equalTo(300_000_000_000d));
        assertThat(NumberTypes.unwrapToStandardNumberType(longAcc), equalTo(400_000_000_000L));
        assertThat(NumberTypes.unwrapToStandardNumberType(longAdd), equalTo(100_000_023_456L));

        assertThat(NumberTypes.unwrapToStandardNumberType(19L), equalTo(19L));
        assertThat(NumberTypes.unwrapToStandardNumberType((byte) 5), equalTo((byte) 5));
        assertThat(NumberTypes.unwrapToStandardNumberType(BigDecimal.TEN), equalTo(BigDecimal.TEN));
        assertThat(NumberTypes.unwrapToStandardNumberType(3.1415f), equalTo(3.1415f));

        assertThat(NumberTypes.unwrapToStandardNumberType((Number) null), nullValue());
    }

    @Test
    void shouldUnwrapObjectToStandardNumberType() {
        // given / when / then
        assertThat(NumberTypes.unwrapToStandardNumberType('A'), equalTo(65));
        assertThat(NumberTypes.unwrapToStandardNumberType((Object) 23), equalTo(23));
        assertThat(NumberTypes.unwrapToStandardNumberType((Object) new AtomicLong(6529342345L)), equalTo(6529342345L));

        assertThat(NumberTypes.unwrapToStandardNumberType((Object) null), nullValue());
        assertThat(NumberTypes.unwrapToStandardNumberType("14"), nullValue());
        assertThat(NumberTypes.unwrapToStandardNumberType(false), nullValue());
        assertThat(NumberTypes.unwrapToStandardNumberType(Collections.emptyList()), nullValue());
    }

    /**
     * If this test fails, it means there are new Number classes in the JDK that should be addressed; the Javadoc
     * in {@link NumberType} guarantees that certain methods can deal with any JDK Number type.
     */
    @Test
    void shouldSupportAllJdkNumberTypes() throws IOException {
        // given
        ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());

        // when
        Set<String> numberClasses = classPath.getAllClasses().stream()
            .filter(classInfo -> classInfo.getPackageName().startsWith("java.")
                    && !classInfo.getPackageName().startsWith("java.awt."))
            .map(classInfo -> ClassUtils.loadClassOrThrow(classInfo.getName()))
            .filter(cls -> Number.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers()))
            .map(Class::getName)
            .collect(Collectors.toSet());

        // then
        Set<String> expectedNumberClasses = new HashSet<>(Arrays.asList(
                "java.lang.Byte",
                "java.lang.Short",
                "java.lang.Integer",
                "java.lang.Long",
                "java.lang.Float",
                "java.lang.Double",
                "java.math.BigInteger",
                "java.math.BigDecimal",
                "java.util.concurrent.atomic.AtomicInteger",
                "java.util.concurrent.atomic.AtomicLong",
                "java.util.concurrent.atomic.DoubleAccumulator",
                "java.util.concurrent.atomic.DoubleAdder",
                "java.util.concurrent.atomic.LongAccumulator",
                "java.util.concurrent.atomic.LongAdder"));
        assertThat(numberClasses, equalTo(expectedNumberClasses));
    }

    @Test
    void shouldStreamThroughAllNumberTypeInstances() {
        // given
        Set<NumberType> expectedNumberTypes = new HashSet<>();
        expectedNumberTypes.addAll(EnumSet.allOf(StandardNumberType.class));

        for (Field field : MoreNumberTypes.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && NumberType.class.isAssignableFrom(field.getType()) && !field.isSynthetic()) {
                NumberType<?> value = getStatic(field);
                expectedNumberTypes.add(value);
            }
        }

        // when
        List<NumberType<?>> result = NumberTypes.streamThroughAll()
            .collect(Collectors.toList());

        // then
        Set<NumberType<?>> resultAsSet = new HashSet<>(result);
        assertThat(result, hasSize(resultAsSet.size())); // Verify that there were no duplicates in the stream
        assertThat(resultAsSet, equalTo(expectedNumberTypes));
    }

    @Test
    void shouldReturnNumberTypeForClass() {
        // given / when / then
        assertThat(NumberTypes.from(byte.class), equalTo(StandardNumberType.BYTE));
        assertThat(NumberTypes.from(Byte.class), equalTo(StandardNumberType.BYTE));
        assertThat(NumberTypes.from(long.class), equalTo(StandardNumberType.LONG));
        assertThat(NumberTypes.from(Long.class), equalTo(StandardNumberType.LONG));
        assertThat(NumberTypes.from(char.class), equalTo(MoreNumberTypes.CHARACTER));
        assertThat(NumberTypes.from(Character.class), equalTo(MoreNumberTypes.CHARACTER));
        assertThat(NumberTypes.from(AtomicInteger.class), equalTo(MoreNumberTypes.ATOMIC_INTEGER));
        assertThat(NumberTypes.from(AtomicLong.class), equalTo(MoreNumberTypes.ATOMIC_LONG));

        assertThat(NumberTypes.from(null), nullValue());
        assertThat(NumberTypes.from(Number.class), nullValue());
        assertThat(NumberTypes.from(double[].class), nullValue());
        assertThat(NumberTypes.from(String.class), nullValue());
        assertThat(NumberTypes.from(List.class), nullValue());
    }

    private static <T> T getStatic(Field field) {
        try {
            return (T) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to get field '" + field + "'", e);
        }
    }
}