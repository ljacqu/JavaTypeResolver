package ch.jalu.typeresolver;

import java.util.Optional;

/**
 * Test helper for types and features that depend on the Java version.
 */
public final class JavaVersionHelper {

    private JavaVersionHelper() {
    }

    public static Optional<ConstableAndConstantDescTypes> getConstableClassIfApplicable() {
        if (isJavaVersionGreaterOrEqualTo(12)) {
            ConstableAndConstantDescTypes types = new ConstableAndConstantDescTypes(
                getClass("java.lang.constant.Constable"), getClass("java.lang.constant.ConstantDesc"));
            return Optional.of(types);
        }
        return Optional.empty();
    }

    private static boolean isJavaVersionGreaterOrEqualTo(int version) {
        String javaVersion = System.getProperty("java.version");
        int majorVersion;
        if (javaVersion.startsWith("1.")) {
            majorVersion = Integer.parseInt(javaVersion.substring(2, 3));
        } else {
            majorVersion = Integer.parseInt(javaVersion.substring(0, javaVersion.indexOf('.')));
        }
        return majorVersion >= version;
    }

    private static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class ConstableAndConstantDescTypes {

        private final Class<?> constableClass;
        private final Class<?> constantDescClass;

        public ConstableAndConstantDescTypes(Class<?> constableClass, Class<?> constantDescClass) {
            this.constableClass = constableClass;
            this.constantDescClass = constantDescClass;
        }

        public Class<?> getConstableClass() {
            return constableClass;
        }

        public Class<?> getConstantDescClass() {
            return constantDescClass;
        }
    }
}
