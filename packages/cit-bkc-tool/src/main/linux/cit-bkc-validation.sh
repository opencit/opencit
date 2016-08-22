#!/bin/bash
CIT_BKC_DATA_PATH=${CIT_BKC_DATA_PATH:-/usr/local/var/cit-bkc-tool/data}
CIT_BKC_REPORT_PATH=${CIT_BKC_DATA_PATH:-/usr/local/var/cit-bkc-tool/reports}
LOG_FILE=$CIT_BKC_DATA_PATH/validation.log
mkdir -p $CIT_BKC_DATA_PATH
mkdir -p $CIT_BKC_REPORT_PATH

ASSET_TAG_NVRAM_INDEX=0x40000011

echo `date +%Y-%m-%d:%H:%M:%S` >> $LOG_FILE;

write_to_report_file() {
  current_date=`date +%Y-%m-%d:%H:%M:%S`
  echo -e $output_message
  echo -e $output_message >> $LOG_FILE;
  echo -e $output_message > $CIT_BKC_REPORT_PATH/$report_file_name
  output_message=""
  report_file_name=""
}

# Determine the TPM Hardware support
test_tpm_support() {
  report_file_name="tpm_support.report"
  file=$(ls --color=none /dev/tpm0)
  if [ "$file" != "/dev/tpm0" ]; then
    output_message="TPM is not supported."
    write_to_report_file
    return 1
  fi
  output_message="TPM is supported."  
  write_to_report_file
}

test_tpm_ownership() {
  report_file_name="tpm_ownership.report"
  if [[ "$(cat /sys/class/misc/tpm0/device/owned)" != 1 ]]; then
    output_message="TPM is not owned."
    write_to_report_file
    return 1
  fi
  output_message="TPM is owned."
  write_to_report_file
}

# Determine the TXT Hardware support
test_txt_support() {
  report_file_name="txt_support.report"
  TXT=$(cat /proc/cpuinfo | grep -o "smx" | head -1)
  if [ "$TXT" != "smx" ]; then
    output_message="TXT is not supported"
    write_to_report_file
    return 1    
  fi
  output_message="TXT is supported."
  write_to_report_file
}

# Determine is AIK is present
test_aik_present() {
  report_file_name="aik_present.report"
  AIKCertFile="/opt/trustagent/configuration/aik.pem"
  if [ ! -f $AIKCertFile ]; then
    output_message="AIK certificate ($AIKCertFile) does not exist."
    write_to_report_file
    return 1
  fi
  output_message="AIK certificate exists."
  write_to_report_file
}

# Determine if binding key is present
test_bindingkey_present() {
  report_file_name="bindingkey_present.report"
  BindingKeyFile="/opt/trustagent/configuration/bindingkey.pem"
  if [ ! -f $BindingKeyFile ]; then
    output_message="Binding key certificate ($BindingKeyFile) does not exist."
    write_to_report_file
    return 1
  fi
  output_message="Binding key certificate exists."
  write_to_report_file
}

# Determine if signing key is present
test_signingkey_present() {
  report_file_name="signingkey_present.report"
  SigningKeyFile="/opt/trustagent/configuration/signingkey.pem"
  if [ ! -f $SigningKeyFile ]; then
    output_message="Signing key certificate ($SigningKeyFile) does not exist."
    write_to_report_file
    return 1
  fi
  output_message="Signing key certificate exists."  
  write_to_report_file
}

# Determine if NV index is defined for asset tag configuration
test_nvindex_defined() {
  report_file_name="nvindex_defined.report"
  indexDefined=$(tpm_nvinfo -i "$ASSET_TAG_NVRAM_INDEX" 2>/dev/null)
  if [ ! -n "$indexDefined" ]; then
    output_message="NV index not defined. Asset tags cannot be configured."
    write_to_report_file
    return 1
  fi
  output_message="NV index defined."
  write_to_report_file
}

# Creates the whitelist and registers the same
test_create_whitelist() {
  whitelist_data_file="create_whitelist.data"
  whitelist_http_status_file="create_whitelist_http.status"
  report_file_name="create_whitelist.report"

  #-s option removes the progress meter
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/json" \
    -H "accept: application/json" \
    -X POST \
    -d  '{"wl_config":{"add_bios_white_list":"true","add_vmm_white_list":"true","bios_white_list_target":"BIOS_HOST","vmm_white_list_target":"VMM_HOST","bios_pcrs":"0,17","vmm_pcrs":"18,19","register_host":"true","overwrite_whitelist": "true","bios_mle_name":"","vmm_mle_name":"","txt_host_record":{"host_name":"127.0.0.1","add_on_connection_string":"intel:https://127.0.0.1:1443","tls_policy_choice": {"tls_policy_id":"TRUST_FIRST_CERTIFICATE"}}}}' \
    https://127.0.0.1:8443/mtwilson/v2/rpc/create-whitelist-with-options \
    1>$CIT_BKC_DATA_PATH/$whitelist_data_file 2>$CIT_BKC_DATA_PATH/$whitelist_http_status_file

  result=$(cat $CIT_BKC_DATA_PATH/$whitelist_http_status_file | grep "200 OK" )
  if [ -z "$result" ]; then
    output_message="Error during whitelisting & registration."
    write_to_report_file
    return 1
  fi
  output_message="Create whitelist with host registration successful."
  write_to_report_file
}

