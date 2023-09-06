package ch.jalu.typeresolver;

import ch.jalu.typeresolver.classutil.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Classes with utils for handling fields.
 */
public final class FieldUtils {

    private FieldUtils() {
    }

    /**
     * Returns a human-friendly representation of a field, e.g. "ArrayList#size".
     *
     * @param field the field to format
     * @return text to reference a field
     */
    public static String formatField(Field field) {
        return ClassUtils.getSemanticName(field.getDeclaringClass()) + "#" + field.getName();
    }

    /**
     * Specifies whether a field is a "regular" (= non-synthetic) instance fields. This method exists for brevity and
     * because it can be easily forgotten to check if a field is synthetic.
     *
     * @param field the field to process
     * @return true if the field is not synthetic and not static, false otherwise
     */
    public static boolean isRegularInstanceField(Field field) {
        return !field.isSynthetic() && !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Returns all fields from the given class and its parents, recursively. The fields of the top-most parent
     * in this class's hierarchy are returned first.
     *
     * @param clazz the class whose fields (incl. its parents' fields) should be returned
     * @return all fields, with top-most parent's fields first, and this class's fields last
     */
    public static List<Field> getFieldsIncludingParents(Class<?> clazz) {
        return getFieldsIncludingParents(clazz, f -> true, true);
    }

    /**
     * Returns all fields from the given class and its parents, recursively, that match the provided filter.
     * The fields of the top-most parent in this class's hierarchy are returned first.
     *
     * @param clazz the class whose fields (incl. its parents' fields) should be returned that match the filter
     * @param fieldFilter the condition a field must fulfill in order to be part of the result
     * @return all fields matching the filter, with the top-most parent's fields first, and this class's fields last
     */
    public static List<Field> getFieldsIncludingParents(Class<?> clazz, Predicate<Field> fieldFilter) {
        return getFieldsIncludingParents(clazz, fieldFilter, true);
    }

    /**
     * Returns all fields from the given class and its parents, recursively, that match the provided filter. Depending
     * on the parameter, fields are either returned from top-to-bottom or bottom-to-top relative to the class's
     * hierarchy.
     *
     * @param clazz the class whose fields (incl. its parents' fields) should be returned that match the filter
     * @param fieldFilter the condition a field must fulfill in order to be part of the result
     * @param topParentFirst true if the top-most parent's fields should come first, false for last
     * @return all fields matching the filter, in the specified order
     */
    public static List<Field> getFieldsIncludingParents(Class<?> clazz, Predicate<Field> fieldFilter,
                                                        boolean topParentFirst) {
        LinkedList<Class<?>> classes = new LinkedList<>();
        collectParents(clazz, (topParentFirst ? classes::addFirst : classes::addLast));

        return classes.stream()
            .flatMap(clz -> Arrays.stream(clz.getDeclaredFields()))
            .filter(fieldFilter)
            .collect(Collectors.toList());
    }

    public static List<Field> getInstanceFieldsIncludingParents(Class<?> clazz) {
        return getFieldsIncludingParents(clazz, FieldUtils::isRegularInstanceField, true);
    }

    public static Optional<Field> tryFindField(Class<?> clazz, String name) {
        try {
            return Optional.of(clazz.getDeclaredField(name));
        } catch (NoSuchFieldException ignore) {
            return Optional.empty();
        }
    }

    public static Optional<Field> tryFindFieldInClassOrParents(Class<?> clazz, String name) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Optional<Field> field = tryFindField(currentClass, name);
            if (field.isPresent()) {
                return field;
            }
            currentClass = currentClass.getSuperclass();
        }
        return Optional.empty();
    }

    private static void collectParents(Class<?> clazz, Consumer<Class<?>> classAdder) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            classAdder.accept(currentClass);
            currentClass = currentClass.getSuperclass();
        }
    }
}
