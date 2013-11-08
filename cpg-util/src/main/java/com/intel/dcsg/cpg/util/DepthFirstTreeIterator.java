/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

import java.util.Iterator;
import java.util.List;

/**
 * An iterator that implements a depth-first traversal of a Tree.
 * Whereas the DepthFirstTreeSearch class requires the caller to provide a Visitor,
 * the DepthFirstTreeIterator puts the caller in control -- the caller is implicitly the Visitor.
 * 
 * @author jbuhacoff
 */
public class DepthFirstTreeIterator<T> implements Iterator<T> {
    private final Tree<T> tree;
    protected T current;
    private final Filter<T> filter;
    private final ArrayStack<T> stack = new ArrayStack<T>();
    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     */
    public DepthFirstTreeIterator(Tree<T> tree, T start) {
        this.tree = tree;
        this.current = start;
        this.filter = null;
        stack.push(current);
    }
    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     * @param filter object that filters nodes to search; for some trees if we know we are not interested in certain nodes, there is no sense in adding them to the stack in the first place; so when the filter rejects a node it's pruning the search tree
     */
    public DepthFirstTreeIterator(Tree<T> tree, T start, Filter<T> filter) {
        this.tree = tree;
        this.current = start;
        this.filter = filter;
        stack.push(current);
    }
    
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    public T next() {
        current = stack.pop();
        List<T> children = tree.children(current);
        if( children != null && !children.isEmpty() ) {
            for(T child : children) { // if you imagine the children are ordered left-to-right, this adds them to the stack in that order which means they get processed in the reverse order: right-to-left ; so the search starts with the right-most branch is explored to down to its last node.   if you need to have the children "in order" probably best to reverse the order, and it should be optional so that programs that don't care about which side is traversed first don't pay for work they don't need
                if( filter == null || filter.accept(child)) {
                    stack.push(child);
                }
            }
        }
        return current;
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove node from tree"); // MutableTree and DepthFirstMutableTreeIterator support this
    }
}
