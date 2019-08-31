package ch.jalu.typeresolver;

import ch.jalu.typeresolver.samples.typeinheritance.AbstractTwoArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessor;
import ch.jalu.typeresolver.samples.typeinheritance.IntegerDoubleArgProcessorExtension;
import ch.jalu.typeresolver.samples.typeinheritance.OneArgProcessor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}