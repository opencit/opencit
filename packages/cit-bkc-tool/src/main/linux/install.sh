#!/bin/bash

# Installation script for CIT BKC Tool. 
# This script is responsible ONLY for ensuring all components are installed locally.
# It DOES NOT run the CIT BKC tool.

# Outline:
# 1. Install Attestation Service 
# 2. Install Trust Agent
# 3. Install CIT BKC Tool

#####

# load installer environment file, if present
if [ -f $HOME/cit.env ]; then
  echo "Loading environment variables from $HOME/cit.env"
  . $HOME/cit.env
  env_file_exports=$(cat $HOME/cit.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
fi


#####
# INSTALL BKC TOOL
cp cit-bkc-tool.sh /usr/local/bin/cit-bkc-tool


#####
# INSTALL ATTESTATION SERVICE
export MTWILSON_LOG_LEVEL=DEBUG

# if Attestation Service is already installed, stop it while we upgrade/reinstall
if which mtwilson; then
  mtwilson stop
fi
if which cit; then
  cit stop
fi

# mtwilson-server-2.1-SNAPSHOT.bin
MTWILSON_BIN=`ls -1 mtwilson-server-*.bin | head -n 1`
if [ -n "$MTWILSON_BIN" ]; then
  cp mtwilson.env $HOME/mtwilson.env
  chmod +x $MTWILSON_BIN
  ./$MTWILSON_BIN
fi


#####
# INSTALL TRUST AGENT
export TAGENT_LOG_LEVEL=DEBUG

# if Trust Agent is already installed, stop it while we upgrade/reinstall
if which tagent; then
  tagent stop
fi

# mtwilson-trustagent-rhel-2.1-20160518.001429-5.bin
TAGENT_BIN=`ls -1 mtwilson-trustagent-*.bin | head -n 1`
if [ -n "$TAGENT_BIN" ]; then
  cp trustagent.env $HOME/trustagent.env
  chmod +x $TAGENT_BIN
  ./$TAGENT_BIN
fi
