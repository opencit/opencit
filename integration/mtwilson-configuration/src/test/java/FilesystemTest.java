
import com.intel.mtwilson.MyConfiguration;
import java.io.IOException;
import org.junit.Test;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

/**
 *
 * @author jbuhacoff
 */
public class FilesystemTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FilesystemTest.class);
/*
    @Test
    public void testDefaultFilesystem() {
        ApplicationFilesystem fs = new ConfigurableFilesystem();
        log.debug("applicationPath = {}", fs.getApplicationPath());
        log.debug("configurationPath = {}", fs.getConfigurationPath());
        log.debug("environmentExtPath = {}", fs.getEnvironmentExtPath());
        FeatureFilesystem bootstrapFilesystem = fs.getBootstrapFilesystem();
        log.debug("bin = {}", bootstrapFilesystem.getBinPath());
        log.debug("hypertext = {}", bootstrapFilesystem.getHypertextPath());
        log.debug("java = {}", bootstrapFilesystem.getJavaPath());
        log.debug("license = {}", bootstrapFilesystem.getLicensePath());
        log.debug("linux-util = {}", bootstrapFilesystem.getLinuxUtilPath());
        log.debug("sql = {}", bootstrapFilesystem.getSqlPath());
        log.debug("var = {}", bootstrapFilesystem.getVarPath());
    }
    */
    @Test
    public void testRetrieveLocale() throws IOException {
        MyConfiguration myConfiguration = new MyConfiguration();
        String[] availableLocales = myConfiguration.getAvailableLocales();
        System.out.println(availableLocales);
}
}
