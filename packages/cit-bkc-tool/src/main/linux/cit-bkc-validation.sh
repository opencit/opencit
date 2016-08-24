#!/bin/bash

# Every test MUST:
# * set variable "bkc_test_name" to the test name (for EXAMPLE "tpm_support")
#   this affects where output is written
# * return 0 on success
# * return 1 on error
# * return 255 on reboot required (for 2-part tests)

# Exit codes:
# 0 on success
# 1 on error while running any test
# 255 on reboot required from any test

CIT_BKC_DATA_PATH=${CIT_BKC_DATA_PATH:-/usr/local/var/cit-bkc-tool/data}
LOG_FILE=$CIT_BKC_DATA_PATH/validation.log
mkdir -p $CIT_BKC_DATA_PATH

CIT_BKC_VALIDATION_REBOOT_REQUIRED=no

ASSET_TAG_NVRAM_INDEX=0x40000011

  # if the user has passed in the reboot count, use it. Or else set it to 1 as default.
  if [ -z "$1" ]; then
    reboot_count="1"
  else
    reboot_count="$1"
  fi

echo "### Started CIT BKC validation ($reboot_count)" >> $LOG_FILE


write_to_report_file() {
  local output_message="$*"
  current_date=`date +%Y-%m-%d:%H:%M:%S`
  echo -e $output_message
  echo -e "# $current_date" >> $LOG_FILE
  echo -e $output_message >> $LOG_FILE
  echo -e $output_message > $CIT_BKC_DATA_PATH/${bkc_test_name}.report
}

# records a successful test result from $bkc_test_name and given message
result_ok() {
  write_to_report_file "OK - $*"
  bkc_test_name=""
  return 0
}

# records an error result from $bkc_test_name and given message
result_error() {
  write_to_report_file "ERROR - $*"
  bkc_test_name=""
  return 1
}

# records a reboot-required result from $bkc_test_name and given message
result_reboot() {
  write_to_report_file "REBOOT - $*"
  bkc_test_name=""
  CIT_BKC_VALIDATION_REBOOT_REQUIRED=yes
  return 255
}

# Determine the TPM Hardware support
test_tpm_support() {
  bkc_test_name="tpm_support"
  if [ -e "/dev/tpm0" ]; then
    result_ok "TPM is supported."
    return $?
  else
    result_error "TPM is not supported."
    return $?
  fi
}

test_tpm_ownership() {
  bkc_test_name="tpm_ownership"
  if [[ "$(cat /sys/class/misc/tpm0/device/owned)" == 1 ]]; then
    result_ok "TPM is owned."
    return $?
  else
    result_error "TPM is not owned."
    return $?
  fi
}

# Determine the TXT Hardware support
test_txt_support() {
  bkc_test_name="txt_support"
  TXT=$(cat /proc/cpuinfo | grep -o "smx" | head -1)
  if [ "$TXT" == "smx" ]; then
    result_ok "TXT is supported."
    return $?
  else
    result_error "TXT is not supported"
    return $?
  fi
}

# Determine is AIK is present
test_aik_present() {
  bkc_test_name="aik_present"
  AIKCertFile="/opt/trustagent/configuration/aik.pem"
  if [ -f $AIKCertFile ]; then
    result_ok "AIK certificate exists."
    return $?
  else
    result_error "AIK certificate ($AIKCertFile) does not exist."
    return $?
  fi
}

# Determine if binding key is present
test_bindingkey_present() {
  bkc_test_name="bindingkey_present"
  BindingKeyFile="/opt/trustagent/configuration/bindingkey.pem"
  if [ -f $BindingKeyFile ]; then
    result_ok "Binding key certificate exists."
    return $?
  else
    result_error "Binding key certificate ($BindingKeyFile) does not exist."
    return $?
  fi
}

# Determine if signing key is present
test_signingkey_present() {
  bkc_test_name="signingkey_present"
  SigningKeyFile="/opt/trustagent/configuration/signingkey.pem"
  if [ -f $SigningKeyFile ]; then
    result_ok "Signing key certificate exists."
    return $?
  else
    result_error "Signing key certificate ($SigningKeyFile) does not exist."
    return $?
  fi
}

