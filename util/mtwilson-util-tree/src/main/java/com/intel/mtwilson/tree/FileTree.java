/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tree;

import com.intel.mtwilson.tree.Tree;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author jbuhacoff
 */
public class FileTree implements Tree<File> {
    private File root;
    
    public FileTree() {
        root = null;
    }
    
    /**
     * Create a FileTree with a virtual root.  This is normally not necessary; only use it
     * if you want to limit how far "up" the filesystem the application can go when using
     * the FileTree to search.
     * 
     * @param root the virtual root directory of the FileTree; parent(root) will return null ; useful for limiting traversal of parents up to a given directory 
     */
    public FileTree(File root) {
        this.root = root;
    }
    
    /**
     * If the FileTree has a virtual root, and it's the same as the given node, this method returns false.
     * Otherwise, this method returns the real parent directory of the given node if it exists or null if it
     * doesn't exist ( / on linux and C: on windows do not have parent directories)
     * @param node
     * @return 
     */
    @Override
    public File parent(File node) {
        if( root != null && root.equals(node) ) { return null; }
        return node.getParentFile();
    }

    @Override
    public List<File> children(File node) {
        if( node.isDirectory() ) {
            File[] children = node.listFiles();
            if( children != null ) {
                return Arrays.asList(children);
            }
        }
        return null;
    }
    
}
