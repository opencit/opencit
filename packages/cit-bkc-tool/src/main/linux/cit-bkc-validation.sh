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

# TERM_DISPLAY_MODE can be "plain" or "color"
TERM_DISPLAY_MODE=color
TERM_STATUS_COLUMN=60
TERM_COLOR_GREEN="\\033[1;32m"
TERM_COLOR_CYAN="\\033[1;36m"
TERM_COLOR_RED="\\033[1;31m"
TERM_COLOR_YELLOW="\\033[1;33m"
TERM_COLOR_NORMAL="\\033[0;39m"

  # if the user has passed in the reboot count, use it. Or else set it to 1 as default.
  if [ -z "$1" ]; then
    reboot_count="1"
  else
    reboot_count="$1"
  fi

echo "### Started CIT BKC validation ($reboot_count)" >> $LOG_FILE

#  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_GREEN}"; fi
#  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi
#  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_RED}"; fi
#  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_YELLOW}"; fi


write_to_report_file() {
  local output_message="$*"
  current_date=`date +%Y-%m-%d:%H:%M:%S`
  # write to log file for debugging
  #echo -e "$bkc_test_name: $output_message"
  echo -e "[$current_date] $bkc_test_name: $output_message" >> $LOG_FILE
  # do NOT insert $bkc_test_name when writing to the .report file
  echo -e $output_message > $CIT_BKC_DATA_PATH/${bkc_test_name}.report
}

# records a successful test result from $bkc_test_name and given message
result_ok() {
  # write to console for interactive use
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_GREEN}"; fi
  echo -e "$bkc_test_name: OK - $*"
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi

  write_to_report_file "OK - $*"
  bkc_test_name=""
  return 0
}

# records an error result from $bkc_test_name and given message
result_error() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_RED}"; fi
  echo -e "$bkc_test_name: ERROR - $*"
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi

  write_to_report_file "ERROR - $*"
  bkc_test_name=""
  return 1
}

# records a skipped-test result from $bkc_test_name and given message
result_skip() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_YELLOW}"; fi
  echo -e "$bkc_test_name: SKIP - $*"
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi

  write_to_report_file "SKIP - $*"
  bkc_test_name=""
  return 2
}

# records a reboot-required result from $bkc_test_name and given message
result_reboot() {
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_GREEN}"; fi
  echo -e "$bkc_test_name: REBOOT - $*"
  if [ "$TERM_DISPLAY_MODE" = "color" ]; then echo -en "${TERM_COLOR_NORMAL}"; fi

  write_to_report_file "REBOOT - $*"
  bkc_test_name=""
  CIT_BKC_VALIDATION_REBOOT_REQUIRED=yes
  return 255
}

is_command_available() {
  which $* > /dev/null 2>&1
}  


# Determine the TPM Hardware support
test_tpm_support() {
  #bkc_test_name="tpm_support"
  if [ -e "/dev/tpm0" ]; then
    result_ok "TPM is supported."
    return $?
  else
    result_error "TPM is not supported."
    return $?
  fi
}


test_txtstat_present() {
  if is_command_available txt-stat; then
    result_ok "txt-stat is present."
    return $?
  else
    result_error "txt-stat is missing."
    return $?
  fi
}


# identify tpm version
# postcondition:
#   variable TPM_VERSION is set to 1.2 or 2.0
test_tpm_version() {
  export TPM_VERSION
  if [[ -f "/sys/class/misc/tpm0/device/caps" || -f "/sys/class/tpm/tpm0/device/caps" ]]; then
    TPM_VERSION=1.2
    result_ok "TPM 1.2"
    return $?
  elif [[ -f "/sys/class/tpm/tpm0/device/description" && `cat /sys/class/tpm/tpm0/device/description` == "TPM 2.0 Device" ]]; then
    TPM_VERSION=2.0
    result_ok "TPM 2.0"
    return $?
  elif [[ -n $(txt-stat | grep "TPM: discrete TPM2.0" | head -n 1) ]]; then
    TPM_VERSION=2.0
    result_ok "TPM 2.0"
    return $?
  else
    TPM_VERSION=
    result_error "Unknown TPM version"
    return $?
  fi
}


# depends on test_tpm_version to run first
test_tpm_ownership() {
  #bkc_test_name="tpm_ownership"
  if [ "$TPM_VERSION" == "1.2" ]; then
    local tpm_owned=$(cat /sys/class/misc/tpm0/device/owned 2>/dev/null)
    if [ "$tpm_owned" == 1 ]; then
      result_ok "TPM is owned."
      return $?
    else
      result_error "TPM is not owned."
      return $?
    fi
  elif [ "$TPM_VERSION" == "2.0" ]; then
    local tpm2_owned=$(/opt/trustagent/bin/tpm2-isowned 2>/dev/null)
    if [ "$tpm2_owned" == "1" ]; then
      result_ok "TPM is owned."
      return $?
    else
      result_error "TPM is not owned."
      return $?
    fi
  fi
}

