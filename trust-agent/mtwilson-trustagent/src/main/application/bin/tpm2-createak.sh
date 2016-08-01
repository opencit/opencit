#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 4 || $# > 5 ]]; then
  echo -e "usage: \n  $0 <ownerpasswd> <akpasswd> <ekhandle> <aktype>\n or\n  $0 <ownerpasswd> <akpasswd> <ekhandle> <aktype> verbose"
  exit 2
fi

ownerPasswd=$1
endorsePasswd=$1
akPasswd=$2
ekHandle=$3
akType=$4 #RSA, ECC
verbose=$5 #verbose
akFile=/tmp/ak.pub
akNameFile=/tmp/ak.name
akTypeHex=unknown
tmpFile=/tmp/persistentobject
akHandle=0x81018000

case $akType in
  "RSA") akTypeHex=0x1;;
  "ECC") akTypeHex=0x23;;
esac

if [[ $verbose == "verbose" ]]; then
  echo -n "Create AK ($akType:$akTypeHex): "
fi

if [[ $akTypeHex == unknown ]]; then
  echo "failed: unknown type"
  exit 1
fi

function clear_ak_handle() {
  if [[ $verbose == "verbose" ]]; then
    echo "Trying to clear any existing AK handles..."
  fi

  tpm2_listpersistent | grep -q "Persistent handle: 0x81018000"
  if [[ $? != 0 ]]; then
    #ak doesnt exist, we dont need to clear, keep going
    return 0
  fi
  if [[ $verbose == "verbose" ]]; then
    echo "clearing $akHandle"
    tpm2_evictcontrol -A o -H $akHandle -S $akHandle -P $ownerPasswd -X
  else
    tpm2_evictcontrol -A o -H $akHandle -S $akHandle -P $ownerPasswd -X > /dev/null
  fi

  if [[ $? != 0 ]]; then
    echo "failed to clear ak @ $akHandle"
    return 1
  fi

  return 0
}

function output_result()
{
  akType=$1
  akHandle=$2
  akFile=$3
  akNameFie=$4

  echo -n "$akHandle "

  case $akType in
    "RSA")
      xxd -s 102 -l 256 -ps $akFile | tr -d "\n"  ;;
    "ECC") # to be changed for ECC case
      xxd -ps $akFile | tr -d "\n"  ;;
  esac

  echo -n " "

  xxd -ps $akNameFile | tr -d "\n"

  echo
}

rm $akFile $akNameFile -f

clear_ak_handle

if [[ $? != 0 ]]; then
  echo "failed: no usable persistent handle"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo "akHandle = $akHandle"
  tpm2_getpubak -e $endorsePasswd -o $ownerPasswd -P $akPasswd -E $ekHandle -k $akHandle -f $akFile -n $akNameFile -g $akTypeHex -D 0x000B -s 0x0014 -X
else
  tpm2_getpubak -e $endorsePasswd -o $ownerPasswd -P $akPasswd -E $ekHandle -k $akHandle -f $akFile -n $akNameFile -g $akTypeHex -D 0x000B -s 0x0014 -X > /dev/null
fi

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

output_result $akType $akHandle $akFile $akName

if [[ $verbose == "verbose" ]]; then
  echo "done. Created @ $akHandle"
fi
