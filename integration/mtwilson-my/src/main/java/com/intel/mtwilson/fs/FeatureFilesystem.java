/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public interface FeatureFilesystem {
    String getBinPath();
    String getHypertextPath();
    String getJavaPath();
    String getLicensePath();
    String getLinuxUtilPath();
    String getSqlPath();
    String getVarPath(); // working directory
//    String getWindowsUtilPath();
//    String getPerlPath();
//    String getPythonPath();
}
