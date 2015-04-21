#!/bin/bash

OPENSSL=openssl-1.0.2a
TROUSERS=trousers-0.3.13
OPENSSL_TPM_ENGINE=openssl_tpm_engine-0.4.2
TPM_TOOLS=$(find . -type d -name "tpm-tools*" | head -n1)
HEX2BIN=hex2bin-master

install_openssl() {
  (cd $OPENSSL && ./config --shared && make && make install)
  if [ -d /etc/ld.so.conf.d ]; then
    echo /usr/local/ssl/lib/ > /etc/ld.so.conf.d/openssl.conf
  fi
  ldconfig
}

install_trousers() {
  (cd $TROUSERS && ./configure --prefix=/usr/local --with-openssl=/usr/local/ssl && make && make install)
  if [ -d /etc/ld.so.conf.d ]; then
    echo /usr/local/lib > /etc/ld.so.conf.d/trousers.conf
  fi
  ldconfig
}

install_tpm_tools() {
  patch $TPM_TOOLS/src/tpm_mgmt/tpm_nvread.c tpm-tools-1.3.8_src_tpm_nvread.patch
  (cd $TPM_TOOLS && LDFLAGS="-L/usr/local/lib" ./configure --prefix=/usr/local && make && make install)
}

install_openssl_tpm_engine() {
  (cd $OPENSSL_TPM_ENGINE && LDFLAGS="-L/usr/local/lib" ./configure --with-openssl=/usr/local/ssl && make && make install)
  if [ -d /etc/ld.so.conf.d ]; then
    echo /usr/local/lib/openssl/engines > /etc/ld.so.conf.d/openssl-engines.conf
  fi
  ldconfig
  export LD_LIBRARY_PATH=/usr/local/lib/openssl/engines
}

install_hex2bin() {
    (cd $HEX2BIN && make && make install)
}

#install_openssl
#install_trousers
install_tpm_tools
#install_openssl_tpm_engine
#install_hex2bin


# COMPILE
#gcc  -lcrypto -ltspi -o target/create_tpm_key2 create_tpm_key2.c
gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_bindaeskey tpm_bindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c -lcrypto -ltspi
gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_unbindaeskey tpm_unbindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi
gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_signdata  tpm_signdata.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto –ltspi
gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_createkey tpm_createkey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto –ltspi
