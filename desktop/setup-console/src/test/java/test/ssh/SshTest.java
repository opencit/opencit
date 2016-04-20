/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;
import net.schmizz.sshj.SSHClient;
import org.junit.Test;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 *
 * @author jbuhacof
 */
public class SshTest {
    
    @Test
    public void testLoadPropertiesFile() throws IOException {
        SSHClient ssh = new SSHClient();
         //ssh.useCompression(); // Can lead to significant speedup (needs JZlib in classpath)
        //ssh.loadKnownHosts();
//        ssh.addHostKeyVerifier("01:c7:75:8e:0a:6c:57:48:22:a5:57:2b:di:28:eg:da");
        ssh.addHostKeyVerifier(new HostKeyVerifier() {@Override public boolean verify(String arg0, int arg1, PublicKey arg2) { return true; } }); // this accepts all remote public keys
        ssh.connect("10.1.71.103");
        try {
            System.out.println("Username: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String username = in.readLine();
            System.out.println("Password: ");
            String password = in.readLine();
            ssh.authPassword(username, password);
                final String src = "/etc/intel/cloudsecurity/attestation-service.properties";
                final String target = "C:\\TEMP\\attestation-service.properties";
                ssh.newSCPFileTransfer().download(src, target);
            }
        finally {
            ssh.disconnect();
        }
    }
}
