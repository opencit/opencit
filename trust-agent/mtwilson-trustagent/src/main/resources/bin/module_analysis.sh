#!/bin/bash
#Analysis tboot log
OUTFILE=/opt/trustagent/var/measureLog.xml
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
  local num="`txt-stat | grep -n "$1" | awk -F: '{print $1}'`"
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
 txt-stat | sed -n "$num p" | sed "s/ //g" | sed "s/\t//g"
}

get_value2()
{
  txt-stat | sed -n "/$1/,/$2/p" | grep "$2" | awk -F: '{print $3}' | sed "s/ //g" | sed "s/\t//g" | sed -n '1 p'
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

 #main

 ######>>>> event
 #0x401
 num="`get_line_number 'Type: 0x401'`"
 for parm in $num;do
 if [ $parm -gt 0 ];then
   x401Data="`txt-stat | sed -n "$((parm+3)), $((parm+3)) p" | sed "s/ //g" | sed "s/\t//g"`"
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
   x402Data="`txt-stat | sed -n "$((parm+1)), $((parm+1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
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
   x403Data="`txt-stat | sed -n "$((parm+3)), $((parm+7)) p" | sed "s/ //g" | sed "s/\t//g" | sed "s/\n//g"`"
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
   x404Data="`txt-stat | sed -n "$((parm+1)), $((parm+1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
 fi
 done
 if [ $round -ne 1 ];
   then x404Data="EOF"
 fi

 #0X501
 num="`get_line_number 'Type: 0x501'`"
 
 for parm in $num;do
 if [ $parm -gt 0 ];then
   x501Data="`txt-stat | sed -n "$((parm+1)), $((parm+1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
   x501PcrIndex="`txt-stat | sed -n "$((parm-1)), $((parm-1)) p" |  awk -F: '{print $3}'| sed "s/ //g" | sed "s/\t//g"`"
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
 txt_measured_launch="`txt-stat | grep 'TXT measured launch: TRUE'`"
 secrets_flag_set="`txt-stat | grep 'secrets flag set: TRUE'`"
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
  echo "@@ sinit_mle_data_policy_control = $sinit_mle_data_policy_control"
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
    echo "@@ os_sinit_data_capabilities = $os_sinit_data_capabilities"
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
  echo "@@ sinit_mle_data_bios_acm_id = $sinit_mle_data_bios_acm_id"
else
  sinit_mle_data_bios_acm_id="`get_value '\<bios_acm_id\>'`"
fi
echo "$BLANK2$BLANK4<biosAcmId>$sinit_mle_data_bios_acm_id</biosAcmId>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_mseg_valid="${x403Data:40:16}"
  echo "@@ sinit_mle_data_mseg_valid = $sinit_mle_data_mseg_valid"
else
  sinit_mle_data_mseg_valid="`get_value2 '\<sinit_mle_data\>' '\<mseg_valid\>'`"
  if [ ${sinit_mle_data_mseg_valid:0:2} == "0x" ];then
     sinit_mle_data_mseg_valid=${sinit_mle_data_mseg_valid:2}
  fi
fi
echo "$BLANK2$BLANK4<msegValid>$sinit_mle_data_mseg_valid</msegValid>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_stm_hash="${x403Data:56:40}"
  echo "@@ sinit_mle_data_stm_hash = $sinit_mle_data_stm_hash"
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
  echo "@@ sinit_mle_data_lcp_policy_hash = $sinit_mle_data_lcp_policy_hash"
else
  sinit_mle_data_lcp_policy_hash="`get_value '\<lcp_policy_hash\>'`"
fi
echo "$BLANK2$BLANK4<lcpPolicyHash>$sinit_mle_data_lcp_policy_hash</lcpPolicyHash>" >>$OUTFILE

if [ $txt_status -eq 2 -a $x403Data!="EOF" ];then
  sinit_mle_data_proc_scrtm_status="${x403Data:152:8}"
  echo "@@ sinit_mle_data_proc_scrtm_status = $sinit_mle_data_proc_scrtm_status"
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
  line=`txt-stat | sed -n "$num, $((num+10)) p" | grep PCR | wc -l`
  pcr22_line=`txt-stat | sed -n "$num, $((num+10)) p" | grep "PCR 22" | wc -l`

  # figure out which version of txt-stat output we're seeing (1-line is older, 2-line is newer)
  #echo '<!--' "line:$line" '-->' >>measureLog.xml
  is_oneline=""
  if [ -n "$is_oneline" ]; then
      # output is one line per module or pcr
      ### now just support 2 same pcr index###
      for((l=1;l<=$line;l++));do
         str="`txt-stat | sed -n "$num, $((num+4)) p" | grep PCR | sed -n "$l p"`"
         #echo '<!--' "str:$str" '-->' >>measureLog.xml
         index=`echo $str | awk -F: '{print $2}' | awk '{print $2}'`
         value="`echo $str | awk -F: '{print $3}'  | sed "s/ //g" | sed "s/\t//g"`"
         #echo '<!--' "1:$index 2:$l 3:$value 4:$line" '-->' >>measureLog.xml
         xml_pcr $index $l $value $line >>measureLog.xml
      done
  else
      # output looks like this, in two lines:
      #TBOOT:     PCR 17 (alg count 1):
      #TBOOT:             alg 0004: c3 43 84 97 fd a8 27 be 3b 32 1c 53 09 a2 04 f0 c9 e5 39 43
      ### now just support 2 same pcr index###
      for((l=1;l<=$line;l++));do
         str1="`txt-stat | sed -n "$num, $((num+10)) p" | grep PCR | sed -n "$l p"`"
         #echo '<!--' "str1:$str1" '-->' >>measureLog.xml
         str2="`txt-stat | sed -n "$num, $((num+10)) p" | grep "alg 0004" | sed -n "$l p"`"
         #echo '<!--' "str2:$str2" '-->' >>measureLog.xml
         index=`echo $str1 | awk -F: '{print $2}' | awk '{print $2}'`
         value="`echo $str2 | awk -F: '{print $3}'  | sed "s/ //g" | sed "s/\t//g"`"
         #echo '<!--' "1:$index 2:$l 3:$value 4:$line pcr22:$pcr22_line" '-->' >>measureLog.xml
         xml_pcr $index $l $value $((line-pcr22_line)) >>measureLog.xml
      done
  fi
fi
echo "$BLANK2$BLANK2</modules>" >>$OUTFILE
echo "$BLANK2</txt>" >>$OUTFILE
echo "</measureLog>" >>$OUTFILE

