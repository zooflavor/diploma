package dog.wiggler.tree23;

import org.jetbrains.annotations.NotNull;

/**
 * The receiver side of the visitor pattern,
 * to pattern match on the type of a {@link Tree}.
 * <br>
 * Use with {@link Tree#visit(Visitor)}.
 */
public interface Visitor<A, L extends Leaf1<A, L>, R> {
    /**
     * A visitor implementation where all methods throw az {@link IllegalArgumentException}.
     */
    abstract class FailVisitor<A, L extends Leaf1<A, L>, R> implements Visitor<A, L, R> {
        @Override
        public R branch1(@NotNull Branch1<A, L> branch1) throws Throwable {
            throw new IllegalArgumentException("branch1 %s".formatted(branch1));
        }

        @Override
        public R branch2(@NotNull Branch2<A, L> branch2) throws Throwable {
            throw new IllegalArgumentException("branch2 %s".formatted(branch2));
        }

        @Override
        public R branch3(@NotNull Branch3<A, L> branch3) throws Throwable {
            throw new IllegalArgumentException("branch3 %s".formatted(branch3));
        }

        @Override
        public R branch4(@NotNull Branch4<A, L> branch4) throws Throwable {
            throw new IllegalArgumentException("branch4 %s".formatted(branch4));
        }

        @Override
        public R leaf0(@NotNull Leaf0<A, L> leaf0) throws Throwable {
            throw new IllegalArgumentException("leaf0 %s".formatted(leaf0));
        }

        @Override
        public R leaf1(@NotNull L leaf1) throws Throwable {
            throw new IllegalArgumentException("leaf1 %s".formatted(leaf1));
        }

        @Override
        public R leaf2(@NotNull Leaf2<A, L> leaf2) throws Throwable {
            throw new IllegalArgumentException("leaf2 %s".formatted(leaf2));
        }
    }

    R branch1(@NotNull Branch1<A, L> branch1) throws Throwable;

    R branch2(@NotNull Branch2<A, L> branch2) throws Throwable;

    R branch3(@NotNull Branch3<A, L> branch3) throws Throwable;

    R branch4(@NotNull Branch4<A, L> branch4) throws Throwable;

    R leaf0(@NotNull Leaf0<A, L> leaf0) throws Throwable;

    R leaf1(@NotNull L leaf1) throws Throwable;

    R leaf2(@NotNull Leaf2<A, L> leaf2) throws Throwable;
}
