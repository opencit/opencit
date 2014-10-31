#!/bin/bash
# decrypt.sh - decrypt and verify with hmac
# USAGE:
#       decrypt.sh --enc encpassword --auth authpassword encrypted.zip
#       decrypt.sh -enc encpassword -auth authpassword encrypted.zip
#       decrypt.sh -e encpassword -a authpassword encrypted.zip
# The zip file will be decrypted and its contents extracted to a
# folder named encrypted.zip.d
# 
# NOTES:
# assumes the encrypted file has the following structure:
#Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"
#Date: Wed Nov 13 00:19:58 PST 2013
#
#<base64 content here>
# Assumes the signature files have the following structure:
#Content-Type: application/signature.openssl; alg="hmac"; digest-alg="sha256"
#Link: <plain.txt>; rel=signed
#Date: Wed Nov 13 00:19:58 PST 2013
#
#07f2b754414cdbc0e7edadb66342a45d4eee1c0f354e316186e92b1c2eb42be4
# 
# Currently the hmac only covers the signed document, not any of the headers
# in the signature file itself.


PBKDF2_OPTS=
DEBUG=

parse_args() {
  if ! options=$(getopt -a -n decrypt.sh -l passwd:,enc:,auth: -o p:,e:,a: -- "$@"); then exit 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      -p|--passwd) eval ENC_PASSWORD="\$$2"; eval AUTH_PASSWORD="\$$2"; shift;;
      -e|--enc) eval ENC_PASSWORD="\$$2"; shift;;
      -a|--auth) eval AUTH_PASSWORD="\$$2"; shift;;
      --) INFILE="$2"; shift;;
    esac
    shift
  done
}

parse_args $@

decrypt_encfile() {
  local encfile="$1"
  #local txtfile="$infile.txt"
  local txtfile="$2"

# find the first content-type header to extract some info we need for decryption and signature verification
# Example content-types:
# Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"; key-gen="pbkdf2"; iter="2052228"
# Content-Type: application/signature.openssl; alg="hmac"; digest-alg="sha256"
  enc_content_type=`cat $encfile | grep "^Content-Type:"`
  enc_alg=`cat $encfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
  enc_digest_alg=`cat $encfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^digest-alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
  enc_key_gen=`cat $encfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^key-gen=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
  enc_iter=`cat $encfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^iter=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
  enc_enclosed=`cat $encfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^enclosed=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`

  if [[ "$enc_key_gen" == "pbkdf2" ]]; then
    PBKDF2_OPTS="$PBKDF2_OPTS -pbkdf2"
  fi
  if [[ -n "$enc_iter" ]]; then
    PBKDF2_OPTS="$PBKDF2_OPTS -c $enc_iter"
  fi


if [[ "$DEBUG" ]]; then
  echo "enc_alg: $enc_alg"
  echo "enc_digest_alg: $enc_digest_alg"
  echo "enc_key_gen: $enc_key_gen"
  echo "enc_iter: $enc_iter"
  # example:
  #enc_alg: aes-256-ofb
  #enc_digest_alg: sha256
  #enc_key_gen: pbkdf2
  #enc_iter: 2052228
fi

export ENC_PASSWORD
export AUTH_PASSWORD

  # decrypt using the hints in the content type parameters
  cat $encfile | awk 'BEGIN { RS="\n\n" } NR==2 { print }' | openssl enc -d -$enc_alg -pass env:ENC_PASSWORD -md $enc_digest_alg $PBKDF2_OPTS -base64 > $txtfile
  
}

verify_signature() {
  local infile="$1"
  local sigfile="$2"
  local docfile="$sigfile.doc"

# find the first content-type header to extract some info we need for decryption and signature verification
# Example content-type:
# Content-Type: application/signature.openssl; alg="hmac"; digest-alg="sha256"
  sig_content_type=`cat $sigfile | grep "^Content-Type:"`
  sig_alg=`cat $sigfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
  sig_digest_alg=`cat $sigfile | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^digest-alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
 

if [[ "$DEBUG" ]]; then
  echo "sig_content_type: $sig_content_type"
  echo "sig_alg: $sig_alg"
  echo "sig_digest_alg: $sig_digest_alg"
  # example:
  #sig_content_type: application/signature.openssl
  #sig_alg: hmac
  #sig_digest_alg: sha256
fi

# The signature itself is everything after the blank line following the
# signature headers:
#inputmac=$(tail -n 1 $sigfile)
inputmac=$(awk 'BEGIN { RS="\n\n" } NR==2 { print }' $sigfile)
#echo "inputmac:  $inputmac"

 # To calculate the signature we need to recreate the original 
 # document which the signature covers, which consists of:
 # 1. the signature headers, followed by a blank line
 # 2. the original input file
 
 local signature_headers=`cat $sigfile | awk 'BEGIN { RS="\n\n" } NR==1 { print }'`

 echo "$signature_headers" > $docfile
 echo >> $docfile
 cat $infile >> $docfile

# calculate the hmac over the entire input file  
calcmac=$(openssl dgst -$sig_digest_alg -$sig_alg $AUTH_PASSWORD -binary $docfile | openssl enc -base64)
#echo "calcmac: $calcmac"

if [ "$inputmac" != "$calcmac" ]; then
  echo "Message failed verification: $infile"
  exit 1
fi

  # The signature document file (docfile) is a temporary file, so delete it:
  rm $docfile

}

# decrypt the input file
ZIPFILE=$INFILE.zip
decrypt_encfile $INFILE $ZIPFILE

# unzip the decrypted zip file
unzip -o -d $INFILE.d $ZIPFILE 

# validate all the signatures for the contents of the zip file
SIGFILES=`ls -1 $INFILE.d/*.sig`
for sigfile in $SIGFILES
do
  signedfile=$INFILE.d/$(basename $sigfile .sig)
  #echo "found signed file: $signedfile"
  verify_signature $signedfile $sigfile
  echo "Verified $signedfile"
done

