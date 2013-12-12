/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

import com.intel.dcsg.cpg.util.DepthFirstTreeIterator;
import com.intel.dcsg.cpg.util.BreadthFirstTreeSearch;
import com.intel.dcsg.cpg.util.Visitor;
import com.intel.dcsg.cpg.util.Tree;
import com.intel.dcsg.cpg.util.BreadthFirstTreeIterator;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class BreadthFirstTreeSearchTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BreadthFirstTreeSearchTest.class);
    private    StringTree tree = new StringTree();
            StringNode root = new StringNode("a", 
                new StringNode("b",
                    new StringNode("d"),
                    new StringNode("e")), 
                new StringNode("c",
                    new StringNode("f"),
                    new StringNode("g")));

    @Test
    public void testStringTreeSearch() {

        BreadthFirstTreeSearch<StringNode> search = new BreadthFirstTreeSearch<StringNode>();
        Visitor<StringNode> visitor = new Visitor<StringNode>() {
            public void visit(StringNode item) {
                log.debug("visit: {}", item.value);
            }
        };
        search.search(tree, root, visitor);
    }
    
    @Test
    public void testBreadthFirstStringTreeIterator() {
        Iterator<StringNode> it = new BreadthFirstTreeIterator<StringNode>(tree, root);
        while(it.hasNext()) {
            log.debug("iterator: {}", it.next());
        }
    }

    @Test
    public void testDepthFirstStringTreeIterator() {
        Iterator<StringNode> it = new DepthFirstTreeIterator<StringNode>(tree, root);
        while(it.hasNext()) {
            log.debug("iterator: {}", it.next());
        }
    }
    
    public static class StringNode {
        public String value = null;
        public StringNode parent = null;
        public StringNode[] children = null;
        public StringNode() { }
        public StringNode(String value) {
            this.value = value;
        }
        public StringNode(String value, StringNode... children) {
            this.value = value;
            this.children = children;
        }
        public List<StringNode> children() { return children == null ? null : Arrays.asList(children); }
        public String toString() { return String.format("StringNode(%s)", value); }
    }
    
public static class StringTree implements Tree<StringNode> {

    public StringNode parent(StringNode node) {
        throw new UnsupportedOperationException();
    }

    public List<StringNode> children(StringNode node) {
        return node.children();
    }
    
}
    
}
