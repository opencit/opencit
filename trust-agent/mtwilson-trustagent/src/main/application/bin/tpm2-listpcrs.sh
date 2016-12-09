#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 2 || $# > 3 ]]; then
  echo -e "usage: \n  $0 <banktype> <resultfile>\n or\n  $0 <banktype> <resultfile> verbose"
  exit 2
fi

bankType=$1 #SHA1, SHA256, SHA384, SHA512, SM3_256
resultFile=$2 #file pathname to save the result
verbose=$3 #verbose
bankTypeHex=unknown

case $bankType in
  "SHA1") bankTypeHex=0x04;;
  "SHA256") bankTypeHex=0x0B;;
  "SHA384") bankTypeHex=0x0C;;
  "SHA512") bankTypeHex=0x0D;;
  "SM3_256") bankTypeHex=0x12;;
esac

echo -n "List PCR bank ($bankType:$bankTypeHex): "
if [[ $bankTypeHex == unknown ]]; then
  echo "failed: unknown type"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  tpm2_listpcrs -g $bankTypeHex > $resultFile
else
  tpm2_listpcrs -g $bankTypeHex 1> $resultFile 2> /dev/null
fi

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

echo "done."
