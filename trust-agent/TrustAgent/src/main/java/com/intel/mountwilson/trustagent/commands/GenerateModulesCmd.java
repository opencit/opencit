/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 *
 * @author skaja
 */
public class GenerateModulesCmd implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;

    public GenerateModulesCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {
            getXmlFromMeasureLog();

        } catch (Exception ex) {
            throw new TAException(ErrorCode.ERROR, "Error while getting Module details.", ex);
        }

    }

    /**
     * calls OAT script prepares XML from measureLog
     *
     * @author skaja
     */
    private void getXmlFromMeasureLog() throws TAException, IOException {

        
        log.info(String.format("%s%s", context.getModulesFolder(), context.getMeasureLogLaunchScript()));
        long startTime = System.currentTimeMillis();
        CommandUtil.runCommand( context.getMeasureLogLaunchScript());
        long endTime = System.currentTimeMillis();
        log.info("measureLog.xml is created from txt-stat in Duration MilliSeconds {}", (endTime - startTime));

        BufferedReader in = null;
        String str = "";
        String content = "";
        try {
            in = new BufferedReader(new FileReader(context.getMeasureLogXmlFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while ((str = in.readLine()) != null) {
                content = content + str;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getModulesFromMeasureLogXml(content);

        CommandUtil.runCommand(String.format("rm -fr %s", context.getMeasureLogXmlFile()));

    }

    /**
     * Obtains <modules> tag under <txt> and add the string to TADataContext
     *
     * @author skaja
     */
    private void getModulesFromMeasureLogXml(String xmlInput) throws TAException {
        try {

            Pattern PATTERN = Pattern.compile("(<txt>.*</txt>)");
            Matcher m = PATTERN.matcher(xmlInput);
            while (m.find()) {
                xmlInput = m.group(1);
            }
            PATTERN = Pattern.compile("(<modules>.*</modules>)");
            m = PATTERN.matcher(xmlInput);
            while (m.find()) {
                xmlInput = m.group(1);
            }

            context.setModules(xmlInput.replaceAll(">\\s*<", "><"));

        } catch (Exception e) {
            throw new TAException(ErrorCode.BAD_REQUEST, "Cannot find modules in the input xml");
        }

    }
}
