/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tree;

import com.intel.mtwilson.tree.Visitor;
import com.intel.mtwilson.tree.Tree;
import com.intel.mtwilson.collection.ArrayStack;
import com.intel.mtwilson.pipe.Filter;
import java.util.List;

/**
 * Probably the DepthFirstTreeIterator is easier to use since the caller is the visitor, so there's
 * one less argument to create and no need to make a one-off implementation of the visitor interface.
 * 
 * XXX TODO Maybe the search class should be re-purposed to use the iterator to traverse the tree and keep track 
 * of nodes that "match" given criteria, then return the results.  So caller can use search to find if
 * a certain node is in the tree.  Can combine breadth-first and depth-first search into one class that
 * implements those two strategies as separate methods that internally use the appropriate tree iterator.
 * 
 * @author jbuhacoff
 */
public class DepthFirstTreeSearch<T> {
    

    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     */
    public void search(Tree<T> tree, T start, Visitor<T> visitor) {
        search(tree, start, visitor, null); // search with no filter
    }
    
    /**
     * 
     * @param tree object that knows how to find parent and children of any node of type T
     * @param start root node of the tree to search
     * @param visitor object that knows how to process each node
     * @param filter object that filters nodes to search; for some trees if we know we are not interested in certain nodes, there is no sense in adding them to the stack in the first place; so when the filter rejects a node it's pruning the search tree
     */
    public void search(Tree<T> tree, T start, Visitor<T> visitor, Filter<T> filter) {
        ArrayStack<T> stack = new ArrayStack<T>();
        stack.push(start);
        while(!stack.isEmpty()) {
            T current = stack.pop();
            visitor.visit(current);
            List<T> children = tree.children(current);
            if( children != null && !children.isEmpty() ) {
                for(T child : children) {
                    if( filter == null || filter.accept(child)) {
                        stack.push(child);
                    }
                }
            }
        }
    }
}
