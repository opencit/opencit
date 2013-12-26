/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mountwilson.common;

import java.util.List;

/**
 *
 * @author stdalex
 */
 public class CommandResponse {
       private List<String> output;
       private int exitValue;
       
       public CommandResponse(){}
       
       public CommandResponse(List<String> output, int exitValue) {
           this.output = output;
           this.exitValue = exitValue;
       }
       
       public List<String> getOutput() { return this.output;}
       public void         setOutput(List<String> output) {this.output = output;}
       public int          getExitValue() { return this.exitValue;}
       public void         setExistValue(int exitValue){ this.exitValue = exitValue;}
}
 