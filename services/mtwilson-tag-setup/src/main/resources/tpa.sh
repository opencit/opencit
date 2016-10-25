#!/bin/bash
# VERSION 1.0.0     last-edited-by: rksavinx     date: 2014-02-01
TITLE="Asset tag provisioning Agent"
tpaDir=/var/tpa
if [ ! -d "$tpaDir" ]; then 
  mkdir -p "$tpaDir"
  chmod 700 "$tpaDir"
fi

certSha1="$tpaDir/certSha1"
nvramPass=ffffffffffffffffffffffffffffffffffffffff
ownerPass=ffffffffffffffffffffffffffffffffffffffff
srkPass=ffffffffffffffffffffffffffffffffffffffff
# autoSelect=0
mode="VMWARE"
selection=""
server=""
cert=""
username=""
password=""
config="$tpaDir/config"
CERT_FILE_LOCATION="$tpaDir/cacert"
XML_FILE_LOCATION="$tpaDir/xml"
values=`cat /proc/cmdline`
OIFS="$IFS"
IFS=' '
read -a valueArray <<< "${values}"
IFS="$OIFS"
cmdFile="$tpaDir/command"
tpmnvinfo=/usr/local/sbin/tpm_nvinfo
tpmnvdefine=/usr/local/sbin/tpm_nvdefine
tpmnvwrite=/usr/local/sbin/tpm_nvwrite
tpmnvrelease=/usr/local/sbin/tpm_nvrelease
tpmnvread=/usr/local/sbin/tpm_nvread
tpmtakeownership=/usr/local/sbin/tpm_takeownership
tpmclear=/usr/local/sbin/tpm_clear
expect=/usr/bin/expect
INDEX=0x40000010
SIZE=0x14

#read the variables from /proc/cmdline
#to support automation ussuage with the script
#to pass data it uses this form
#atag_KEY=VALUE
#where key is the variable name
#and value is the value you want want KEY set to
#current values are
#cert
#username
#password
#config
for i in "${valueArray[@]}"
do
  prefix=${i:0:5}
  if [ "$prefix" == "atag_" ]; then
   keyPair=`echo $i| awk -F'=' '{print $1}'`
   key=`echo $keyPair| awk -F'_' '{print $2}'`
   #echo "key = $key"
   value=`echo $i| awk -F'=' '{print $2}'`
   #echo "value = $value"
   eval $key=$value
  fi
done

#now allow config file to overwrite
. $config

#download the certificate of the asset tag server 
#so we know who we are connecting to
if [ -n "$cert" ]; then
    wget --no-proxy $cert -O $CERT_FILE_LOCATION
fi
if [ -n "$xml" ]; then 
    wget --no-proxy $xml -O $XML_FILE_LOCATION
fi

WGET="wget --no-proxy --ca-certificate=$CERT_FILE_LOCATION --password=$password --user=$username --auth-no-challenge"
UUID=`dmidecode |grep UUID | awk '{print $2}'`
tagChoice=""
tagFile=""
tagServer=""
tagSelectionName=""
certAuthority=""
certFile=""
selectionFile=$tpaDir/selection
certFile=$tpaDir/cert
certInfoFile=$tpaDir/certInfo
certFileValues=$tpaDir/certValues

rm $selectionFile
rm $certFile
rm $certInfoFile
rm $certFileValues
rm $certSha1

functionReturn=0
isUsingXml=0

function generatePasswordHex() {
  < /dev/urandom tr -dc a-f0-9 | head -c${1:-32}
}