# Determine the TXT Hardware support
test_txt_support() {
  #bkc_test_name="txt_support"
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
  #bkc_test_name="aik_present"
  AIKCertFile="/opt/trustagent/configuration/aik.pem"
  if [ -f $AIKCertFile ]; then
    result_ok "AIK certificate exists."
    return $?
  else
    result_error "AIK certificate '$AIKCertFile' does not exist."
    return $?
  fi
}

# Determine if binding key is present
test_bindingkey_present() {
  #bkc_test_name="bindingkey_present"
  BindingKeyFile="/opt/trustagent/configuration/bindingkey.pem"
  if [ -f $BindingKeyFile ]; then
    result_ok "Binding key certificate exists."
    return $?
  else
    result_error "Binding key certificate '$BindingKeyFile' does not exist."
    return $?
  fi
}

# Determine if signing key is present
test_signingkey_present() {
  #bkc_test_name="signingkey_present"
  SigningKeyFile="/opt/trustagent/configuration/signingkey.pem"
  if [ -f $SigningKeyFile ]; then
    result_ok "Signing key certificate exists."
    return $?
  else
    result_error "Signing key certificate '$SigningKeyFile' does not exist."
    return $?
  fi
}

# Determine if NV index is defined for asset tag configuration
test_nvindex_defined() {
  #bkc_test_name="nvindex_defined"
  if [ "$TPM_VERSION" == "1.2" ]; then
    local indexDefined=$(tpm_nvinfo -i "$ASSET_TAG_NVRAM_INDEX" 2>/dev/null)
    if [ -n "$indexDefined" ]; then
      result_ok "NV index defined."
      return $?
    else 
      result_error "NV index not defined. Asset tags cannot be configured."
      return $?
    fi
  elif [ "$TPM_VERSION" == "2.0" ]; then
    local indexDefined=$(/opt/trustagent/bin/tpm2-nvindex-exists.sh 0x40000001 2>/dev/null)
    if [ "$indexDefined" == "1" ]; then
      result_ok "NV index defined."
      return $?
    else
      result_error "NV index not defined. Asset tags cannot be configured."
      return $?
    fi
  fi
}

