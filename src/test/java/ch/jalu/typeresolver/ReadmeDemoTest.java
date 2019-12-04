package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.AbstractArrayProperties;
import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests the snippets similar to the demos in the README.MD for consistency.
 */
class ReadmeDemoTest {

  private List<String> list;

  @Test
  void shouldExecuteFirstDemoAsExpected() throws Exception {
    TypeInfo listType = TypeInfo.of(ReadmeDemoTest.class.getDeclaredField("list"));

    // Outputs: "Generic type of List is class java.lang.String"
    // System.out.println("Generic type of List is " + listType.getTypeArgumentAsClass(0));
    assertThat(listType.getTypeArgumentAsClass(0), equalTo(String.class));

    // Outputs: "As an Iterable: java.lang.Iterable<java.lang.String>"
    // System.out.println("As an Iterable: " + listType.resolveSuperclass(Iterable.class).getType());
    assertThat(listType.resolveSuperclass(Iterable.class), equalTo(new TypeReference<Iterable<String>>() { }));

    // Outputs: "List#get returns E, which we know is class java.lang.String"
    Type getReturnRaw = List.class.getDeclaredMethod("get", int.class).getGenericReturnType();
    Type getReturnActual = listType.resolve(getReturnRaw).getType();
    // System.out.println("List#get returns " + getReturnRaw + ", which we know is " + getReturnActual);
    assertThat(getReturnRaw + "//" + getReturnActual, equalTo("E//class java.lang.String"));
  }

  @Test
  void shouldExecuteSecondDemoAsExpected() {
    TypeInfo mySetType = new TypeReference<Set<Double>>() { };
    // Outputs: "Created set type: java.util.Set<java.lang.Double>"
    // System.out.println("Created set type: " + mySetType.getType());
    assertThat(mySetType.getType().toString(), equalTo("java.util.Set<java.lang.Double>"));

    Type myWildcard = WildcardTypeImpl.newWildcardExtends(String.class);
    // Outputs: "Created wildcard: ? extends java.lang.String"
    // System.out.println("Created wildcard: " + myWildcard);
    assertThat(myWildcard.toString(), equalTo("? extends java.lang.String"));

    // Implementations of Type interfaces are available which have the same #equals and #hashCode as the JRE impl.
    ParameterizedType pt = new ParameterizedTypeImpl(Set.class, null, Double.class);
    // Outputs: "Set types equal -> true"
    // System.out.println("Set types equal -> " + pt.equals(mySetType.getType()));
    assertThat(pt, equalTo(mySetType.getType()));
  }

  @Test
  void shouldExecuteThirdDemoAsExpected() {
    AbstractArrayProperties arrayInfo = CommonTypeUtil.getArrayProperty(String[][].class);
    // Outputs: "Component = class java.lang.String, dimension = 2"
    // System.out.println("Component = " + arrayInfo.getComponentType() + ", dimension = " + arrayInfo.getDimension());
    assertThat(arrayInfo.getComponentType(), equalTo(String.class));
    assertThat(arrayInfo.getDimension(), equalTo(2));

    Type listType = new TypeReference<List<String>>() { }.getType();
    Type doubleArr = CommonTypeUtil.createArrayType(listType, 3);
    // Outputs: "Created java.util.List<java.lang.String>[][][]"
    // System.out.println("Created type " + doubleArr);
    assertThat(doubleArr.toString(), equalTo("java.util.List<java.lang.String>[][][]"));
  }
}