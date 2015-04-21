#!/bin/bash

OPENSSL_URL=http://openssl.org/source/openssl-1.0.2.tar.gz
TROUSERS_URL=http://downloads.sourceforge.net/project/trousers/trousers/0.3.13/trousers-0.3.13.tar.gz
TROUSERS_OPENSSL_TPM_ENGINE_URL=http://downloads.sourceforge.net/project/trousers/OpenSSL%20TPM%20Engine/0.4.2/openssl_tpm_engine-0.4.2.tar.gz
TPM_TOOLS_URL=http://downloads.sourceforge.net/project/trousers/tpm-tools/1.3.8/tpm-tools-1.3.8.tar.gz
HEX2BIN_URL=https://github.com/jbuhacoff/hex2bin/archive/master.zip

OPENSSL=openssl-1.0.2
TROUSERS=trousers-0.3.13
OPENSSL_TPM_ENGINE=openssl_tpm_engine-0.4.2
TPM_TOOLS=tpm-tools-1.3.8
HEX2BIN=hex2bin-master

download_files() {
  wget $OPENSSL_URL
  wget $TROUSERS_URL
  wget $TROUSERS_OPENSSL_TPM_ENGINE_URL
  wget $TPM_TOOLS_URL
  wget $HEX2BIN_URL -O $HEX2BIN.zip
}

install_openssl() {
  OPENSSL_FILE=`find ${OPENSSL}*gz`
  echo "openssl: $OPENSSL_FILE"
  if [ -n "$OPENSSL_FILE" ] && [ -f "$OPENSSL_FILE" ]; then
    tar fxz $OPENSSL_FILE
    (cd $OPENSSL && ./config --shared && make && make install)
    if [ -d /etc/ld.so.conf.d ]; then
      echo /usr/local/ssl/lib/ > /etc/ld.so.conf.d/openssl.conf
    fi
    ldconfig
  fi
}

install_trousers() {
  TROUSERS_FILE=`find ${TROUSERS}*gz`
  echo "trousers: $TROUSERS_FILE"
  if [ -n "$TROUSERS_FILE" ] && [ -f "$TROUSERS_FILE" ]; then
    tar fxz $TROUSERS_FILE
    (cd $TROUSERS && ./configure --prefix=/usr/local --with-openssl=/usr/local/ssl && make && make install)
    if [ -d /etc/ld.so.conf.d ]; then
      echo /usr/local/lib > /etc/ld.so.conf.d/trousers.conf
    fi
    ldconfig
  fi
}

install_tpm_tools() {
  TPM_TOOLS_FILE=`find ${TPM_TOOLS}*gz`
  echo "tpm-tools: $TPM_TOOLS_FILE"
  if [ -n "$TPM_TOOLS_FILE" ] && [ -f "$TPM_TOOLS_FILE" ]; then
    tar fxz $TPM_TOOLS_FILE
    patch $TPM_TOOLS/src/tpm_mgmt/tpm_nvread.c tpm-tools-1.3.8_src_tpm_nvread.patch
    (cd $TPM_TOOLS && LDFLAGS="-L/usr/local/lib" ./configure --prefix=/usr/local && make && make install)
  fi
}

install_openssl_tpm_engine() {
  OPENSSL_TPM_ENGINE_FILE=`find ${OPENSSL_TPM_ENGINE}*gz`
  echo "openssl-tpm-engine: $OPENSSL_TPM_ENGINE_FILE"
  if [ -n "$OPENSSL_TPM_ENGINE_FILE" ] && [ -f "$OPENSSL_TPM_ENGINE_FILE" ]; then
    tar fxz $OPENSSL_TPM_ENGINE_FILE
    (cd $OPENSSL_TPM_ENGINE && LDFLAGS="-L/usr/local/lib" ./configure --with-openssl=/usr/local/ssl && make && make install)
    if [ -d /etc/ld.so.conf.d ]; then
      echo /usr/local/lib/openssl/engines > /etc/ld.so.conf.d/openssl-engines.conf
    fi
    ldconfig
    export LD_LIBRARY_PATH=/usr/local/lib/openssl/engines
  fi
}

install_hex2bin() {
  HEX2BIN_FILE=`find ${HEX2BIN}*zip`
  echo "hex2bin: $HEX2BIN"
  if [ -n "$HEX2BIN_FILE" ] && [ -f "$HEX2BIN_FILE" ]; then
    unzip $HEX2BIN_FILE
    (cd $HEX2BIN && make && make install)
  fi
}

install_create_tpm_key2() {
  gcc  -lcrypto -ltspi -o create_tpm_key2 create_tpm_key2.c
  cp create_tpm_key2 /usr/local/bin
}

install_tpm_createkey() {
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_createkey tpm_createkey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi 
  cp tpm_createkey /usr/local/bin
}

install_tpm_bindaeskey() {
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_bindaeskey tpm_bindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c -lcrypto -ltspi 
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_unbindaeskey tpm_unbindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi 
  cp tpm_bindaeskey tpm_unbindaeskey /usr/local/bin
}

install_tpm_signdata() {
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_createsigningkey tpm_createsigningkey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c -lcrypto -ltspi 
  gcc -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -o tpm_signdata  tpm_signdata.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi 
  cp tpm_signdata /usr/local/bin
}

# look for /usr/bin/openssl, return 0 if found, return 1 if not found
detect_openssl() {
  openssl_bin=`which openssl`
  if [ -n "$openssl_bin" ]; then return 0; fi
  return 1
}

# look for /usr/sbin/tcsd, return 0 if found, return 1 if not found
detect_trousers() {
  trousers_bin=`which tcsd`
  if [ -n "$trousers_bin" ]; then return 0; fi
  return 1
}

# look for /usr/sbin/tpm_version, return 0 if found, 1 if not found
detect_tpm_tools() {
  tpm_tools_bin=`which tpm_version`
  if [ -n "$tpm_tools_bin" ]; then return 0; fi
  return 1
}

# look for /usr/local/bin/hex2bin, return 0 if found, 1 if not found
detect_hex2bin() {
  hex2bin_bin=`which hex2bin`
  if [ -n "$hex2bin_bin" ]; then return 0; fi
  return 1
}

#download_files
#install_openssl
#install_trousers
#install_tpm_tools
#install_openssl_tpm_engine
#install_hex2bin
##install_create_tpm_key2
install_tpm_createkey
install_tpm_signdata
install_tpm_bindaeskey
