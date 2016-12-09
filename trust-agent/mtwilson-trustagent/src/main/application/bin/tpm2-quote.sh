#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 5 || $# > 6 ]]; then
  echo -e "usage: \n  $0 <akhandle> <akpasswd> <banktype> <pcrlist> <quotefile>\n or\n  $0 <akhandle> <akpasswd> <banktype> <pcrlist> <quotefile> verbose"
  exit 2
fi

akHandle=$1
akPasswd=$2
bankType=$3 #SHA1, SHA256, SHA384, SHA512, SM3_256
pcrList=$4 #like "17,18,19"
quoteFile=$5
verbose=$6 #verbose
bankTypeHex=unknown

case $bankType in
  "SHA1") bankTypeHex=0x04;;
  "SHA256") bankTypeHex=0x0B;;
  "SHA384") bankTypeHex=0x0C;;
  "SHA512") bankTypeHex=0x0D;;
  "SM3_256") bankTypeHex=0x12;;
esac

echo -n "Create quote ($bankType:$bankTypeHex,'$pcrList'): "
if [[ $bankTypeHex == unknown ]]; then
  echo "failed: unknown type"
  exit 1
fi

rm -f $quoteFile
if [[ $verbose == "verbose" ]]; then
  tpm2_quote -k  $akHandle -P $akPasswd -g $bankTypeHex -l $pcrList -o $quoteFile -X
else
  tpm2_quote -k  $akHandle -P $akPasswd -g $bankTypeHex -l $pcrList -o $quoteFile -X > /dev/null
fi

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

echo "done"
