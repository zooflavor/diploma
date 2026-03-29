package dog.wiggler.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LRUPolicy implements ReplacementPolicy {
    private static class ListNode {
        public final @NotNull Long address;
        public boolean dirty;
        public @Nullable ListNode next;
        public @Nullable ListNode prev;

        public ListNode(
                @NotNull Long address,
                boolean dirty) {
            this.address=Objects.requireNonNull(address, "address");
            this.dirty=dirty;
        }
    }

    private final @NotNull Map<@NotNull Long, @NotNull ListNode> lines=new HashMap<>();
    private @Nullable ListNode listHead;
    private @Nullable ListNode listTail;

    @Override
    public void access(long address, boolean dirty) {
        var node=lines.get(address);
        if (null==node) {
            throw new IllegalArgumentException("address is not cached");
        }
        if (dirty) {
            node.dirty=true;
        }
        unlink(node);
        linkFirst(node);
    }

    @Override
    public void addAndAccess(long address, boolean dirty) {
        var node=new ListNode(address, dirty);
        linkFirst(node);
        var oldValue=lines.put(address, node);
        if (null!=oldValue) {
            throw new IllegalArgumentException("address is already cached");
        }
    }

    @Override
    public boolean contains(long address) {
        return lines.containsKey(address);
    }

    @Override
    public @Nullable Long evict() {
        if (null==listTail) {
            throw new IllegalStateException("cache is empty");
        }
        var node=listTail;
        unlink(node);
        lines.remove(node.address);
        return node.dirty
                ?node.address
                :null;
    }

    private void linkFirst(@NotNull ListNode node) {
        node.next=listHead;
        if (null!=node.next) {
            node.next.prev=node;
        }
        listHead=node;
        if (null==listTail) {
            listTail=node;
        }
    }

    @Override
    public int size() {
        return lines.size();
    }

    private void unlink(@NotNull ListNode node) {
        if (listHead==node) {
            listHead=node.next;
        }
        if (listTail==node) {
            listTail=node.prev;
        }
        if (null!=node.next) {
            node.next.prev=node.prev;
        }
        if (null!=node.prev) {
            node.prev.next=node.next;
        }
        node.next=null;
        node.prev=null;
    }
}
