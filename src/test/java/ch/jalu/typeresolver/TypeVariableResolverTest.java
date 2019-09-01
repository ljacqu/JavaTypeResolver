package ch.jalu.typeresolver;

import ch.jalu.typeresolver.samples.typeinheritance.AbstractTwoArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessorExtension;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerGenericArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.OneArgProcessor;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link TypeVariableResolver}.
 */
class TypeVariableResolverTest {

    @Test
    void shouldResolveTypeVariables() throws NoSuchMethodException, NoSuchFieldException {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessorExtension.class);

        Method processMethod = OneArgProcessor.class.getDeclaredMethod("process", Object.class);
        Field tuMap = AbstractTwoArgProcessor.class.getDeclaredField("tuMap");

        // when
        Type processArgMethodType = resolver.resolve(processMethod.getGenericParameterTypes()[0]);
        TypeInfo resolvedTuMapTypeInfo = new TypeInfo(resolver.resolve(tuMap.getGenericType()));

        // then
        assertEquals(processArgMethodType, BigDecimal.class);
        assertEquals(resolvedTuMapTypeInfo.toClass(), Map.class);
        assertEquals(resolvedTuMapTypeInfo.getGenericTypeAsClass(0), Integer.class);
        assertEquals(resolvedTuMapTypeInfo.getGenericTypeAsClass(1), Double.class);
    }

    @Test
    void shouldReturnWildcardTypeIfItCannotBeResolved() throws NoSuchFieldException {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);
        Field optionalValueField = Optional.class.getDeclaredField("value");

        // when
        Type resolvedType = resolver.resolve(optionalValueField.getGenericType());

        // then
        assertEquals(resolvedType, optionalValueField.getGenericType());
    }

    @Test
    void shouldReturnArgumentIfItIsAClass() {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);

        // when / then
        assertEquals(resolver.resolve(String.class), String.class);
        assertEquals(resolver.resolve(int.class), int.class);
        assertEquals(resolver.resolve(BigDecimal[][].class), BigDecimal[][].class);
        assertEquals(resolver.resolve(char[].class), char[].class);
        assertNull(resolver.resolve(null));
    }

    @Test
    void shouldResolveTypesWithChildResolvers() throws Exception {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(ClassWithTypes.class);
        TypeVariableResolver intCharProcessorResolver = createChildResolver(resolver, ClassWithTypes.class, "intCharProcessor");
        Type tSetType = IntegerGenericArgProcessor.class.getDeclaredField("tSet").getGenericType();
        Type uListType = AbstractTwoArgProcessor.class.getDeclaredField("uList").getGenericType();
        Type tuMapType = AbstractTwoArgProcessor.class.getDeclaredField("tuMap").getGenericType();

        TypeVariableResolver floatListProcessorsResolver = createChildResolver(resolver, ClassWithTypes.class, "floatListProcessors");
        TypeVariableResolver oneArgProcessorResolver = createChildResolver(floatListProcessorsResolver, ListProcessorContainer.class, "oneArgProcessor");
        Type processParamType = OneArgProcessor.class.getDeclaredMethod("process", Object.class).getGenericParameterTypes()[0];
        TypeVariableResolver twoArgProcessorResolver = createChildResolver(floatListProcessorsResolver, ListProcessorContainer.class, "twoArgProcessor");

        // when
        Type tSetResolved = intCharProcessorResolver.resolve(tSetType);
        Type uListResolved = intCharProcessorResolver.resolve(uListType);
        Type tuMapResolved = intCharProcessorResolver.resolve(tuMapType);
        Type processParamTypeResolved = oneArgProcessorResolver.resolve(processParamType);
        Type tSetResolved2 = twoArgProcessorResolver.resolve(tSetType);
        Type tuMapResolved2 = twoArgProcessorResolver.resolve(tuMapType);

        // then
        assertIsParameterizedType(tSetResolved, Set.class, Character.class);
        assertIsParameterizedType(uListResolved, List.class, Character.class);
        assertIsParameterizedType(tuMapResolved, Map.class, Integer.class, Character.class);
        assertIsParameterizedType(processParamTypeResolved, List.class, Float.class);

        TypeToken<Set<Map<Float, Set<Float>>>> expectedTSetType = new TypeToken<Set<Map<Float, Set<Float>>>>() {};
        assertEquals(tSetResolved2, expectedTSetType.getType());

        TypeToken<Map<Integer, Map<Float, Set<Float>>>> expectedTuMapType = new TypeToken<Map<Integer, Map<Float, Set<Float>>>>() {};
        assertEquals(tuMapResolved2, expectedTuMapType.getType());
    }

    @Test
    void shouldResolveQuestionMarkTypes() throws Exception {
        // given
        TypeVariableResolver resolver = new TypeVariableResolver(IntegerDoubleArgProcessor.class);
        Type optionalType = AbstractTwoArgProcessor.class.getDeclaredField("uExtOptional").getGenericType();
        Type comparableType = AbstractTwoArgProcessor.class.getDeclaredField("tSuperComparable").getGenericType();

        // when
        Type resolvedOptionalType = resolver.resolve(optionalType);
        Type resolvedComparableType = resolver.resolve(comparableType);

        // then
        Type expectedOptionalType = new TypeToken<Optional<? extends Double>>() { }.getType();
        assertEquals(resolvedOptionalType, expectedOptionalType);
        Type expectedComparableType = new TypeToken<Comparable<? super Integer>>() { }.getType();
        assertEquals(resolvedComparableType, expectedComparableType);
    }

    private static TypeVariableResolver createChildResolver(TypeVariableResolver parentResolver,
                                                            Class<?> clazz, String fieldName) {
        try {
            return parentResolver.createChildResolver(clazz.getDeclaredField(fieldName).getGenericType());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(clazz + " - " + fieldName, e);
        }
    }

    private static void assertIsParameterizedType(Type actualType, Class<?> expectedRawType, Class<?>... expectedClasses) {
        assertTrue(actualType instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) actualType;
        assertEquals(expectedRawType, pt.getRawType());
        assertArrayEquals(expectedClasses, pt.getActualTypeArguments());
    }

    private class ClassWithTypes {

        private IntegerGenericArgProcessor<Character> intCharProcessor;
        private ListProcessorContainer<Float> floatListProcessors;

    }

    private class ListProcessorContainer<T> {

        private OneArgProcessor<List<T>> oneArgProcessor;
        private IntegerGenericArgProcessor<Map<T, Set<T>>> twoArgProcessor;

    }
}