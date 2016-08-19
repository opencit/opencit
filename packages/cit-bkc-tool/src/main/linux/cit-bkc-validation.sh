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

write_to_report_file() {
  current_date=`date +%Y-%m-%d:%H:%M:%S`
  echo $output_message
  echo $current_date $output_message > $CIT_BKC_DATA_PATH/$report_file_name
  output_message=""
  report_file_name=""
}

test_tpm_support() {
  file=$(ls --color=none /dev/tpm0)
  if [ "$file" = "/dev/tpm0" ]; then
    output_message="TPM is supported."	
  else
    output_message="TPM is not supported."
  fi
  report_file_name="tpm_support.report"
  write_to_report_file
}

test_tpm_ownership() {
  if [[ "$(cat /sys/class/misc/tpm0/device/owned)" == 1 ]]; then
    output_message="TPM is owned."
  else
    output_message="TPM is not owned."
  fi
  report_file_name="tpm_ownership.report"
  write_to_report_file
}

test_txt_support() {
  TXT=$(cat /proc/cpuinfo | grep -o "smx" | head -1)
  if [ $TXT = "smx" ]; then
    output_message="TXT is supported."
  else
    output_message="TXT is not supported"
  fi
  report_file_name="txt_support.report"
  write_to_report_file
}

test_aik_present() {
  AIKCertFile="/opt/trustagent/configuration/aik.pem"
  if [ -f $AIKCertFile ]; then
    output_message="AIK certificate exists."
  else
    output_message="AIK certificate ($AIKCertFile) does not exist."
  fi
  report_file_name="aik_present.report"
  write_to_report_file
}

test_bindingkey_present() {
  BindingKeyFile="/opt/trustagent/configuration/bindingkey.pem"
  if [ -f $BindingKeyFile ]; then
    output_message="Binding key certificate exists."
  else
    output_message="Binding key certificate ($BindingKeyFile) does not exist."
  fi
  report_file_name="bindingkey_present.report"
  write_to_report_file
}

test_signingkey_present() {
  SigningKeyFile="/opt/trustagent/configuration/signingkey.pem"
  if [ -f $SigningKeyFile ]; then
    output_message="Signing key certificate exists."
  else
    output_message="Signing key certificate ($SigningKeyFile) does not exist."
  fi
  report_file_name="signingkey_present.report"
  write_to_report_file
}

test_nvindex_defined() {
  indexDefined=$(tpm_nvinfo -i "$ASSET_TAG_NVRAM_INDEX" 2>/dev/null)
  if [ -n "$indexDefined" ]; then
    output_message="NV index defined."
  else
    output_message="NV index not defined. Asset tags cannot be configured."
  fi
  report_file_name="nvindex_defined.report"
  write_to_report_file
}

test_create_whitelist() {
  whitelist_data_file="create_whitelist.data"
  #Writing the entire standard output to the .data file. We would parse that file to get the result. 
  #-s option removes the progress meter
  #TODO - Stop from showing the output on stdout.
  curl --noproxy 127.0.0.1 -k -vs -H "Content-Type: application/json" -H "accept: application/json" -X POST -d '{"wl_config":{"add_bios_white_list":"true","add_vmm_white_list":"true","bios_white_list_target":"BIOS_HOST","vmm_white_list_target":"VMM_HOST","bios_pcrs":"0,17","vmm_pcrs":"18,19","register_host":"true","overwrite_whitelist": "true","bios_mle_name":"","vmm_mle_name":"","txt_host_record":{"host_name":"127.0.0.1","add_on_connection_string":"intel:https://127.0.0.1:1443","tls_policy_choice": {"tls_policy_id":"TRUST_FIRST_CERTIFICATE"}}}}' https://127.0.0.1:8443/mtwilson/v2/rpc/create-whitelist-with-options &> /dev/stdout | tee $CIT_BKC_DATA_PATH/$whitelist_data_file

  result=$(cat $CIT_BKC_DATA_PATH/$whitelist_data_file | grep "200 OK" | awk '{print $2" "$3}')
  if [ "$result" == "HTTP/1.1 200" ]; then
    output_message="Create whitelist with host registration successful."
  else
    output_message="Error during whitelisting & registration."
  fi
  report_file_name="create_whitelist.report"
  write_to_report_file
}

