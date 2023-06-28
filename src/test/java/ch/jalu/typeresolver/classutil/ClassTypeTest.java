package ch.jalu.typeresolver.classutil;

import org.junit.jupiter.api.Test;

import java.awt.font.NumericShaper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;

/**
 * Test for {@link ClassType}.
 */
class ClassTypeTest {

    /**
     * Tests the example in the Javadoc of {@link ClassType#ANNOTATION}.
     */
    @Test
    void shouldHaveValidDemo() throws NoSuchMethodException {
        // given
        Test ann = getClass().getDeclaredMethod("shouldHaveValidDemo").getAnnotation(Test.class);
        String annClassStr = ann.getClass().toString();     // Prints something like "$Proxy9"
        ClassType type = ClassUtil.getType(ann.getClass()); // Prints PROXY_CLASS

        // when / then
        // Java 8 and 11 have something like com.sun.proxy.$Proxy8, whereas Java 16 has "jdk.proxy2.$Proxy8"
        assertThat(annClassStr, matchesRegex("class (com\\.sun\\.proxy|jdk\\.proxy2)\\.\\$Proxy\\d+"));
        assertThat(type, equalTo(ClassType.PROXY_CLASS));
    }

    /**
     * Tests the example given in {@link ClassType#ENUM_ENTRY}.
     */
    @Test
    void shouldHaveValidEnumEntryExtensionExample() {
        assertThat(NumericShaper.Range.ETHIOPIC.getClass().getName(), matchesRegex(".*?\\.NumericShaper\\$Range\\$\\d+"));
    }
}