# Determine if NV index is defined for asset tag configuration
test_nvindex_defined() {
  bkc_test_name="nvindex_defined"
  indexDefined=$(tpm_nvinfo -i "$ASSET_TAG_NVRAM_INDEX" 2>/dev/null)
  if [ -n "$indexDefined" ]; then
    result_ok "NV index defined."
    return $?
  else 
    result_error "NV index not defined. Asset tags cannot be configured."
    return $?
  fi
}

# Creates the whitelist and registers the same
test_create_whitelist() {
  whitelist_data_file="create_whitelist.data"
  whitelist_http_status_file="create_whitelist_http.status"
  bkc_test_name="create_whitelist"

  #-s option removes the progress meter
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/json" \
    -H "accept: application/json" \
    -X POST \
    -d  '{"wl_config":{"add_bios_white_list":"true","add_vmm_white_list":"true","bios_white_list_target":"BIOS_HOST","vmm_white_list_target":"VMM_HOST","bios_pcrs":"0,17","vmm_pcrs":"18,19","register_host":"true","overwrite_whitelist": "true","bios_mle_name":"","vmm_mle_name":"","txt_host_record":{"host_name":"127.0.0.1","add_on_connection_string":"intel:https://127.0.0.1:1443","tls_policy_choice": {"tls_policy_id":"TRUST_FIRST_CERTIFICATE"}}}}' \
    https://127.0.0.1:8443/mtwilson/v2/rpc/create-whitelist-with-options \
    1>$CIT_BKC_DATA_PATH/$whitelist_data_file 2>$CIT_BKC_DATA_PATH/$whitelist_http_status_file

  result=$(cat $CIT_BKC_DATA_PATH/$whitelist_http_status_file | grep "200 OK" )
  if [ -n "$result" ]; then
    result_ok "Create whitelist with host registration successful."
    return $?
  else
    result_error "Error during whitelisting & registration."
    return $?
  fi
}

# Creates the asset tag certificate by retrieving the hardware UUID and then pushes the certificate to the target host.
test_write_assettag() {
  assettag_data_file="write_assettag.data"
  assettag_http_status_file="write_assettag_http.status"
  certificate_date_file="certificate.data"
  certificate_http_status_file="certificate_http.status"
  bkc_test_name="write_assettag"
  
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/xml" \
    https://127.0.0.1:8443/mtwilson/v2/hosts?nameEqualTo=127.0.0.1 \
    1>$CIT_BKC_DATA_PATH/$assettag_data_file 2>$CIT_BKC_DATA_PATH/$assettag_http_status_file
  
  result=$(cat $CIT_BKC_DATA_PATH/$assettag_http_status_file | grep "200 OK")
  if [ -z "$result" ]; then
    result_error "Error during retrieval of hardware UUID."
    return $?
  fi
  echo "Successfully called into CIT to retrieve the hardware UUID of the host."
  
  hostHardwareUuid=$(xmlstarlet sel -t -v "(/host_collection/hosts/host/hardwareUuid)" $CIT_BKC_DATA_PATH/$assettag_data_file)
  #hostHardwareUuid=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "hardwareUuid" | sed -n 's/.*<hardwareUuid> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the hardware UUID of the host - $hostHardwareUuid"
  
  hostUuid=$(xmlstarlet sel -t -v "(/host_collection/hosts/host/id)" $CIT_BKC_DATA_PATH/$assettag_data_file)
  #hostUuid=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "<id>" | sed -n 's/.*<id> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the UUID of the host - $hostUuid"

  #Now that we have the hardware UUID, create the asset tag certificate
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/xml" \
    -H "accept: application/xml" \
    -X POST \
    -d '<?xml version="1.0" encoding="UTF-8"?><selections xmlns="urn:mtwilson-tag-selection"><selection><subject><uuid>$hostHardwareUuid</uuid></subject><attribute oid="2.5.4.789.1"><text> Country=US</text></attribute><attribute oid="2.5.4.789.1"><text>State=CA</text></attribute><attribute oid="2.5.4.789.1"><text> City=Folsom</text></attribute></selection></selections>' \
    https://127.0.0.1:8443/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=$hostHardwareUuid \
    1>$CIT_BKC_DATA_PATH/$certificate_date_file 2>$CIT_BKC_DATA_PATH/$certificate_http_status_file

  result=$(cat $CIT_BKC_DATA_PATH/$certificate_http_status_file | grep "200 OK" )
  if [ -z "$result" ]; then
    result_error "Error during creation the asset tag certificate for host with hardware UUID $hostHardwareUuid."
    return $?
  fi
  echo "Successfully created the asset tag certificate for host with hardware UUID $hostHardwareUuid."

  certificateId=$(xmlstarlet sel -t -v "(/certificate/id)" $CIT_BKC_DATA_PATH/$certificate_date_file)
  #certificateId=$(cat $CIT_BKC_DATA_PATH/$certificate_date_file | grep "<id>" | sed -n 's/.*<id> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the certificate id - $certificateId"
  
  #Push the tag
  #We are using the -a tag to append to the file instead of overwriting it.
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/json" \
    -H "accept: application/json" \
    -X POST \
    -d '{"certificate_id":"$certificateId","host":"$hostUuid"}' \
    https://127.0.0.1:8443/mtwilson/v2/rpc/deploy-tag-certificate \
    1>$CIT_BKC_DATA_PATH/$assettag_data_file 2>$CIT_BKC_DATA_PATH/$assettag_http_status_file
  
  result=$(cat $CIT_BKC_DATA_PATH/$assettag_http_status_file | grep "200 OK" )
  if [ -z "$result" ]; then
    result_error "Error during pushing the asset tag certificate for host."
    return $?
  fi
  result_reboot "Successfully pushed the asset tag certificate to host. Reboot required to complete the test."
  return $?
}