#test_register_host() {}

test_write_assettag() {
  assettag_data_file="write_assettag.data"
  certificate_date_file="certificate.data"
  
  curl --noproxy 127.0.0.1 -k -vs https://127.0.0.1:8443/mtwilson/v2/hosts?nameEqualTo=127.0.0.1 &> /dev/stdout | tee $CIT_BKC_DATA_PATH/$assettag_data_file
  result=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "200 OK" | awk '{print $2" "$3}')
  if [ "$result" == "HTTP/1.1 200" ]; then
    echo "Successfully called into CIT to retrieve the hardware UUID of the host."
  else
    echo "Error during retrieval of hardware UUID."
	return
  fi
  
  hostHardwareUuid=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "hardwareUuid" | sed -n 's/.*<hardwareUuid> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the hardware UUID of the host - $hostHardwareUuid"
  
  hostUuid=$(cat $CIT_BKC_DATA_PATH/$assettag_data_file | grep "<id>" | sed -n 's/.*<hardwareUuid> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the UUID of the host - $hostUuid"

  #Now that we have the hardware UUID, create the asset tag certificate
  curl --noproxy 127.0.0.1 -k -vs -H "Content-Type: application/xml" -H "accept: application/xml" -X POST -d '<?xml version="1.0" encoding="UTF-8"?><selections xmlns="urn:mtwilson-tag-selection"><selection><subject><uuid>$hostHardwareUuid</uuid></subject><attribute oid="2.5.4.789.1"><text> Country=US</text></attribute><attribute oid="2.5.4.789.1"><text>State=CA</text></attribute><attribute oid="2.5.4.789.1"><text> City=Folsom</text></attribute></selection></selections>' https://127.0.0.1:8443/mtwilson/v2/tag-certificate-requests-rpc/provision?subject=$hostHardwareUuid &> /dev/stdout | tee $CIT_BKC_DATA_PATH/$certificate_date_file
  result=$(cat $CIT_BKC_DATA_PATH/$certificate_date_file | grep "200 OK" | awk '{print $2" "$3}')
  if [ "$result" == "HTTP/1.1 200" ]; then
    echo "Successfully created the asset tag certificate for host with hardware UUID $hostHardwareUuid ."
  else
    echo "Error during creation the asset tag certificate for host with hardware UUID $hostHardwareUuid ."
	return
  fi

  certificateId=$(cat $CIT_BKC_DATA_PATH/$certificate_date_file | grep "<id>" | sed -n 's/.*<id> *\([^<]*\).*/\1/p')
  echo "Successfully retrieved the certificate id - $certificateId"
  
  #Push the tag
  #We are using the -a tag to append to the file instead of overwriting it.
  curl --noproxy 127.0.0.1 -k -vs -H "Content-Type: application/json" -H "accept: application/json" -X POST -d '{"certificate_id":"$certificateId","host":"$hostUuid"}' https://127.0.0.1:8443/mtwilson/v2/rpc/deploy-tag-certificate &> /dev/stdout | tee  $CIT_BKC_DATA_PATH/$assettag_data_file
  result=$(cat $CIT_BKC_DATA_PATH/$certificate_date_file | grep "200 OK" | awk '{print $2" "$3}')
  if [ "$result" == "HTTP/1.1 200" ]; then
    output_message="Successfully pushed the asset tag certificate to host."
  else
    output_message="Error during pushing the asset tag certificate for host."
  fi
  report_file_name="write_assettag.report"
  write_to_report_file
}

#test_get_pcr0() {}

#test_create_assettag() {}

main(){
  #test_tpm_support
  #test_tpm_ownership
  #test_txt_support
  #test_bindingkey_present
  #test_signingkey_present
  #test_nvindex_defined
  #test_create_whitelist
  test_write_assettag
}
main "$@"

