#!/bin/bash
# *** use SPACES not TABS
# Note endorsePassword and ownerPassword are the same in CIT's use cases

if [[ $# < 5 || $# > 6 ]]; then
  echo -e "usage: \n $0 <ownerpasswd> <akpasswd> <akhandle> <ekhandle> <makecredentialoutput> <verbose> "
  echo -e "example: \n $0 0x8101000 0x8101001 EBF4DAD19A9.... verbose"
  exit 2
fi

ownerPasswd=$1
akPasswd=$2
akHandle=$3
ekHandle=$4
credBlobHex=$5
credentialBlobFile=/tmp/mkcredential.out
decryptedCred=/tmp/decrypted.out

echo -n $credBlobHex | xxd -r -p - $credentialBlobFile

tpm2_activatecredential -e $ownerPasswd -P $akPasswd -H $akHandle -k $ekHandle -f $credentialBlobFile -o $decryptedCred -X > /dev/null
if [[ $? != 0 ]]; then
  echo "failed to activate credential"
  exit 1
fi

xxd -ps $decryptedCred | tr -d "\n"
echo
rm -f $credentialBlobFile $decryptedCred