# Creates the whitelist and registers the same
test_create_whitelist() {
  whitelist_data_file="create_whitelist.data"
  whitelist_http_status_file="create_whitelist_http.status"
  #bkc_test_name="create_whitelist"

  #-s option removes the progress meter
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/json" \
    -H "accept: application/json" \
    -X POST \
    -d  '{"wl_config":{"add_bios_white_list":"true","add_vmm_white_list":"true","bios_white_list_target":"BIOS_HOST","vmm_white_list_target":"VMM_HOST","bios_pcrs":"0,17","vmm_pcrs":"18","register_host":"true","overwrite_whitelist": "true","bios_mle_name":"","vmm_mle_name":"","txt_host_record":{"host_name":"127.0.0.1","add_on_connection_string":"intel:https://127.0.0.1:1443","tls_policy_choice": {"tls_policy_id":"TRUST_FIRST_CERTIFICATE"}}}}' \
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
  host_attestation_result_file="host_attestation_result_$reboot_count.data"
  #bkc_test_name="write_assettag"

  if [ -f "$CIT_BKC_DATA_PATH/${bkc_test_name}.report" ]; then
    local last_status=$(head -n 1 "$CIT_BKC_DATA_PATH/${bkc_test_name}.report" | awk '{print $1}')
    if [ "$last_status" == "REBOOT" ]; then
       test_host_attestation_status 
       local asset_tag_trusted=$(grep 'AssetTag Trusted' "$CIT_BKC_DATA_PATH/$host_attestation_result_file" | awk '{print $3 }')
       if [ "$asset_tag_trusted" == "true" ]; then
         result_ok "AssetTag validated."
         return $?
       else
         result_error "AssetTag validation failed."
         return $?
       fi
    fi
  fi
  
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/xml" \
    https://127.0.0.1:8443/mtwilson/v2/hosts?nameEqualTo=127.0.0.1 \
    1>$CIT_BKC_DATA_PATH/$assettag_data_file 2>$CIT_BKC_DATA_PATH/$assettag_http_status_file
  
  result=$(cat $CIT_BKC_DATA_PATH/$assettag_http_status_file | grep "200 OK")
  if [ -z "$result" ]; then
    result_error "Error during retrieval of hardware UUID."
    return $?
  fi
  echo "Successfully called into CIT to retrieve the hardware UUID of the host." >> $LOG_FILE
  
  hostHardwareUuid=$(xmlstarlet sel -t -v "(/host_collection/hosts/host/hardwareUuid)" $CIT_BKC_DATA_PATH/$assettag_data_file)
  #hostHardwareUuid=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "hardwareUuid" | sed -n 's/.*<hardwareUuid> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the hardware UUID of the host - $hostHardwareUuid" >> $LOG_FILE
  
  hostUuid=$(xmlstarlet sel -t -v "(/host_collection/hosts/host/id)" $CIT_BKC_DATA_PATH/$assettag_data_file)
  #hostUuid=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "<id>" | sed -n 's/.*<id> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the UUID of the host - $hostUuid" >> $LOG_FILE

  #Now that we have the hardware UUID, create the asset tag certificate
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/xml" \
    -H "accept: application/xml" \
    -X POST \
    -d "<?xml version=\"1.0\" encoding=\"UTF-8\"?><selections xmlns=\"urn:mtwilson-tag-selection\"><selection><subject><uuid>$hostHardwareUuid</uuid></subject><attribute oid=\"2.5.4.789.1\"><text>Country=US</text></attribute><attribute oid=\"2.5.4.789.1\"><text>State=CA</text></attribute><attribute oid=\"2.5.4.789.1\"><text>City=Folsom</text></attribute></selection></selections>" \
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
    -d "{\"certificate_id\":\"$certificateId\",\"host\":\"$hostUuid\"}" \
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
# NOTE: the $host_attestation_result_file is used by the test_write_assettag
test_host_attestation_status() {
  host_attestation_data_file="host_attestation_$reboot_count.data"
  host_attestation_http_status_file="host_attestation_http_$reboot_count.status"
  host_attestation_result_file="host_attestation_result_$reboot_count.data"
  
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

  echo "BIOS Trusted: $BIOS_status" >> $host_attestation_result_file
  echo "VMM Trusted: $VMM_status" >> $host_attestation_result_file
  echo "AssetTag Trusted: $Asset_Tag_status" >> $host_attestation_result_file
  echo "Overall Trusted: $Trust_status" >> $host_attestation_result_file
  if [ "$Trust_status" == "true" ]; then
    result_ok "Host is trusted";
    return $?
  else
    result_error "Host is not trusted: BIOS=$BIOS_status, VMM=$VMM_status, AssetTag=$Asset_Tag_status"
    return $?
  fi
}

# input: list of tests to run
run_tests() {
  local result
  local failed=""

  for testname in $*
  do
    bkc_test_name="$testname"
    # echo "Running test: $testname"
    if [ -n "$failed" ]; then
      result_skip "depends on $failed"
    else
      # security note: this is safe because we are hard-coding test sequence above; there is no user input in $testname
      eval "test_$testname"
      result=$?
      if [ $result -eq 1 ]; then
        # when a test fails, we record the test name then skip rest of tests (see above)
        failed="$testname"
      elif [ $result -eq 255 ]; then
        # reboot required
        return $result
      fi
    fi
    #if [ $result -ne 0 ]; then return $result; fi
  done

  if [ -n "$failed" ]; then return 1; fi
  return 0
}

main(){
  PLATFORM_TESTS="txt_support txtstat_present tpm_support tpm_version tpm_ownership"
  CIT_TPM12_TESTS="aik_present bindingkey_present signingkey_present"
  CIT_TPM20_TESTS="aik_present"
  CIT_FUNCTIONAL_TESTS="create_whitelist write_assettag nvindex_defined host_attestation_status"

  run_tests $PLATFORM_TESTS
  result=$?
  if [ $result -ne 0 ]; then return $result; fi

  if [ "$TPM_VERSION" == "1.2" ]; then
    run_tests $CIT_TPM12_TESTS
    result=$?
    if [ $result -ne 0 ]; then return $result; fi
  elif [ "$TPM_VERSION" == "2.0" ]; then
    run_tests $CIT_TPM20_TESTS
    result=$?
    if [ $result -ne 0 ]; then return $result; fi
  fi
  run_tests $CIT_FUNCTIONAL_TESTS
  result=$?
  if [ $result -ne 0 ]; then return $result; fi

  return 0
}

main "$@"
result=$?

if [ "$CIT_BKC_VALIDATION_REBOOT_REQUIRED" == "yes" ]; then
  exit 255
else
  exit $result
fi
