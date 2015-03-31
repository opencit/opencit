/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.console.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Generates a password. The output will include only ASCII printable
 * characters.
 *
 * How to run this command: kms generate-password [--length=(#bytes)]
 *
 * Example output:
 * <pre>
 * CGaTpWf3YcFeEzyQfxlOAQ==
 * </pre>
 *
 * Note that the length parameter specifies how much randomness should be in the
 * password (number of random bytes) not necessarily how many characters should
 * be in the output; because the output is base64-encoded password, it will
 * always be longer than the actual password but this extra length does not
 * provide security because it is only encoding.
 *
 * This command does not require reading or writing to any configuration or
 * file. The user must copy and paste the generated password and provide it in
 * an environment variable when starting the KMS: export KMS_PASSWORD=(generated
 * password here)
 *
 * A complete PEM-style envelope can be printed with the base64-encoded key by
 * providing the --pem option: kms generate-password --pem
 *
 * Example PEM output:
 * <pre>
 * -----BEGIN PASSWORD-----
 * DrwgJGzw5C9rwpeQVkAU0TFxIu4JTTyzmeHmxcyxFaE=
 * -----END PASSWORD-----
 *
 * @author jbuhacoff
 */
public class GeneratePassword extends InteractiveCommand {

    @Override
    public void execute(String[] args) throws Exception {
        int lengthBytes = options.getInt("length", 16);

        char[] password = RandomUtil.randomBase64String(lengthBytes).toCharArray();

        if (this.options != null && options.getBoolean("pem", false)) {
            // print base64-encoded key in PEM-style format
            Pem pem = new Pem("PASSWORD", String.valueOf(password).getBytes(Charset.forName("UTF-8")));
            System.out.println(pem.toString());
        } else {
            System.out.println(password);
        }
    }
}
