/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tree;
import java.util.List;

    // not the type itself but the structure that says "what are the children" ; 

/**
 * A tree is a connected graph with no cycles; there is no corresponding Tree class because a collection of 
 * connected TreeNodes represents an entire tree. The TreeNode where parent() == null is the root.
 * 
 * see also net.sourceforge.jsl.AbstractSearchNode that defines cost, depth, breadth, getitem, incoming edgest, and outgoing edges, link forward/back
 * and http://en.wikipedia.org/wiki/Graph_(mathematics)
 * 
 * and http://docs.oracle.com/javase/6/docs/api/javax/swing/tree/DefaultMutableTreeNode.html  that defines a lot
 * of methods on each node such as finding siblings, and incorporates node, leaf, and child concepts so there
 * are methods that look similar but operate on different nouns. 
 * 
 * Note that the Tree interface is an adapter: you only need to instantiate a single object that implements
 * a Tree of type T and that single object knows how to find the parent and children of nodes in the tree.
 * This approach is different than implementing a Node interface where each node knows its own parent and 
 * children, because when implementing a Node interface each node in the tree needs to implement that interface
 * which means that, for example, if you want to represent a directory of files as a tree you have to wrap
 * each File object with a Node object and that leads to a lot of instantiations. On the other hand, if you
 * have a single Tree<File> object that knows how to call getParentFile and listFiles on each node, it can
 * help you walk the tree without creating any additional objects.
 * The Node interface mentioned would look like this:
 * <pre>
 * public interface Node<T> {
 *   Node<T> parent();
 *   List<Node<T>> children();
 * }
 * </pre>
 * 
 * @author jbuhacoff
 */
public interface Tree<T> {
    T parent(T node); // given a node, returns its parent
    List<T> children(T node); // given a node , erturns its chilrdren
}
