/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.mtwilson.setup.SetupTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Returns a list of setup tasks in the order they should be run
 * 
 * @author jbuhacoff
 */
public class SetupTaskFactory {
    public static List<SetupTask> getTasks() {
        ArrayList<SetupTask> list = new ArrayList<>();
        list.add(new CreateKeystorePassword());
        list.add(new CreateTlsKeypair());
        list.add(new CreateAdminUser());
        list.add(new CreateTpmOwnerSecret());
        list.add(new CreateAikSecret());
        list.add(new TakeOwnership());
        list.add(new DownloadMtWilsonTlsCertificate());
        list.add(new DownloadMtWilsonPrivacyCACertificate());
        list.add(new RequestEndorsementCertificate());
        list.add(new RequestAikCertificate());
        // TODO: register host with Mt Wilson (TBD - requires Mt Wilson to allow registration and setting trust policy as separate steps, which is not yet implemented)
        return list;
    }
}
