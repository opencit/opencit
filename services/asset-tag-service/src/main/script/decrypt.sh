#!/bin/sh
# decrypt.sh - decrypt and verify with hmac
# USAGE:
#       decrypt.sh --in encrypted.txt --out plain.txt --enc encpassword --auth authpassword
#       decrypt.sh -in encrypted.txt -out plain.txt -enc encpassword -auth authpassword
#       decrypt.sh -i encrypted.txt -o plain.txt -e encpassword -a authpassword
# NOTES:
# assumes the input file has the following structure:
#Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"
#Date: Wed Nov 13 00:19:58 PST 2013
#
#<base64 content here>
#-----
#Content-Type: application/signature.openssl; alg="hmac"; digest-alg="sha256"
#
#07f2b754414cdbc0e7edadb66342a45d4eee1c0f354e316186e92b1c2eb42be4
# the hmac is over the entire file including the newline right before
# the line where the hmac itself is appended.
# if you add any other headers to the file (to be included in the hmac)
# you need to adjust the +/- numbers on the head and tail commands below.

PBKDF2_OPTS=
DEBUG=

parse_args() {
  if ! options=$(getopt -a -n decrypt.sh -l in:,out:,enc:,auth: -o i:,o:,e:,a: -- "$@"); then exit 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      -i|--in) INFILE="$2"; shift;;
      -o|--out) OUTFILE="$2"; shift;;
      -e|--enc) ENC_PASSWORD="$2"; shift;;
      -a|--auth) AUTH_PASSWORD="$2"; shift;;
    esac
    shift
  done
}

parse_args $@

# check if input file is base64 encoded, and decode it first if necessary
infile_basename=$(basename $INFILE .base64)
if [[ "$INFILE" != "$infile_basename" ]]; then
  cat $INFILE | base64 -d > $infile_basename
  INFILE=$infile_basename
fi

# find the first content-type header to extract some info we need for decryption and signature verification
# Example content-types:
# Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"; key-gen="pbkdf2"; iter="2052228"
# Content-Type: application/signature.openssl; alg="hmac"; digest-alg="sha256"
enc_content_type=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==1 { print }' | grep "^Content-Type:"`
enc_alg=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==1 { print }' | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
enc_digest_alg=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==1 { print }' | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^digest-alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
enc_key_gen=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==1 { print }' | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^key-gen=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
enc_iter=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==1 { print }' | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^iter=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
sig_content_type=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==2 { print }' | grep "^Content-Type:"`
sig_alg=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==2 { print }' | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`
sig_digest_alg=`cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==2 { print }' | grep "^Content-Type:" | awk 'BEGIN { RS="; " } /^digest-alg=/ { print }' | awk 'BEGIN { FS="=" } { print $2 }' | tr -d \"`

if [[ "$DEBUG" ]]; then
  echo "enc_alg: $enc_alg"
  echo "enc_digest_alg: $enc_digest_alg"
  echo "enc_key_gen: $enc_key_gen"
  echo "enc_iter: $enc_iter"
  echo "sig_alg: $sig_alg"
  echo "sig_digest_alg: $sig_digest_alg"
  # example:
  #enc_alg: aes-256-ofb
  #enc_digest_alg: sha256
  #enc_key_gen: pbkdf2
  #enc_iter: 2052228
  #sig_alg: hmac
  #sig_digest_alg: sha256
fi

if [[ "$enc_key_gen" == "pbkdf2" ]]; then
  PBKDF2_OPTS="$PBKDF2_OPTS -pbkdf2"
fi
if [[ -n "$enc_iter" ]]; then
  PBKDF2_OPTS="$PBKDF2_OPTS -c $enc_iter"
fi

# assume the mac value is the last line of the file
inputmac=$(tail -n 1 $INFILE)
#echo $inputmac

# calculate the hmac over the entire file except for the last line
calcmac=$(head -n -1 $INFILE | openssl dgst -$sig_digest_alg -$sig_alg $AUTH_PASSWORD -hex | awk '{ print $2 }')
#echo $calcmac

if [ "$inputmac" != "$calcmac" ]; then
  echo "Message failed verification"
  exit 1
fi

export ENC_PASSWORD

# decrypt the base64 content but first need to extract it from message:
cat $INFILE |  awk 'BEGIN { RS="-----\n" } NR==1 { print }' |  awk 'BEGIN { RS="\n\n" } NR==2 { print }' | openssl enc -d -$enc_alg -pass env:ENC_PASSWORD -md $enc_digest_alg $PBKDF2_OPTS -base64 > $OUTFILE
