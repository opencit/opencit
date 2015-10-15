#!/bin/bash
#Analysis tboot log
# Usage: ./module_analysis.sh   (reads from txt-stat output)
#        ./module_analysis.sh  file1  (reads from previously saved output in file1)
TXTSTAT=$(which txt-stat 2>/dev/null)
TXTSTAT=${TXTSTAT:-"/usr/sbin/txt-stat"}
if [ -n "$1" ]; then INFILE="cat $1"; else INFILE="$TXTSTAT"; fi
INFILE_TCB_MEASUREMENT_SHA1=${INFILE_TCB_MEASUREMENT_SHA1:-/var/log/trustagent/measurement.sha1}
# 2.0 outputs to /opt/trustagent/var/measureLog.xml
OUTFILE=${OUTFILE:-/opt/trustagent/var/measureLog.xml}
# 1.2 outputs to measureLog.xml in current directory
#OUTFILE=measureLog.xml
#skaja, use this script to generate measureLog for modules from rc3 
BIOS_CONTECT_NONE_NAME_FILE=BIOS_CONTECT_NONE_NAME_FILE
BIOSTMP=/tmp/BIOSTMP_abc123OoooooooO
BLANK2="  "
BLANK4="    "
BLANK6="      "
BLANK8="        "
x401Data="EOF"
x402Data="EOF"
x403Data="EOF"
x404Data="EOF"
x501Data="EOF"
x501PcrIndex="EOF"
txt_status=0
round=0
declare -a x501DataArray
declare -a x501PcrIndexArray
bios_contect()
{
  # cat ascii_bios_measurements
  cat /sys/kernel/security/tpm0/ascii_bios_measurements
}
###name : value : pcrNumber ###
xml_module()
{
  echo "$BLANK6<module>"
  echo "$BLANK8<pcrNumber>$1</pcrNumber>"
  echo "$BLANK8<name>$2</name>"
  echo "$BLANK8<value>$3</value>"
  echo "$BLANK6</module>"
}