# Creates the asset tag certificate by retrieving the hardware UUID and then pushes the certificate to the target host.
test_write_assettag() {
  assettag_data_file="write_assettag.data"
  assettag_http_status_file="write_assettag_http.status"
  certificate_date_file="certificate.data"
  certificate_http_status_file="certificate_http.status"
  report_file_name="write_assettag.report"
  
  curl --noproxy 127.0.0.1 -k -vs \
    https://127.0.0.1:8443/mtwilson/v2/hosts?nameEqualTo=127.0.0.1 \
    1>$CIT_BKC_DATA_PATH/$assettag_data_file 2>$CIT_BKC_DATA_PATH/$assettag_http_status_file
  
  result=$(cat $CIT_BKC_DATA_PATH/$assettag_http_status_file | grep "200 OK")
  if [ -z "$result" ]; then
    echo "Error during retrieval of hardware UUID."
    return 1
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
    echo "Error during creation the asset tag certificate for host with hardware UUID $hostHardwareUuid ."
    return 1
  fi
  echo "Successfully created the asset tag certificate for host with hardware UUID $hostHardwareUuid ."

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
    output_message="Error during pushing the asset tag certificate for host."
    write_to_report_file
    return 1
  fi
  output_message="Successfully pushed the asset tag certificate to host."
  write_to_report_file
}

# Gets the attestation status of the host. Reboot counter would be appended to the file name to compare the
# status of the host after reboot.
test_host_attestation_status() {
  host_attestation_data_file="host_attestation_$reboot_count.data"
  host_attestation_http_status_file="host_attestation_http_$reboot_count.status"
  report_file_name="host_attestation_$reboot_count.report"
  
  curl --noproxy 127.0.0.1 -k -vs \
    -H "Content-Type: application/json" \
    -H "accept: application/samlassertion+xml" \
    -X POST \
    -d '{"host_uuid":"HostID"}' \
    https://127.0.0.1:8443/mtwilson/v2/host-attestations \
    1>$CIT_BKC_DATA_PATH/$host_attestation_data_file 2>$CIT_BKC_DATA_PATH/$host_attestation_http_status_file

  result=$(cat $CIT_BKC_DATA_PATH/$assettag_http_status_file | grep "200 OK" )
  if [ -z "$result" ]; then
    output_message="Error retrieving the attestation information for the host."
    write_to_report_file
    return 1
  fi
  output_message="Successfully retrieved the attestation information for the host."
  write_to_report_file
  
  #Extract the results of the attestation and write it to the report file. Since there will be new lines and white spaces
  #we need to remove them as well.
  Trust_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Trusted'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  BIOS_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Trusted_BIOS'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  VMM_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Trusted_VMM'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  Asset_Tag_status=$(xmlstarlet sel -t -v "(/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='Asset_Tag'])"  $CIT_BKC_DATA_PATH/$host_attestation_data_file | sed '/^\s*$/d; s/ //g')
  
  output_message="BIOS Trust status:$BIOS_status\nVMM Trust Status:$VMM_status\nAsset Tag Trust status:$Asset_Tag_status"
  write_to_report_file
}

main(){
  # if the user has passed in the reboot count, use it. Or else set it to 1 as default.
  if [ -z "$1" ]; then
    reboot_count="1"
  else
    reboot_count="$1"
  fi
  
  test_tpm_support
  if [ "$?" != "0" ]; then  return 1; fi
  
  test_tpm_ownership
  if [ "$?" != "0" ]; then return 1; fi

  test_txt_support
  if [ "$?" != "0" ]; then  return 1; fi

  test_bindingkey_present
  if [ "$?" != "0" ]; then  return 1; fi

  test_signingkey_present
  if [ "$?" != "0" ]; then  return 1; fi

  test_nvindex_defined
  if [ "$?" != "0" ]; then  return 1; fi
  
  test_create_whitelist
  if [ "$?" != "0" ]; then  return 1; fi

  test_write_assettag
  if [ "$?" != "0" ]; then  return 1; fi

  test_host_attestation_status
  if [ "$?" != "0" ]; then  return 1; fi
  
}
main "$@"

