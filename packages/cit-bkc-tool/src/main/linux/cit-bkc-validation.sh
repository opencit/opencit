#!/bin/bash
CIT_BKC_DATA_PATH=${CIT_BKC_DATA_PATH:-/usr/local/var/cit-bkc-tool/data}
LOG_FILE=$CIT_BKC_DATA_PATH/validation.log
mkdir -p $CIT_BKC_DATA_PATH

ASSET_TAG_NVRAM_INDEX=0x40000010

touch $LOG_FILE >/dev/null 2>&1
if [ -f $LOG_FILE ] && [ ! -w $LOG_FILE ]; then
  echo "Cannot write to log file: $LOG_FILE"
  exit 1
fi

echo `date +%Y-%m-%d:%H:%M:%S` >> $LOG_FILE;

test_tpm_support() {
  file=$(ls --color=none /dev/tpm0)
  if [ "$file" = "/dev/tpm0" ]; then
    echo "TPM is supported."
  else
    echo "TPM is not supported."
    echo "TPM is not supported." >> $LOG_FILE
  fi
}

test_tpm_ownership() {
  if [[ "$(cat /sys/class/misc/tpm0/device/owned)" == 1 ]]; then
    echo "TPM is owned."
  else
    echo "TPM is not owned."
    echo "TPM is not owned." >> $LOG_FILE
  fi
}

test_txt_support() {
  TXT=$(cat /proc/cpuinfo | grep -o "smx" | head -1)
  if [ $TXT = "smx" ]; then
    echo "TXT is supported."
  else
    echo "TXT is not supported"
    echo "TXT is not supported" >> $LOG_FILE
  fi
}

test_aik_present() {
  AIKCertFile="/opt/trustagent/configuration/aik.pem"
  if [ -f $AIKCertFile ]; then
    echo "AIK certificate exists."
  else
    echo "AIK certificate ($AIKCertFile) does not exist."
    echo "AIK certificate ($AIKCertFile) does not exist." >> $LOG_FILE
  fi
}

test_bindingkey_present() {
  BindingKeyFile="/opt/trustagent/configuration/bindingkey.pem"
  if [ -f $BindingKeyFile ]; then
    echo "Binding key certificate exists."
  else
    echo "Binding key certificate ($BindingKeyFile) does not exist."
    echo "Binding key certificate ($BindingKeyFile) does not exist." >> $LOG_FILE
  fi
}

test_signingkey_present() {
  SigningKeyFile="/opt/trustagent/configuration/signingkey.pem"
  if [ -f $SigningKeyFile ]; then
    echo "Signing key certificate exists."
  else
    echo "Signing key certificate ($SigningKeyFile) does not exist."
    echo "Signing key certificate ($SigningKeyFile) does not exist." >> $LOG_FILE
  fi
}

test_nvindex_defined() {
  indexDefined=$(tpm_nvinfo -i "$ASSET_TAG_NVRAM_INDEX" 2>/dev/null)
  if [ -n "$indexDefined" ]; then
    echo "NV index defined."
  else
    echo "NV index not defined. Asset tags cannot be configured."
    echo "NV index not defined. Asset tags cannot be configured." >> $LOG_FILE
  fi
}

test_create_whitelist() {
  
}

#test_register_host() {}

#test_write_assettag() {}

#test_get_pcr0() {}

#test_create_assettag() {}

main(){
  test_tpm_support
  test_tpm_ownership
  test_txt_support
  test_bindingkey_present
  test_signingkey_present
  test_nvindex_defined
}
main "$@"

