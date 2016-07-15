/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.shell;

/**
 *
 * @author dczech
 */
public class CommandLineResult {

    private int returnCode = 0;
    private String[] results = null;
    String returnOutput;

    public String getReturnOutput() {
        return returnOutput;
    }

    public void setReturnOutput(String returnOutput) {
        this.returnOutput = returnOutput;
    }

    /**
     *
     * @param newReturnCode
     * @param numResults
     */
    public CommandLineResult(int newReturnCode, int numResults) {
        returnCode = newReturnCode;
        results = new String[numResults];
    }

    /**
     *
     * @return
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     *
     * @param index
     * @param result
     * @throws IllegalArgumentException
     */
    public void setResult(int index, String result)
            throws IllegalArgumentException {
        if (index + 1 > results.length) {
            throw new IllegalArgumentException("Array index out of bounds.");
        }
        results[index] = result;
    }

    /**
     *
     * @return
     */
    public int getResultCount() {
        return results.length;
    }

    /**
     *
     * @param index
     * @return
     * @throws IllegalArgumentException
     */
    public String getResult(int index)
            throws IllegalArgumentException {
        if (index + 1 > results.length) {
            throw new IllegalArgumentException("Array index out of bounds.");
        }
        return results[index];
    }
}
