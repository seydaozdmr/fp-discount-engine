package com.example.discount;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple exclusivity matrix: if a group is applied, it can block other groups.
 */
public final class ExclusivityPolicy {

    private final Map<DiscountGroup, Set<DiscountGroup>> blocks = new EnumMap<>(DiscountGroup.class);

    public ExclusivityPolicy block(DiscountGroup applied, DiscountGroup... blocked) {
        if (blocked == null || blocked.length == 0) {
            blocks.put(applied, EnumSet.noneOf(DiscountGroup.class));
            return this;
        }
        EnumSet<DiscountGroup> set = EnumSet.noneOf(DiscountGroup.class);
        for (DiscountGroup g : blocked) set.add(g);
        blocks.put(applied, set);
        return this;
    }

    public boolean isAllowed(Set<DiscountGroup> alreadyApplied, DiscountGroup candidate) {
        for (DiscountGroup g : alreadyApplied) {
            Set<DiscountGroup> blocked = blocks.getOrDefault(g, Set.of());
            if (blocked.contains(candidate)) return false;
        }
        return true;
    }
}