# Gets the attestation status of the host. Reboot counter would be appended to the file name to compare the
# status of the host after reboot.
test_host_attestation_status() {
  host_attestation_data_file="host_attestation_$reboot_count.data"
  host_attestation_http_status_file="host_attestation_http_$reboot_count.status"
  bkc_test_name="host_attestation_$reboot_count"
  
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/json" \
    -H "accept: application/samlassertion+xml" \
    -X POST \
    -d '{"host_uuid":"HostID"}' \
    https://127.0.0.1:8443/mtwilson/v2/host-attestations \
    1>$CIT_BKC_DATA_PATH/$host_attestation_data_file 2>$CIT_BKC_DATA_PATH/$host_attestation_http_status_file

  result=$(cat $CIT_BKC_DATA_PATH/$assettag_http_status_file | grep "200 OK" )
  if [ -z "$result" ]; then
    result_error "Error retrieving the attestation information for the host."
    return $?
  fi
  echo "Successfully retrieved the attestation information for the host."
  
  #Extract the results of the attestation and write it to the report file. Since there will be new lines and white spaces
  #we need to remove them as well.
  Trust_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Trusted'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  BIOS_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Trusted_BIOS'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  VMM_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Trusted_VMM'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  Asset_Tag_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Asset_Tag'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')

  # TODO: we need to check for trusted values, since we are whitelisting localhost if it comes back as untrusted there is a problem
  result_ok "BIOS Trust status:$BIOS_status\nVMM Trust Status:$VMM_status\nAsset Tag Trust status:$Asset_Tag_status"
  return $?
}

main(){

  TEST_SEQUENCE="tpm_support tpm_ownership txt_support bindingkey_present signingkey_present nvindex_defined create_whitelist write_assettag host_attestation_status"
  local result

  for testname in $TEST_SEQUENCE
  do
    echo "Running test: $testname"
    # security note: this is safe because we are hard-coding test sequence above; there is no user input in $testname
    eval "test_$testname"
    result=$?
    if [ $result -ne 0 ]; then return $result; fi
  done

  return 0
}
main "$@"
result=$?

if [ "$CIT_BKC_VALIDATION_REBOOT_REQUIRED" == "yes" ]; then
  exit 255
else
  exit $result
fi
