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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestUpdateSslPort.class);
    
    @Test
    public void testUpdateSslPort() throws Exception {

        File shiroIniFile = new File("C:\\Intel\\MtWilson\\conf\\shiro.ini");
        Ini shiroIni = new Ini();
        shiroIni.load(FileUtils.readFileToString(shiroIniFile));
        shiroIni.setSectionProperty("main","ssl.port",String.valueOf(8181));
        shiroIni.setSectionProperty("main","ssl.enabled",String.valueOf(true));
        String newShiroConfig = "";
        Collection<Ini.Section> sections = shiroIni.getSections();
        for (Ini.Section section : sections) {
            newShiroConfig = newShiroConfig + "[" + section.getName() + "]\r\n";
            for (String sectionKey : section.keySet()) {
                newShiroConfig = newShiroConfig + sectionKey + " = " + section.get(sectionKey) + "\r\n";
            }
        }
        FileUtils.writeStringToFile(shiroIniFile, newShiroConfig);

    }
    
    @Test
    public void testStoreIni() {
        String text = "[main]\nfoo=bar\n[baz]\nhello=world\n";
        Ini ini = new Ini();
        ini.load(text);
        log.debug("ini tostring: {}", ini.toString()); // outputs list of section names, for example: ini tostring: sections=main,baz
        Collection<Ini.Section> sections = ini.getSections();
        for(Ini.Section section : sections) {
            log.debug("section tostring: {}", section.toString()); // outputs section name, for example example:  "main"  and  "baz"
        }
    }
}
