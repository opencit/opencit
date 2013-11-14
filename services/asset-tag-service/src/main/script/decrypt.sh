#!/bin/sh
# decrypt.sh - decrypt and verify with hmac
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

parse_args() {
  if ! options=$(getopt -n encrypt.sh -l infile:,outfile:,enc-passwd:,auth-passwd: -- "$@"); then exit 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      --infile) INFILE="$2"; shift;;
      --outfile) OUTFILE="$2"; shift;;
      --enc-passwd) ENC_PASSWORDFILE="$2"; shift;;
      --auth-passwd) AUTH_PASSWORDFILE="$2"; shift;;
    esac
    shift
  done
}

parse_args $@

# assume the mac value is the last line of the file
inputmac=$(tail -n 1 $INFILE)
#echo $inputmac

# calculate the hmac over the entire file except fo rthe last line
calcmac=$(head -n -1 $INFILE | openssl dgst -sha256 -hmac file:AUTH_PASSWORDFILE -hex | awk '{ print $2 }')
#echo $calcmac

if [ "$inputmac" != "$calcmac" ]; then
  echo "Message failed verification"
  exit 1
fi

# decrypt the base64 content but first need to extract it from message:
# skip three lines from the beginning (content-type, date, and blank)
# and skip three lines from the end (content-type, blank line, and hmac)
tail -n +3 $INFILE | head -n -3 | openssl enc -d -aes-256-ofb -pass file:ENC_PASSWORDFILE -md sha256 -base64 > $OUTFILE
