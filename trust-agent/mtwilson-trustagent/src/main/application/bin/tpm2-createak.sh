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
akHandle=

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

function get_next_usable_persistent_handle()
{
  rm -rf $tmpFile
  tpm2_listpersistent > $tmpFile
  if [[ $? != 0 ]];then
    echo "failed: unable to list persistent handles"
    return 1
  fi

  #if [[ $verbose == "verbose" ]]; then
  #  echo
  #  cat $tmpFile
  #fi

  result=`grep -o "0x8101...." $tmpFile`
  #echo $result

  for ((i=0x81018000; i<=0x8101ffff; i++))
  do
    j=`printf '0x%08x\n' $i`
    if [ -z `echo $result | grep -o "$j"` ]; then
      echo "$j"
      return 0
    fi
  done

  echo "no usable persistent handle"
  return 1
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

akHandle=`get_next_usable_persistent_handle`
if [[ $? != 0 ]]; then
  echo "failed: no usable persistent handle"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo "akHandle = $akHandle"
  tpm2_getpubak -e $endorsePasswd -o $ownerPasswd -P $akPasswd -E $ekHandle -k $akHandle -f $akFile -n $akNameFile -g $akTypeHex -X
else
  tpm2_getpubak -e $endorsePasswd -o $ownerPasswd -P $akPasswd -E $ekHandle -k $akHandle -f $akFile -n $akNameFile -g $akTypeHex -X > /dev/null
fi

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

output_result $akType $akHandle $akFile $akName

if [[ $verbose == "verbose" ]]; then
  echo "done. Created @ $akHandle"
fi
