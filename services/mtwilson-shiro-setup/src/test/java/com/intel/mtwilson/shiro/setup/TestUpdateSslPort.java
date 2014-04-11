/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.setup;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.config.Ini;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TestUpdateSslPort {
    
    @Test
    public void testUpdateSslPort() throws Exception {

        File shiroIniFile = new File("C:\\Intel\\MtWilson\\conf\\shiro.ini");
        Ini shiroIni = new Ini();
        shiroIni.load(FileUtils.readFileToString(shiroIniFile));
        shiroIni.setSectionProperty("main","ssl.port","443");
        shiroIni.setSectionProperty("main","ssl.enabled","8181");
        String newShiroConfig = "";
        Collection<Ini.Section> sections = shiroIni.getSections();
        for (Ini.Section section : sections) {
            newShiroConfig = newShiroConfig + "[" + section.getName() + "]\n";
            for (String sectionKey : section.keySet()) {
                newShiroConfig = newShiroConfig + sectionKey + " = " + section.get(sectionKey) + "\n";
            }
        }
        FileUtils.writeStringToFile(shiroIniFile, newShiroConfig);

    }
}
