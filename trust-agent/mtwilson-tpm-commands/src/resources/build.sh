#!/bin/bash

OPENSSL=openssl-1.0.2a
TROUSERS=trousers-0.3.13
OPENSSL_TPM_ENGINE=openssl_tpm_engine-0.4.2
TPM_TOOLS=tpm-tools-1.3.8
HEX2BIN=hex2bin-master

install_openssl() {
  OPENSSL_FILE=`find ${OPENSSL}*gz`
  echo "openssl: $OPENSSL_FILE"
  if [ -n "$OPENSSL_FILE" ] && [ -f "$OPENSSL_FILE" ]; then
    tar fxz $OPENSSL_FILE
    (cd $OPENSSL && ./config --shared && make && sudo -n make install)
    if [ -d /etc/ld.so.conf.d ]; then
      echo /usr/local/ssl/lib/ | sudo -n tee /etc/ld.so.conf.d/openssl.conf
    fi
    sudo -n ldconfig
  fi
}

install_trousers() {
  TROUSERS_FILE=`find ${TROUSERS}*gz`
  echo "trousers: $TROUSERS_FILE"
  if [ -n "$TROUSERS_FILE" ] && [ -f "$TROUSERS_FILE" ]; then
    tar fxz $TROUSERS_FILE
    (cd $TROUSERS && ./configure --prefix=/usr/local --with-openssl=/usr/local/ssl && make && sudo -n make install)
    if [ -d /etc/ld.so.conf.d ]; then
      echo /usr/local/lib | sudo -n tee /etc/ld.so.conf.d/trousers.conf
    fi
    sudo -n ldconfig
  fi
}

install_tpm_tools() {
  TPM_TOOLS_FILE=`find ${TPM_TOOLS}*gz`
  echo "tpm-tools: $TPM_TOOLS_FILE"
  if [ -n "$TPM_TOOLS_FILE" ] && [ -f "$TPM_TOOLS_FILE" ]; then
    tar fxz $TPM_TOOLS_FILE
    patch $TPM_TOOLS/src/tpm_mgmt/tpm_nvread.c tpm-tools-1.3.8_src_tpm_nvread.patch
    (cd $TPM_TOOLS && LDFLAGS="-L/usr/local/lib" ./configure --prefix=/usr/local && make && sudo -n make install)
  fi
}

install_openssl_tpm_engine() {
  OPENSSL_TPM_ENGINE_FILE=`find ${OPENSSL_TPM_ENGINE}*gz`
  echo "openssl-tpm-engine: $OPENSSL_TPM_ENGINE_FILE"
  if [ -n "$OPENSSL_TPM_ENGINE_FILE" ] && [ -f "$OPENSSL_TPM_ENGINE_FILE" ]; then
    tar fxz $OPENSSL_TPM_ENGINE_FILE
    (cd $OPENSSL_TPM_ENGINE && LDFLAGS="-L/usr/local/lib" ./configure --with-openssl=/usr/local/ssl && make && sudo -n make install)
    if [ -d /etc/ld.so.conf.d ]; then
      echo /usr/local/lib/openssl/engines | sudo -n tee /etc/ld.so.conf.d/openssl-engines.conf
    fi
    sudo -n ldconfig
    export LD_LIBRARY_PATH=/usr/local/lib/openssl/engines
  fi
}

install_hex2bin() {
  HEX2BIN_FILE=`find ${HEX2BIN}*zip`
  echo "hex2bin: $HEX2BIN"
  if [ -n "$HEX2BIN_FILE" ] && [ -f "$HEX2BIN_FILE" ]; then
    unzip $HEX2BIN_FILE
    (cd $HEX2BIN && make && sudo -n make install)
  fi
}

compile_create_tpm_key2() {
  gcc  -lcrypto -ltspi -o create_tpm_key2 create_tpm_key2.c
  #cp create_tpm_key2 /usr/local/bin
}

compile_tpm_bindaeskey() {
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_bindaeskey tpm_bindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c -lcrypto -ltspi
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_unbindaeskey tpm_unbindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi
  #cp tpm_bindaeskey tpm_unbindaeskey /usr/local/bin
}

compile_tpm_createkey() {
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_createkey tpm_createkey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi
  #cp tpm_createkey /usr/local/bin
}

compile_tpm_signdata() {
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_signdata  tpm_signdata.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi
  #cp tpm_signdata /usr/local/bin
}

# INSTALL
install_openssl
install_trousers
install_tpm_tools
install_openssl_tpm_engine
install_hex2bin

# COMPILE
#compile_create_tpm_key2
compile_tpm_createkey
compile_tpm_signdata
compile_tpm_bindaeskey

