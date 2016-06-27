#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 1 || $# > 2 ]]; then
  echo -e "usage: \n  $0 <ektype>\n or\n  $0 <ektype> verbose"
  exit 2
fi

ekType=$1 #RSA, ECC
verbose=$2 #verbose
ekTypeHex=unknown
tmpFile=/tmp/nvindex

case $ekType in
  "RSA") ekTypeHex=0x1;;
  "ECC") ekTypeHex=0x23;;
esac

echo -n "Find EC ($ekType:$ekTypeHex): "
if [[ $ekTypeHex == unknown ]]; then
  echo "failed: unknown type"
  exit 1
fi

rm -rf $tmpFile
tpm2_nvlist > $tmpFile
if [[ $? != 0 ]];then
  echo "failed: unable to list nv indices"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo
  cat $tmpFile
fi

for ((i=0x01c00; i<0x01c08; i++))
do
  j=`printf '0x%x\n' $i`
  result=`grep "NV Index: $j..." $tmpFile | head -n1 | grep -o "$j..."`
  if [ -n $result ]; then
    break
  fi
done

if [ -z $result ]; then
  echo "failed"
  exit 1
fi

echo "done. Found @ $result"
