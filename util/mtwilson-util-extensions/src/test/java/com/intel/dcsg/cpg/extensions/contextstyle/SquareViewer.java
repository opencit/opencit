/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class SquareViewer extends ShapeViewer {
    private Square square;
    public SquareViewer(Square square) {
        this.square = square;
    }
    @Override
    public String getShapeDescription() {
        return String.format("%s square with length %d", square.getColor(), square.getLength());
    }
    
}