analysis_bios()
{
  if [ -d $BIOSTMP ];then
    rm -rf $BIOSTMP
  fi
  mkdir $BIOSTMP

  line=`bios_contect | wc -l`
  for((i=1;i<=$line;i++));do
    str="`bios_contect | sed -n "$i p"`"
    key_name="`echo $str | awk -F[ '{print $2}' | awk -F] '{print $1}' | grep -i -E '[A-z0-9_ ]'`"
    key_value="`echo $str | awk '{print $2}'`"
    key_type="`echo $str | awk '{print $3}'`"

    if [ ${#key_name} -eq 0 ];then
       if [ -e $BIOSTMP/$key_type ];then
         tmp="`cat $BIOSTMP/$key_type | grep "$key_value"`"
         if [ ${#tmp} -eq 0 ];then
           echo "$key_value" >> $BIOSTMP/$key_type
         fi
       else
         echo "$key_value" >> $BIOSTMP/$key_type
       fi
    else
       key_name2="`echo $key_name | sed 's/ /#/g'`"
       if [ -e $BIOSTMP/$key_name2 ];then
         tmp="`cat $BIOSTMP/$key_name2 | grep "$key_value"`"
         if [ ${#tmp} -eq 0 ];then
           echo "$key_value" >> "$BIOSTMP/$key_name2"
         fi
       else
         echo "$key_value" >> "$BIOSTMP/$key_name2"
       fi
    fi
  done
}

construct_bios_info()
{
  line=`bios_contect | wc -l`
  for((i=1;i<=$line;i++));do
    str="`bios_contect | sed -n "$i p"`"
    key_name="`echo $str | awk -F[ '{print $2}' | awk -F] '{print $1}' | grep -i -E '[A-z0-9_ ]'`"
    key_pcr="`echo $str | awk '{print $1}'`"
    key_value="`echo $str | awk '{print $2}'`"
    key_type="`echo $str | awk '{print $3}'`"
    if [ ${#key_name} -eq 0 ];then
      line2="`cat $BIOSTMP/$key_type | wc -l`"
      if [ $line2 -eq 1 ];then
        xml_module "bios_$key_type" "$key_value" "$key_pcr"
      else
        index="`cat $BIOSTMP/$key_type | grep -n $key_value | awk -F: '{print $1}'`"
        key_name2="bios_$key_type@$index"
        key_name="`echo $key_name2 | sed "s/@/_/"`"
        xml_module "$key_name" "$key_value" "$key_pcr"
      fi
    else
      key_name2="`echo $key_name | sed 's/ /#/g'`"
      line2="`cat $BIOSTMP/$key_name2 | wc -l`"
      if [ $line2 -eq 1 ];then
         xml_module "$key_name" "$key_value" "$key_pcr"
      else
         index="`cat $BIOSTMP/$key_name2 | grep -n $key_value | awk -F: '{print $1}'`"
         xml_module "$key_name $index" "$key_value" "$key_pcr"
      fi
    fi
  done
  if [ -d $BIOSTMP ];then
    rm -rf $BIOSTMP
  fi
}
bios_xml()
{
  echo "$BLANK2<bios>"
  echo "$BLANK4<modules>"
  analysis_bios
  construct_bios_info
  echo "$BLANK4</modules>"
  echo "$BLANK2</bios>"
}
########################## txt ##################################
get_line_number()
{
  local num="`$INFILE | grep -n "$1" | awk -F: '{print $1}'`"
  if [ ${#num} -gt 0 ];then
    echo $num
  else
    echo 0
  fi
}
get_value()
{
  num=`get_line_number $1`
  let "num++"
 $INFILE | sed -n "$num p" | sed "s/ //g" | sed "s/\t//g"
}

get_value2()
{
  $INFILE | sed -n "/$1/,/$2/p" | grep "$2" | awk -F: '{print $3}' | sed "s/ //g" | sed "s/\t//g" | sed -n '1 p'
}
###parm1:pcrNumber,parm2:vl_index,parm3:pcr value,parm4:xen(1)/native linux(0)###
xml_pcr()
{
  echo "$BLANK6<module>"
  echo "$BLANK8<pcrNumber>$1</pcrNumber>"
  if [[ $4 -eq 4 ]]; then
    case $2 in
    1)
      echo "$BLANK8<name>tb_policy</name>"
      ;;
    2)
      echo "$BLANK8<name>xen.gz</name>"
      ;;
    3)
      echo "$BLANK8<name>vmlinuz</name>"
      ;;
    4)
      echo "$BLANK8<name>initrd</name>"
      ;;
    5)
      echo "$BLANK8<name>asset-tag</name>"
      ;;
    esac
  else
      case $2 in
    1)
      echo "$BLANK8<name>tb_policy</name>"
      ;;
    2)
      echo "$BLANK8<name>vmlinuz</name>"
      ;;
    3)
      echo "$BLANK8<name>initrd</name>"
      ;;
    4)
      echo "$BLANK8<name>asset-tag</name>"
      ;;
    esac
  fi

  echo "$BLANK8<value>$3</value>"
  echo "$BLANK6</module>"
}
xml_pcr2() {
  xml_pcr2_index="$1"
  xml_pcr2_measurement="$2"
  xml_pcr2_measurement_name="$3"
  echo "$BLANK6<module>"
  echo "$BLANK8<pcrNumber>$xml_pcr2_index</pcrNumber>"
  echo "$BLANK8<name>$xml_pcr2_measurement_name</name>"
  echo "$BLANK8<value>$xml_pcr2_measurement</value>"
  echo "$BLANK6</module>"
}
 #main

 ######>>>> event
 #0x401
 num="`get_line_number 'Type: 0x401'`"
 for parm in $num;do
 if [ $parm -gt 0 ];then
   x401Data="`$INFILE | sed -n "$((parm+3)), $((parm+3)) p" | sed "s/ //g" | sed "s/\t//g"`"
   if [ $x401Data = "00000000" ];then
   txt_status=2
   fi
 fi
 done

if [ $txt_status -eq 2 ];then
 #0x402
 round=0
 num="`get_line_number 'Type: 0x402'`"
 for parm in $num;do
 round=$((round+1))
 if [ $parm -gt 0 ];then
   x402Data="`$INFILE | sed -n "$((parm+1)), $((parm+1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
 fi
 done
 if [ $round -ne 1 ];
   then x402Data="EOF"
 fi
 #0x043
 round=0
 num="`get_line_number 'Type: 0x403'`"
 for parm in $num;do
 round=$((round+1))
 if [ $parm -gt 0 ];then
   x403Data="`$INFILE | sed -n "$((parm+3)), $((parm+7)) p" | sed "s/ //g" | sed "s/\t//g" | sed "s/\n//g"`"
 fi
 done
 if [ $round -ne 1 ];
   then x403Data="EOF"
 else
 STRING=`echo "$x403Data"`
 x403Data="`echo $STRING | sed "s/ //g"`"
 fi
#        echo "biosAcmId = ${x403Data:0:40}"
#        echo "msegValid = ${x403Data:40:16}"
#        echo "stmHash = ${x403Data:56:40}"
#        echo "policyControl =  ${x403Data:96:8}"
#        echo "lcpPolicyHash =  ${x403Data:104:40}"
#        echo "osSinitDataCapabilities = ${x403Data:144:8}"
#        echo "processorSCRTMStatus = ${x403Data:152:8}"
 
 #0x404
 round=0
 num="`get_line_number 'Type: 0x404'`"
 for parm in $num;do
 round=$((round+1))
 if [ $parm -gt 0 ];then
   x404Data="`$INFILE | sed -n "$((parm+1)), $((parm+1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
 fi
 done
 if [ $round -ne 1 ];
   then x404Data="EOF"
 fi

 #0X501
 num="`get_line_number 'Type: 0x501'`"
 
 for parm in $num;do
 if [ $parm -gt 0 ];then
   x501Data="`$INFILE | sed -n "$((parm+1)), $((parm+1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
   x501PcrIndex="`$INFILE | sed -n "$((parm-1)), $((parm-1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
   x501DataArray[$round]=$x501Data
   x501PcrIndexArray[$round]=$x501PcrIndex
   round=$((round+1))
 fi
 done

fi
#### <<<<< event

 echo "<measureLog>" >$OUTFILE
 #bios_xml  >>$OUTFILE
 echo "$BLANK2<txt>" >>$OUTFILE
 txt_measured_launch="`$INFILE | grep 'TXT measured launch: TRUE'`"
 secrets_flag_set="`$INFILE | grep 'secrets flag set: TRUE'`"
 if [ ${#txt_measured_launch} -eq 27 -a ${#secrets_flag_set} -eq 24 ];then
   if [ $txt_status -eq 2 ];then
      echo "$BLANK2$BLANK2<txtStatus>2</txtStatus>" >>$OUTFILE
   else
      echo "$BLANK2$BLANK2<txtStatus>1</txtStatus>" >>$OUTFILE
   fi
 else
   echo "$BLANK2$BLANK2<txtStatus>0</txtStatus>" >>$OUTFILE
   echo "$BLANK2</txt>" >>$OUTFILE
   echo "</measureLog>" >>$OUTFILE
   exit 0
 fi
##
if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_policy_control="${x403Data:96:8}"
  #echo "@@ sinit_mle_data_policy_control = $sinit_mle_data_policy_control"
else
  sinit_mle_data_policy_control="`get_value2 '\<sinit_mle_data\>' '\<lcp_policy_control\>'`"
  if [ ${sinit_mle_data_policy_control:0:2} == "0x" ];then
     sinit_mle_data_policy_control=${sinit_mle_data_policy_control:2}
  fi
fi
##
if [ ${#sinit_mle_data_policy_control} -eq 8 ];then 
  second_bit_sinit_mle_data_policy_control=${sinit_mle_data_policy_control:6:1}
fi

if [ $second_bit_sinit_mle_data_policy_control -ne 1 ];then
  os_sinit_data_capabilities="00000000"
else
  if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
    os_sinit_data_capabilities="${x403Data:144:8}"
    #echo "@@ os_sinit_data_capabilities = $os_sinit_data_capabilities"
  else
    os_sinit_data_capabilities="`get_value2 '\<os_sinit_data\>' '\<capabilities\>'`" >>$OUTFILE
    if [ "${os_sinit_data_capabilities:0:2}" == "0x" ];then
       os_sinit_data_capabilities=${os_sinit_data_capabilities:2}
    fi
  fi
fi
echo "$BLANK2$BLANK2<osSinitDataCapabilities>$os_sinit_data_capabilities</osSinitDataCapabilities>" >>$OUTFILE
echo "$BLANK2$BLANK2<sinitMleData>" >>$OUTFILE
sinit_mle_data_version="`get_value2 '\<sinit_mle_data\>' '\<version\>'`" >>$OUTFILE
echo "$BLANK2$BLANK4<version>$sinit_mle_data_version</version>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x402Data!="EOF" ];then
  echo "$BLANK2$BLANK4<sinitHash>$x402Data</sinitHash>" >>$OUTFILE
else 
  sinit_mle_data_sinit_hash="`get_value '\<sinit_hash\>'`"
  echo "$BLANK2$BLANK4<sinitHash>$sinit_mle_data_sinit_hash</sinitHash>" >>$OUTFILE
fi

if [ $txt_status -eq 2 -a $x404Data!="EOF" ];then
   echo "$BLANK2$BLANK4<mleHash>$x404Data</mleHash>" >>$OUTFILE
else
  sinit_mle_data_mle_hash="`get_value '\<mle_hash\>'`"
  echo "$BLANK2$BLANK4<mleHash>$sinit_mle_data_mle_hash</mleHash>" >>$OUTFILE
fi


if [ $txt_status -eq 2 -a $x404Data!="EOF" ];then
  sinit_mle_data_bios_acm_id="${x403Data:0:40}"
  #echo "@@ sinit_mle_data_bios_acm_id = $sinit_mle_data_bios_acm_id"
else
  sinit_mle_data_bios_acm_id="`get_value '\<bios_acm_id\>'`"
fi
echo "$BLANK2$BLANK4<biosAcmId>$sinit_mle_data_bios_acm_id</biosAcmId>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_mseg_valid="${x403Data:40:16}"
  #echo "@@ sinit_mle_data_mseg_valid = $sinit_mle_data_mseg_valid"
else
  sinit_mle_data_mseg_valid="`get_value2 '\<sinit_mle_data\>' '\<mseg_valid\>'`"
  if [ ${sinit_mle_data_mseg_valid:0:2} == "0x" ];then
     sinit_mle_data_mseg_valid=${sinit_mle_data_mseg_valid:2}
  fi
fi
echo "$BLANK2$BLANK4<msegValid>$sinit_mle_data_mseg_valid</msegValid>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_stm_hash="${x403Data:56:40}"
  #echo "@@ sinit_mle_data_stm_hash = $sinit_mle_data_stm_hash"
else
  sinit_mle_data_stm_hash="`get_value '\<stm_hash\>'`"
fi
echo "$BLANK2$BLANK4<stmHash>$sinit_mle_data_stm_hash</stmHash>" >>$OUTFILE

#if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
#  sinit_mle_data_policy_control="${x403Data:96:8}"
#  echo "@@ sinit_mle_data_policy_control = $sinit_mle_data_policy_control"
#else
#  sinit_mle_data_policy_control="`get_value2 '\<sinit_mle_data\>' '\<lcp_policy_control\>'`"
#  if [ ${sinit_mle_data_policy_control:0:2} == "0x" ];then
#     sinit_mle_data_policy_control=${sinit_mle_data_policy_control:2}
#  fi
#fi
echo "$BLANK2$BLANK4<policyControl>$sinit_mle_data_policy_control</policyControl>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_lcp_policy_hash="${x403Data:104:40}"
  #echo "@@ sinit_mle_data_lcp_policy_hash = $sinit_mle_data_lcp_policy_hash"
else
  sinit_mle_data_lcp_policy_hash="`get_value '\<lcp_policy_hash\>'`"
fi
echo "$BLANK2$BLANK4<lcpPolicyHash>$sinit_mle_data_lcp_policy_hash</lcpPolicyHash>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_proc_scrtm_status="${x403Data:152:8}"
  #echo "@@ sinit_mle_data_proc_scrtm_status = $sinit_mle_data_proc_scrtm_status"
else
  sinit_mle_data_proc_scrtm_status="`get_value2 '\<sinit_mle_data\>' '\<proc_scrtm_status\>'`"
  if [ ${sinit_mle_data_proc_scrtm_status:0:2} == "0x" ];then
     sinit_mle_data_proc_scrtm_status=${sinit_mle_data_proc_scrtm_status:2}
  fi
fi
echo "$BLANK2$BLANK4<processorSCRTMStatus>$sinit_mle_data_proc_scrtm_status</processorSCRTMStatus>" >>$OUTFILE

sinit_mle_data_edx_senter_flags="`get_value2 '\<sinit_mle_data\>' '\<edx_senter_flags\>'`"
if [ ${sinit_mle_data_edx_senter_flags:0:2} == "0x" ];then
   sinit_mle_data_edx_senter_flags=${sinit_mle_data_edx_senter_flags:2}
fi
echo "$BLANK2$BLANK4<edxSenterFlags>$sinit_mle_data_edx_senter_flags</edxSenterFlags>" >>$OUTFILE

#if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
#   echo "$BLANK2$BLANK4<eventSinitMleSubData>$x403Data</eventSinitMleSubData>" >>$OUTFILE
#fi

echo "$BLANK2$BLANK2</sinitMleData>" >>$OUTFILE

echo "$BLANK2$BLANK2<modules>" >>$OUTFILE
if [ $txt_status -eq 2 -a $x501Data!="EOF" ];then
  for((g=1;g<=${#x501DataArray[*]};g++));do
    xml_pcr  "${x501PcrIndexArray[$g]}" "$g" "${x501DataArray[$g]}"  "${#x501DataArray[*]}"  >>$OUTFILE
  done
else
  num=`get_line_number 'VL measurements'`
  # tboot 1.7.x has one line per measurement
  # TBOOT:   VL measurements:
  # TBOOT:     PCR 17: 97 04 35 36 30 67 4b fe 21 b8 6b 64 a7 b0 f9 9c 29 7c f9 02
  # TBOOT:     PCR 18: 4f f9 2c 28 a3 2d 92 88 95 eb 1d 52 6f df f8 44 cd e3 98 59
  # TBOOT:     PCR 19: 42 52 51 2c 59 67 44 9e 19 37 38 e8 e5 59 d6 c6 b9 aa 16 55
  # TBOOT:     PCR 19: bc 69 04 f8 85 a0 13 48 be 00 07 12 a8 21 3b 02 da fc 41 06
  # tboot 1.7.x with asset tag patch will not have PCR 22 in the measurement section
  # but it has an additional section afterwards which should NOT be processed because
  # it only contains PCR values before/after and does not print the tag measurement;
  # the TPM quote will already have the PCR 22 value.
  # TBOOT: reading Asset TAG from from TPM NV_Index 1073741840...
  # TBOOT: PCR22 before asset_tag extention:
  # TBOOT:   PCR 22: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
  # TBOOT:  :20 GEO NV bytes read
  # TBOOT: PCR22 after GEO-TAG extending:
  # TBOOT:   PCR 22: 63 cd 97 f5 c9 b3 07 7a 79 12 9e 9a ce d2 bf 39 48 76 97 30
  # 
  # tboot 1.8.x has two or more lines per measurement
  # TBOOT:   VL measurements:
  # TBOOT:     PCR 17 (alg count 1):
  # TBOOT:             alg 0004: c3 43 84 97 fd a8 27 be 3b 32 1c 53 09 a2 04 f0 c9 e5 39 43
  # TBOOT:     PCR 18 (alg count 1):
  # TBOOT:             alg 0004: 23 46 e1 09 0b 17 f7 db a9 10 9f 66 a0 c7 10 8c 81 78 53 44
  # TBOOT:     PCR 19 (alg count 1):
  # TBOOT:             alg 0004: 32 6e 1a 61 9f e6 d0 b7 22 36 5f ef 9a 1c 8c 55 ed 85 6c 26
  # TBOOT:     PCR 22 (alg count 1):
  # TBOOT:             alg 0004: 91 cb 24 bf 69 88 89 07 8e f3 3a 21 e4 2a 4c c0 79 b9 18 c3
  #
  vl_measurements_line=`$INFILE | sed -n "$num p"`
  #echo '<!--' "vl_measurements_line:$vl_measurements_line" '-->' >>$OUTFILE
  # count the number of spaces before "VL measurements" (the indentation) so we know when
  # this section is over. that way we can process any number of measurements instead of
  # previously hard-coded max of 4 measurements.
  vl_indent=`$INFILE | sed -n "$num p" | grep -o '^TBOOT:[[:space:]]*' | wc -c`
  #echo '<!--' "vl_indent:$vl_indent" '-->' >>$OUTFILE
  # now process each line after VL measurements until the indent level indicates we
  # processed all the measurements (when it becomes less than or equal to the vl_indent)
  vl_current_line_num=$((num+1))
  vl_current_indent=`$INFILE | sed -n "$vl_current_line_num p" | grep -o '^TBOOT:[[:space:]]*' | wc -c`
  #echo '<!--' "vl_current_line_num:$vl_current_line_num" '-->' >>$OUTFILE
  #echo '<!--' "vl_current_indent:$vl_current_indent" '-->' >>$OUTFILE
  while [ $vl_current_indent -gt $vl_indent ]
  do
    #echo '<!--' "current indent: $vl_current_indent > vl indent: $vl_indent"  '-->' >>$OUTFILE
    vl_tboot_1_8_alg_count=`$INFILE | sed -n "$vl_current_line_num p" | grep -o '(alg count [0-9]*)' | grep -o '[0-9]*'`
	if [ -n "$vl_tboot_1_8_alg_count" ]; then
      #echo '<!--' "tboot 1.8 format with alg count: $vl_tboot_1_8_alg_count"  '-->' >>$OUTFILE
	  # process tboot 1.8 format. assume alg count is 1 (just SHA-1 for TPM 1.2)
	  # current line contains PCR xx (alg count yy)
	  index=`$INFILE | sed -n "$vl_current_line_num p" | grep -o 'PCR [0-9]*' | grep -o '[0-9]*'`
	  # next line contains alg zzzz: measurement (alg 0004 is SHA-1)
      vl_current_line_num=$((vl_current_line_num+1))
	  measurement=`$INFILE | sed -n "$vl_current_line_num p" | grep -o 'alg [0-9]*: [0-9a-f ]*' | cut -d':' -f2 | tr -d '[:space:]'`
	else
      #echo '<!--' "tboot 1.7 format"  '-->' >>$OUTFILE
	  # assume tboot 1.7 format
	  index=`$INFILE | sed -n "$vl_current_line_num p" | grep -o 'PCR [0-9]*' | grep -o '[0-9]*'`
	  measurement=`$INFILE | sed -n "$vl_current_line_num p" | grep -o 'PCR [0-9]*: [0-9a-f ]*' | cut -d':' -f2 | tr -d '[:space:]'`
	fi
    # now it will be helpful to search the rest of the txt-stat output to find the
    # name of the measurement. 
	measurement_name=
    mrefs_count=`$INFILE | tr -d ' ' | grep "$measurement" | wc -l`
    if [ $mrefs_count -eq 1 ]; then
	  case $index in
	    "17")
		  # platforms before ivy bridge do not report the tboot measurement itself,
		  # but it's the only measurement that would be in 17
		  measurement_name="tb_policy"
		  ;;
		"22")
		  # the asset tag measurement is not output like others, but it's the only 
		  # measurement in 22
		  measurement_name="asset-tag"
		  ;;
		*)
		  measurement_name="unknown"
          ;;
	  esac
	else
	  # check reference to the measurement, looking for line number of "OK:<measurement>" 
	  # (after spaces are removed... before removing spaces it would be "OK : <measurement>")
	  mref_ok_num=`$INFILE | tr -d ' ' | grep -n "$measurement" | grep "OK:" | cut -d':' -f1`
	  #echo '<!--' "mref_ok_num:$mref_ok_num" '-->' >>$OUTFILE
	  if [ -n "$mref_ok_num" ]; then
        # the module name itself is on the line(s) BEFORE the OK line, so backtrack until
        # we see "TBOOT: verifying module" then capture everything in between (could be multiple lines)
        # TBOOT: verifying module "
        # /initramfs-2.6.32-358.el6.x86_64.img"...
        # TBOOT:   OK : 80 f5 c9 e1 a8 b5 c3 43 d0 23 61 a0 cc d0 c9 1b dc e7 9a 06
        mref_prev_num=$((mref_ok_num-1))
        mref_is_verifying_module=`$INFILE | sed -n "$mref_prev_num p" | grep '^TBOOT: verifying module'`
		mref_is_verifying_index=`$INFILE | sed -n "$mref_prev_num p" | grep '^TBOOT: verifying nv index'`
     	  #echo '<!--' "mref_prev_num:$mref_prev_num" '-->' >>$OUTFILE
     	  #echo '<!--' "mref_is_verifying_module:$mref_is_verifying_module" '-->' >>$OUTFILE
        while [ -z "$mref_is_verifying_module" ] && [ -z "$mref_is_verifying_index" ] && [ $mref_prev_num -gt 1 ]
		do
          mref_prev_num=$((mref_prev_num-1))
          mref_is_verifying_module=`$INFILE | sed -n "$mref_prev_num p" | grep '^TBOOT: verifying module'`
     	  mref_is_verifying_index=`$INFILE | sed -n "$mref_prev_num p" | grep '^TBOOT: verifying nv index'`
     	  #echo '<!--' "mref_prev_num:$mref_prev_num" '-->' >>$OUTFILE
     	  #echo '<!--' "mref_is_verifying_module:$mref_is_verifying_module" '-->' >>$OUTFILE
     	  #echo '<!--' "mref_is_verifying_index:$mref_is_verifying_index" '-->' >>$OUTFILE
		done
        mref_prev=`$INFILE | sed -n "$mref_prev_num,$mref_ok_num p"`
		#echo '<!--' "mref_prev:$mref_prev" '-->' >>$OUTFILE
		if [ -n "$mref_is_verifying_module" ]; then
		  measurement_name=`$INFILE | sed -n "$mref_prev_num,$mref_ok_num p" | tr -d '\n' | sed 's/^TBOOT: verifying module "//' | sed 's/"\.\.\..*//'`
		fi
		if [ -n "$mref_is_verifying_index" ]; then
		  nv_index_name=`$INFILE | sed -n "$mref_prev_num,$mref_ok_num p" | tr -d '\n' | grep -o 'verifying nv index [0-9a-fx]*' | sed 's/verifying nv index //'`
		  if [ "$nv_index_name" == "0x40000010" ]; then
		    measurement_name="asset-tag"
		  else
		    measurement_name="$nv_index_name"
		  fi
		fi
      else
	    measurement_name="unknown"
	  fi
    fi
	# at this point measurement names are the complete names from the txt-stat log, 
	# for example: /vmlinuz-3.8.0-31-generic root=/dev/sda3 ro console=tty0 console=ttyS0,9600 intel_iommu=on
	# and also: /initrd.img-3.8.0-31-generic
	# but to maintain backward compatibility with mtwilson, we now map these to the previously hard-coded names
	# tb_policy, vmlinuz, initrd, and xen.gz
	if [[ "$measurement_name" =~ "tb_policy" ]]; then measurement_name=tb_policy; fi
	if [[ "$measurement_name" =~ "vmlinuz" ]]; then measurement_name=vmlinuz; fi
	if [[ "$measurement_name" =~ "initrd" ]]; then measurement_name=initrd; fi
	if [[ "$measurement_name" =~ "initramfs" ]]; then measurement_name=initrd; fi
	if [[ "$measurement_name" =~ "xen" ]]; then measurement_name=xen.gz; fi
	# output xml snippet
	xml_pcr2 "$index" "$measurement" "$measurement_name" >>$OUTFILE
	# read the next line and check indent level
    vl_current_line_num=$((vl_current_line_num+1))
    vl_current_indent=`$INFILE | sed -n "$vl_current_line_num p" | grep -o '^TBOOT:[[:space:]]*' | wc -c`
    #echo '<!--' "vl_current_line_num:$vl_current_line_num" '-->' >>$OUTFILE
    #echo '<!--' "vl_current_indent:$vl_current_indent" '-->' >>$OUTFILE
  done
fi

### looks for tcb measurement hash in /var/log/trustagent/measurement.sha1, adds
### as a module to OUTFILE
if [ -f "$INFILE_TCB_MEASUREMENT_SHA1" ]; then
  measurement_name="tbootxm"
  measurement=$(cat "$INFILE_TCB_MEASUREMENT_SHA1")
  xml_pcr2 "19" "$measurement" "$measurement_name" >>$OUTFILE
fi

echo "$BLANK2$BLANK2</modules>" >>$OUTFILE
echo "$BLANK2</txt>" >>$OUTFILE
echo "</measureLog>" >>$OUTFILE