function jsonval() {
  json_string=$1
  json_property=$2
  temp=echo $json_string | sed 's/\\\\\//\//g' | sed 's/[{}]//g' | awk -v k="text" '{n=split($0,a,","); for (i=1; i<=n; i++) print a[i]}' | sed 's/\"\:\"/\|/g' | sed 's/[\,]/ /g' | sed 's/\"//g' | grep -w $json_property| cut -d":" -f2| sed -e 's/^ *//g' -e 's/ *$//g'
  echo ${temp##*|}
}

function takeOwnershipTpm() {
  $tpmtakeownership -x -t -oownerPass -z #> /dev/null 2>&1
#(
#$expect << EOD
#spawn $tpmtakeownership
#expect "Enter owner password:"
#send "$ownerPass\r"
#expect "Confirm password:"
#send "$ownerPass\r"
#expect "Enter SRK password:"
#send "$srkPass\r"
#expect "Confirm password:"
#send "$srkPass\r"
#interact
#expect eof
#EOD
#) > /dev/null 2>&1
}

function clearOwnershipTpm() {
  $tpmclear -t -x -oownerPass #> /dev/null 2>&1
}

function releaseNvram() {
 functionReturn=0
 $tpmnvrelease -x -t -i $INDEX -oownerPass #> /dev/null 2>&1
}


function createIndex4() {
 functionReturn=0
 output=`$tpmnvinfo -i $INDEX`
 if [ -z "$output" ]; then
  $tpmnvdefine -x -t -i $INDEX -s $SIZE -anvramPass -oownerPass -p "AUTHWRITE"
 fi
}

function getLocalTag() {
 functionReturn=0
 if [ -f "$XML_FILE_LOCATION" ]; then
   tagFile=$XML_FILE_LOCATION
 else
   tagFile=$(dialog --stdout --backtitle "$TITLE" --stdout --title "Please choose a file" --fselect ~ 14 48)
   if [ $? -eq 1 ]; then 
     functionReturn=1
     return
   fi
 fi
 isUsingXml=1
}

function getRemoteTag() {
 functionReturn=0
 if [ -z "$selection" ]; then 
   tagServer=$(dialog --stdout --backtitle "$TITLE" --inputbox "Enter URL to download Asset tag Selection:" 8 50)
   if [ $? -eq 1 ]; then 
     functionReturn=1
     return
   fi
 else
    tagServer=$selection
 fi
 #wget "$URL" 2>&1 | awk '/[.] +[0-9][0-9]?[0-9]?%/ { print substr($0,63,3) }' |  dialog --gauge "Download Test" 10 100
 echo "$WGET $tagServer -O $selectionFile" >> $cmdFile
 $WGET "$tagServer" -O $selectionFile 2>&1 | awk '/[.] +[0-9][0-9]?[0-9]?%/ { print substr($0,63,3) }' |  dialog --stdout --backtitle "$TITLE" --title "Please wait..." --gauge "Downloading the Asset Tag selection from $tagServer" 10 60 0
 clear
 if [ ! -s $selectionFile ]; then
  dialog --stdout --backtitle "$TITLE" --msgbox 'Unable to download tag selection!' 6 20
  exit -1;
 fi
 if [ ! "$accept" == "yes"]; then
   dialog --stdout --backtitle "$TITLE" --msgbox 'Tag selection downloaded successfully!' 6 20
 fi
}

function getTagOption() {
 functionReturn=0
 if [ -z "$selection" ]; then
   if [ -z "$xml" ]; then
     # tagChoice=$(dialog --stdout --backtitle "$TITLE" --radiolist "Select how to obtain tags" 10 70 3 1 "Download from remote server" on 2 "Local file" off 3 "Automatic" off)
     tagChoice=$(dialog --stdout --backtitle "$TITLE" --radiolist "Select how to obtain tags" 10 70 3 1 "Download from remote server" on 2 "Local file" off)
     if [ $? -eq 1 ]; then
       exit 0;
     fi
   else
     tagChoice=2
   fi
 else
  tagChoice=1
 fi
}

function provisionCert() {
 functionReturn=0
 if [ -z "$server" ]; then 
   server=$(dialog --stdout --backtitle "$TITLE" --inputbox "Enter URL to Asset Certificate Authority:" 8 50)
 fi

 rm "$tpaDir/tempStatus"
 echo "$WGET -q -O $tpaDir/tempStatus $server/version" >> $cmdFile
 $WGET -q -O "$tpaDir/tempStatus" "$server/version"
 if [ ! -s "$tpaDir/tempStatus" ]; then
   echo "$WGET --secure-protocol=TLSv1_2 -q -O $tpaDir/tempStatus $server/version" >> $cmdFile
   $WGET --secure-protocol=TLSv1_2 -q -O "$tpaDir/tempStatus" "$server/version"
   if [ -s "$tpaDir/tempStatus" ]; then
     export WGET="WGET --secure-protocol=TLSv1_2"
   else
     echo "$WGET --secure-protocol=TLSv1_1 -q -O $tpaDir/tempStatus $server/version" >> $cmdFile
     $WGET --secure-protocol=TLSv1_1 -q -O "$tpaDir/tempStatus" "$server/version"
     if [ -s "$tpaDir/tempStatus" ]; then
       export WGET="WGET --secure-protocol=TLSv1_1"
     else
       dialog --stdout --backtitle "$TITLE" --msgbox 'A SSL/TLS connection could not be established with the server. Please verify settings and try again.' 10 60
       echo "A SSL/TLS connection could not be established with the server. Please verify settings and try again." > "$tpaDir/completion"
       exit -1;
     fi
   fi
 fi
 
 if [ $isUsingXml == 0 ]; then
   # if [ $autoSelect != 1 ]; then
    if [ -z "$selectionName" ]; then
      selectionName=$(dialog --stdout --backtitle "$TITLE" --inputbox "Enter Tag Selection Name:" 8 50)
    fi
#    json='{"selections":[{"name":"'$selectionName'"}]}'
    json='{"options":{"cache":{"mode":"off"}},"default":{"selections":[{"name":"'"$selectionName"'"}]}}'
   # fi
   echo "$WGET --header=\"Content-Type: application/json\" --header=\"Accept: application/pkix-cert\" --post-data=\"$json\" $server/tag-certificate-requests-rpc/provision?subject=$UUID -O $certFile" >> $cmdFile
   $WGET --header="Content-Type: application/json" --header="Accept: application/pkix-cert" --post-data="$json" $server/tag-certificate-requests-rpc/provision?subject=$UUID -O $certFile 2>&1 | awk '/[.] +[0-9][0-9]?[0-9]?%/ { print substr($0,63,3) }'
 else
   #here we need to read the xml from the file, escape the " with \ then build our string to send via wget
   encrypted=`head -n 1 $tagFile | grep "Content-Type: encrypted"`
   if [[ -z "$encrypted" ]]; then   # NOT encrypted
     #xmlData=`cat $tagFile | tr -d '\n'`
     #json='[{ "subject": "'$UUID'", "selection": "xml", "xml": "'$xmlData'"}]'
     echo "$WGET --header=\"Content-Type: application/xml\" --header=\"Accept: application/pkix-cert\" --post-file=\"$tagFile\" $server/tag-certificate-requests-rpc/provision?subject=$UUID -O $certFile" >> $cmdFile
     $WGET --header="Content-Type: application/xml" --header="Accept: application/pkix-cert" --post-file="$tagFile" $server/tag-certificate-requests-rpc/provision?subject=$UUID -O $certFile 2>&1 | awk '/[.] +[0-9][0-9]?[0-9]?%/ { print substr($0,63,3) }'
   else   #encrypted
     #xmlData=`cat $tagFile | tr -d '\n'`
     #json='[{ "subject": "'$UUID'", "selection": "xml", "xml": "'$xmlData'"}]'
     echo "$WGET --header=\"Content-Type: message/rfc822\" --header=\"Accept: application/pkix-cert\" --post-file=\"$tagFile\" $server/tag-certificate-requests-rpc/provision?subject=$UUID -O $certFile" >> $cmdFile
     $WGET --header="Content-Type: message/rfc822" --header="Accept: application/pkix-cert" --post-file="$tagFile" $server/tag-certificate-requests-rpc/provision?subject=$UUID -O $certFile 2>&1 | awk '/[.] +[0-9][0-9]?[0-9]?%/ { print substr($0,63,3) }'
   fi
 fi

 if [ ! -s "$certFile" ]; then
   echo "Error downloading asset tag certificate. Check certificate file output here: $certFile"
   echo "Error downloading asset tag certificate. Check certificate file output here: $certFile" > "$tpaDir/completion"
   exit -1
 fi

 #if [ ! "$accept" == "yes" ]; then
 #  acceptCert=$(dialog --stdout --backtitle "$TITLE" --title "Asset Certificate"  --yesno "Do you wish to view the certificate?" 10 60)
 #  if [ $? -eq 0 ]; then
     #xml2 < $certFile > $certFileValues
     #dialog --stdout --backtitle "$TITLE" --title "Asset Certificate:" --textbox $certFileValues 35 80
     
     #openssl x509 -in $certFile -text -noout > $certFileValues
 #    less $certFile
 #  fi
 #fi
 if [ ! "$accept" == "yes" ]; then
   writeCert=$(dialog --stdout --backtitle "$TITLE" --title "Asset Certificate"  --yesno "Do you wish to deploy downloaded certificate to host TPM?" 10 60)
   resp=$?;
 else
    resp=0;
 fi
 if [ $resp -eq 0 ]; then
  # Retrieve password if TA, else generate new passwords and take ownership
  echo "$WGET $server/host-tpm-passwords/$UUID.json -q -O $tpaDir/tpmPassword" >> $cmdFile
  $WGET $server/host-tpm-passwords/$UUID.json -q -O $tpaDir/tpmPassword
  #export ownerPass=`cat /tmp/tpmPassword | cut -d':' -f2 | sed -e 's/\"//g'| sed -e 's/}//g'`
  export ownerPass=`cat $tpaDir/tpmPassword | awk -F'"password":' '{print $2}' | awk -F'"' '{print $2}'`
  if [ -z $ownerPass ]; then
    mode="VMWARE"
    export ownerPass=`generatePasswordHex 40`
    export srkPass=`generatePasswordHex 40`
    takeOwnershipTpm
  else
    mode="TA"
  fi
  export nvramPass=`generatePasswordHex 40`

  releaseNvram
  createIndex4

  #sha1=`xml2 < $certFile  | grep sha1`
  #sha1=`echo "${sha1#*sha1=}"`
  #sha1=`openssl dgst -sha1 $certFile`
  #echo "$sha1" | hex2bin > $certSha1
  # hex2bin "$sha1" $certSha1
  openssl dgst -sha1 -binary $certFile > $certSha1

  echo "$tpmnvwrite -x -t -i $INDEX -pnvramPass -f $certSha1 > $tpaDir/certWrite" >> $cmdFile
  $tpmnvwrite -x -t -i $INDEX -pnvramPass -f $certSha1 #> $tpaDir/certWrite 2>&1
  result=$?

  # If VMWARE, clear TPM ownership
  if [ "$mode" == "VMWARE" ]; then
    clearOwnershipTpm
  fi

  sleep 5;
  if [ $result -eq 0 ]; then
   if [ "$accept" == "yes" ]; then
     echo "completed sucessfully " > $tpaDir/completion
   else
     dialog --backtitle "$TITLE" --msgbox "Certificate deployed.\nThank you for using the Asset Tag Provisioning Tool" 10 34
   fi
  else
   if [ "$accept" == "yes" ]; then
     echo "completed sucessfully " > $tpaDir/completion
   else
     dialog --backtitle "$TITLE" --msgbox "Certificate not deployed.\nPlease check $tpaDir/certWrite for error messages" 10 34
   fi
  fi
 fi
}


function _main() {
 getTagOption
 mybreak=0
 while [ $mybreak -ne 1 ]; do
  case "$tagChoice" in 
   1)
    #getRemoteTag
    #if [ $functionReturn -eq 0 ]; then
    # mybreak=1
    #else
    #  tagChoice=4
    #fi
    mybreak=1
    # autoSelect=0
    tagChoice=4
    ;;
   2)
    getLocalTag
    if [ $functionReturn -eq 0 ]; then
     mybreak=1
    else
      tagChoice=4
    fi
    ;;
  # 3)
  #  mybreak=1
  #  autoSelect=1
  #  tagChoice=4
  #  ;;
   *)
    getTagOption
    ;;
  esac
 done

 provisionCert

}

_main
