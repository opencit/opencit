/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.Test;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class YamlFileTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(YamlFileTest.class);

    private ObjectMapper getMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return mapper;
    }

    /**
     * Looks like this:
     * <pre>
     * ---
     * - username: "user1"
     * password_hash: "tyA60ZyvxFyyEpURnYwzxuDkSE9OEyMREApwht0+kPw="
     * salt: "14/mxd6epjs="
     * iterations: 1
     * algorithm: "SHA256"
     * - username: "user1"
     * password_hash: "tyA60ZyvxFyyEpURnYwzxuDkSE9OEyMREApwht0+kPw="
     * salt: "14/mxd6epjs="
     * iterations: 1
     * algorithm: "SHA256"
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testYamlWriteObjectList() throws Exception {
        ArrayList<UserPassword> list = new ArrayList<>();
        UserPassword u1 = new UserPassword();
        u1.setUsername("user1");
        u1.setAlgorithm("SHA256");
        u1.setIterations(1);
        u1.setSalt(RandomUtil.randomByteArray(8));
        u1.setPasswordHash(RandomUtil.randomByteArray(32));
        list.add(u1);
        list.add(u1);
        log.debug("yaml:\n{}", getMapper().writeValueAsString(list));
    }

    /**
     * Looks like this:
     * <pre>
     * ---
     * - "user1"
     * - "user2"
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testYamlWriteListString() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("user1");
        list.add("user2");
        log.debug("yaml:\n{}", getMapper().writeValueAsString(list));
    }

    /**
     * Looks like this:
     *
     * <pre>
     * ---
     * role2:
     * - "permission1"
     * - "permission2"
     * role1:
     * - "permission1"
     * - "permission2"
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testYamlWriteMapList() throws Exception {
        HashMap<String, List<String>> map = new HashMap<>();
        ArrayList<String> list1 = new ArrayList<>();
        list1.add("permission1");
        list1.add("permission2");
        map.put("role1", list1);
        ArrayList<String> list2 = new ArrayList<>();
        list2.add("permission1");
        list2.add("permission2");
        map.put("role2", list2);

        log.debug("yaml:\n{}", getMapper().writeValueAsString(map));
    }
}
