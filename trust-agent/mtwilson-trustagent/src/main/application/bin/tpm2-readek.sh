#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 2 || $# > 3 ]]; then
  echo -e "usage: \n  $0 <ownerpasswd> <ektype>\n or\n  $0 <ownerpasswd> <ektype> verbose"
  exit 2
fi

passwd=$1
ekType=$2 #RSA, ECC
verbose=$3 #verbose
ekFile=/tmp/ek.pub
ekHandle=
scriptPath=`dirname $0`

if [[ -z $scriptPath ]]; then
  cmdfindek=tpm2-findek.sh
  cmdcreateek=tpm2-createek.sh
else
  cmdfindek="$scriptPath/tpm2-findek.sh"
  cmdcreateek="$scriptPath/tpm2-createek.sh"
fi

function read_pubkey()
{
  ekHandle=$1
  ekFile=$2
  verbose=$3 #verbose

  if [[ $verbose == "verbose" ]]; then
    echo "ekHandle = $ekHandle"
    tpm2_readpublic -H $ekHandle -o $ekFile
  else
    tpm2_readpublic -H $ekHandle -o $ekFile > /dev/null
  fi

  if [[ $? != 0 ]];then
    echo "failed: unable to read pubkey."
    return 1
  fi
}

function output_result()
{
  ekType=$1
  ekHandle=$2
  ekFile=$3
 
  echo -n "$ekHandle "
  
  case $ekType in
    "RSA")
      xxd -s 102 -l 256 -ps $ekFile | tr -d "\n"  ;;
    "ECC") # to be changed for ECC case
      xxd -ps $ekFile | tr -d "\n"  ;;
  esac

  echo
}

rm $ekFile -f

result=`$cmdfindek $ekType`
if [[ $? != 0 ]]; then
  result1=`$cmdcreateek $passwd $ekType $ekFile`
  if [[ $? != 0 ]]; then
    echo "failed to create EK"
    exit 1
  fi
  output_result $ekType `echo $result1 | grep -o "0x[[:xdigit:]]\{8\}"` $ekFile
  exit 0
fi

#echo $result
ekHandle=`echo $result | grep -o "0x[[:xdigit:]]\{8\}"`
read_pubkey $ekHandle $ekFile $verbose
if [[ $? != 0 ]]; then
  echo failed to read $ekHandle
  exit 1
fi

output_result $ekType $ekHandle $ekFile

if [[ $verbose == "verbose" ]]; then
  echo "done. Read from $ekHandle"
fi



