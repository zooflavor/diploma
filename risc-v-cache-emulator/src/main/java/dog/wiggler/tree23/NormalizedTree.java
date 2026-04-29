package dog.wiggler.tree23;

/**
 * Superinterface for nodes that are normalized.
 * A tree is normalized if it's a leaf holding one data,
 * or a branch holding two or three children.
 */
public interface NormalizedTree<A, L extends Leaf1<A, L>> extends Tree<A, L> {
    /**
     * Return the aggregated data of the leaves of this subtree.
     */
    A aggregate();
}
