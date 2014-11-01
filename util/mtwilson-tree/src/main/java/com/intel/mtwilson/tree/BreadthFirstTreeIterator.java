/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tree;

import com.intel.mtwilson.tree.Tree;
import com.intel.mtwilson.pipe.Filter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An iterator that implements a breadth-first traversal of a Tree.
 * Whereas the BreadthFirstTreeSearch class requires the caller to provide a Visitor,
 * the BreadthFirstTreeIterator puts the caller in control -- the caller is implicitly the Visitor.
 * 
 * @author jbuhacoff
 */
public class BreadthFirstTreeIterator<T> implements Iterator<T> {
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BreadthFirstTreeSearch.class);
    private final Tree<T> tree;
    protected T current;
    private final Filter<T> filter;
    private final LinkedList<T> queue = new LinkedList<T>();


    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     */
    public BreadthFirstTreeIterator(Tree<T> tree, T start) {
        this.tree = tree;
        this.current = start;
        this.filter = null;
        queue.addLast(current);
    }
    
    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     * @param filter object that filters nodes to search; for some trees if we know we are not interested in certain nodes, there is no sense in adding them to the stack in the first place; so when the filter rejects a node it's pruning the search tree
     */
    public BreadthFirstTreeIterator(Tree<T> tree, T start, Filter<T> filter) {
        this.tree = tree;
        this.current = start;
        this.filter = filter;
        queue.addLast(current);
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public T next() {
        current = queue.removeFirst();
        List<T> children = tree.children(current);
        if( children != null && !children.isEmpty() ) {
            for(T child : children) {
                if( filter == null || filter.accept(child)) {
                    queue.addLast(child);
                }
            }
        }
        return current;
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove node from tree"); // MutableTree and BreadthFirstMutableTreeIterator support this
    }
}
