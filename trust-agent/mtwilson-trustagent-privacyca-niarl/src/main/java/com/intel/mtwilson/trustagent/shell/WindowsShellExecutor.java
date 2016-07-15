/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.shell;

import java.util.List;

/**
 *
 * @author dczech
 */
class WindowsShellExecutor extends GenericShellExecutor {

    @Override
    void PrepareCommandOverride(List<String> cmd) {        
        cmd.add(0, "TPMTool.exe");
        cmd.add(0, "/c");
        cmd.add(0, "cmd.exe");
    }
    
}
