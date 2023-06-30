package ch.jalu.typeresolver.modifier;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

public abstract class ModifierFilter {

    public boolean matches(Member member) {
        return matches(member.getModifiers());
    }

    public boolean matches(Class<?> clazz) {
        return matches(clazz.getModifiers());
    }

    public abstract boolean matches(int modifiers);

    public static ModifierFilter is(int flag) {
        return new ExactValueFilter(flag, false);
    }

    public static ModifierFilter isNot(int flag) {
        return new ExactValueFilter(flag, true);
    }

    public static ModifierFilter is(int... flags) {
        if (flags.length == 0) {
            return alwaysTrue();
        }
        return new ExactValueFilter(combineFlagsWithOr(flags), false);
    }

    public static ModifierFilter isAny(int... flags) {
        if (flags.length == 0) {
            return alwaysFalse();
        }
        return new AnyValueFilter(ModifierFilter.combineFlagsWithOr(flags), false);
    }

    public static ModifierFilter isNotAny(int... flags) {
        if (flags.length == 0) {
            return alwaysTrue();
        }
        return new AnyValueFilter(combineFlagsWithOr(flags), true);
    }

    public static ModifierFilter isPublicStaticFinal() {
        return is(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
    }

    public static ModifierFilter isInstanceMember() {
        return isNot(Modifier.STATIC);
    }

    public static ModifierFilter isPackagePrivate() {
        return isNotAny(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE);
    }

    public static ModifierFilter alwaysTrue() {
        return new ConstantFilter(true);
    }

    public static ModifierFilter alwaysFalse() {
        return new ConstantFilter(false);
    }

    public abstract ModifierFilter negate();

    public ModifierFilter or(ModifierFilter other) {
        return new CombiningModifierFilter(this, other, CombineOp.OR);
    }

    public ModifierFilter and(ModifierFilter other) {
        return new CombiningModifierFilter(this, other, CombineOp.AND);
    }

    public ModifierFilter xor(ModifierFilter other) {
        return new CombiningModifierFilter(this, other, CombineOp.XOR);
    }

    private static int combineFlagsWithOr(int[] flags) {
        return IntStream.of(flags).reduce(0, (a, b) -> a | b);
    }

    private static final class ExactValueFilter extends ModifierFilter {

        private final int flag;
        private final boolean negate;

        ExactValueFilter(int flag, boolean negate) {
            this.flag = flag;
            this.negate = negate;
        }

        public boolean matches(int modifiers) {
            boolean hasFlag = (modifiers & flag) == flag;
            return hasFlag ^ negate;
        }

        public ExactValueFilter negate() {
            return new ExactValueFilter(flag, !negate);
        }

        public String toString() {
            return "ValueFilter[value=" + flag + ", negate=" + negate + "]";
        }
    }

    private static final class AnyValueFilter extends ModifierFilter {

        private final int flag;
        private final boolean negate;

        AnyValueFilter(int flag, boolean negate) {
            this.flag = flag;
            this.negate = negate;
        }

        public boolean matches(int modifiers) {
            boolean hasAnyValue = (modifiers & flag) != 0;
            return hasAnyValue ^ negate;
        }

        public AnyValueFilter negate() {
            return new AnyValueFilter(flag, !negate);
        }

        public String toString() {
            return "AnyValueFilter[value=" + flag + ", negate=" + negate + "]";
        }
    }

    private static final class ConstantFilter extends ModifierFilter {

        private final boolean constantResult;

        private ConstantFilter(boolean constantResult) {
            this.constantResult = constantResult;
        }

        @Override
        public boolean matches(int modifiers) {
            return constantResult;
        }

        @Override
        public ModifierFilter negate() {
            return new ConstantFilter(!constantResult);
        }

        @Override
        public ModifierFilter and(ModifierFilter other) {
            if (!constantResult) {
                return this;
            }
            return super.and(other);
        }

        @Override
        public ModifierFilter or(ModifierFilter other) {
            if (constantResult) {
                return this;
            }
            return super.or(other);
        }
    }

    private enum CombineOp {
        /** a || b */
        OR((r1, r2) -> r1.getAsBoolean() || r2.getAsBoolean()),
        /** a && b */
        AND((r1, r2) -> r1.getAsBoolean() && r2.getAsBoolean()),
        /** NOR(a, b) = AND(!a, !b) */
        NOR((r1, r2) -> !r1.getAsBoolean() && !r2.getAsBoolean()),
        /** NAND(a, b) = OR(!a, !b) */
        NAND((r1, r2) -> !r1.getAsBoolean() || !r2.getAsBoolean()),
        /** XOR(a, b) */
        XOR((r1, r2) -> r1.getAsBoolean() ^ r2.getAsBoolean()),
        /** a == b, used to negate XOR. */
        EQ((r1, r2) -> r1.getAsBoolean() == r2.getAsBoolean());

        private final BiFunction<BooleanSupplier, BooleanSupplier, Boolean> combiner;

        CombineOp(BiFunction<BooleanSupplier, BooleanSupplier, Boolean> combiner) {

            this.combiner = combiner;
        }

        boolean combine(BooleanSupplier result1, BooleanSupplier result2) {
            return combiner.apply(result1 ,result2);
        }

        CombineOp negate() {
            switch (this) {
                case OR:  return NOR;
                case AND: return NAND;
                case NOR: return OR;
                case XOR: return EQ;
                case EQ:  return XOR;
                default:  return AND; // for NAND
            }
        }
    }

    private static final class CombiningModifierFilter extends ModifierFilter {

        private final ModifierFilter filter1;
        private final ModifierFilter filter2;
        private final CombineOp combineOp;

        CombiningModifierFilter(ModifierFilter filter1, ModifierFilter filter2, CombineOp combination) {
            this.filter1 = filter1;
            this.filter2 = filter2;
            this.combineOp = combination;
        }

        public boolean matches(int modifiers) {
            return combineOp.combine(
                () -> filter1.matches(modifiers),
                () -> filter2.matches(modifiers));
        }

        public CombiningModifierFilter negate() {
            return new CombiningModifierFilter(filter1, filter2, combineOp.negate());
        }

        public String toString() {
            return combineOp + "Filter[filter1=" + filter1 + ", filter2=" + filter2 + "]";
        }
    }
}
