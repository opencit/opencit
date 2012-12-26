/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.setup.model.*;
import java.net.URL;

/**
 *
 * @author jbuhacoff
 */
public class SetupContext {
    public SetupTarget target;
    
    public URL serverUrl; // typically https://serverAddress:serverPort 
    public InternetAddress serverAddress; // do we really need this if we're going to keep the URL ??? 
    public Integer serverPort; // do we really need this if we're going to keep the URL ??? 
    
    public Database databaseServer;
    
    public PrivacyCA privacyCA; // what is used to download Mt Wilson EK Signing Key (aka Privacy CA EK Signing Key) 
    public AdminUser admin; // first admin user that can then approve all other accounts and maange settings
}
