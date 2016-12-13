#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 1 || $# > 2 ]]; then
  echo -e "usage: \n  $0 <aktype>\n or\n  $0 <aktype> verbose"
  exit 2
fi

akType=$1 #RSA, ECC
verbose=$2 #verbose
akTypeHex=unknown
tmpFile=/tmp/persistentobject

case $akType in
  "RSA") akTypeHex=0x1;;
  "ECC") akTypeHex=0x23;;
esac

echo -n "Find AK ($akType:$akTypeHex): "
if [[ $akTypeHex == unknown ]]; then
  echo "failed: unknown type"
  exit 1
fi

rm -rf $tmpFile
tpm2_listpersistent > $tmpFile
if [[ $? != 0 ]];then
  echo "failed: unable to list persistent handles"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo
  cat $tmpFile
fi

for ((i=0x81018; i<0x81020; i++))
do
  j=`printf '0x%05x\n' $i`
  result=`grep -B2 "Type: $akTypeHex" $tmpFile | grep -o "$j..." | head -n1`
  if [ -n $result ]; then
    break
  fi
done

if [ -z $result ]; then
  echo "failed"
  exit 1
fi

echo "done. Found @ $result"
