#!/bin/bash

#gcc  -lcrypto -ltspi -o target/create_tpm_key2 create_tpm_key2.c
gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Iprereqs/tpm-tools-1.3.8 -Iprereqs/tpm-tools-1.3.8/include -o target/tpm_bindaeskey tpm_bindaeskey.c prereqs/tpm-tools-1.3.8/lib/tpm_tspi.c prereqs/tpm-tools-1.3.8/lib/tpm_utils.c prereqs/tpm-tools-1.3.8/lib/tpm_log.c -lcrypto -ltspi 
gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Iprereqs/tpm-tools-1.3.8 -Iprereqs/tpm-tools-1.3.8/include -o target/tpm_unbindaeskey tpm_unbindaeskey.c prereqs/tpm-tools-1.3.8/lib/tpm_tspi.c prereqs/tpm-tools-1.3.8/lib/tpm_utils.c prereqs/tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi 
