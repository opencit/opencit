/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tree;

import com.intel.mtwilson.tree.MutableTree;
import com.intel.mtwilson.pipe.Filter;

/**
 * An iterator that implements a breadth-first traversal of a Tree.
 * Whereas the BreadthFirstTreeSearch class requires the caller to provide a Visitor,
 * the BreadthFirstTreeIterator puts the caller in control -- the caller is implicitly the Visitor.
 * 
 * @author jbuhacoff
 */
public class BreadthFirstMutableTreeIterator<T> extends BreadthFirstTreeIterator<T> {
    private final MutableTree<T> tree;
    

    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     */
    public BreadthFirstMutableTreeIterator(MutableTree<T> tree, T start) {
        super(tree,start);
        this.tree = tree;
    }
    
    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     * @param filter object that filters nodes to search; for some trees if we know we are not interested in certain nodes, there is no sense in adding them to the stack in the first place; so when the filter rejects a node it's pruning the search tree
     */
    public BreadthFirstMutableTreeIterator(MutableTree<T> tree, T start, Filter<T> filter) {
        super(tree,start,filter);
        this.tree = tree;
    }

    @Override
    public void remove() {
        tree.remove(current);
    }
}
