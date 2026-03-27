package dog.wiggler.tree23;

public interface NormalizedTree<A, L extends Leaf1<A, L>> extends Tree<A, L> {
    A aggregate();
}
