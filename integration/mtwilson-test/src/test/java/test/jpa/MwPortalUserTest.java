/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jpa;

import com.intel.mtwilson.My;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import com.intel.mtwilson.ms.data.MwPortalUser;
/**
 *
 * @author jbuhacoff
 */
public class MwPortalUserTest {
    @Test
    public void testFindByUsernameEnabled() throws IOException {
        String username = "jbuhacof";
        List<MwPortalUser> list = My.jpa().mwPortalUser().findMwPortalUserByUsernameEnabled(username);
        for(MwPortalUser user : list) {
            System.out.println(user.getUsername());
        }
    }
}
