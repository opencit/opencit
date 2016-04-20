#!/bin/bash
# makebin-auto.sh - creates a self-extracting installer
# how it works:
# 1. in the maven pom.xml, you add a maven-dependency-plugin to copy into the target folder ${project.build.directory} all the artifacts to install
# 2. in the maven pom.xml, you add a exec-maven-plugin to run this script
# 3. this script automatically looks for the latest rpm and deb file in the target folder. if it chooses the wrong file clean up your target folder.
# 4. this script runs the makeself command to create the self-extracting executable

# workspace is typically "target" and must contain the files to package in the installer including the setup script
workspace="${1}"
# installer name
projectNameVersion=`basename "${workspace}"`
# where to save the installer (parent of directory containing files)
targetDir=`dirname "${workspace}"`

if [ -z "$workspace" ]; then
  echo "Usage: $0 <workspace>"
  echo "Example: $0 /path/to/AttestationService-0.5.1"
  echo "The self-extracting installer AttestationService-0.5.1.bin would be created in /path/to"
  exit 1
fi

if [ ! -d "$workspace" ]; then echo "Cannot find workspace '$workspace'"; exit 1; fi

# ensure all executable files in the target folder have the x bit set
chmod +x $workspace/*.sh $workspace/*.bin

# check for the makeself tool
makeself=`which makeself`
if [ -z "$makeself" ]; then
    echo "Missing makeself tool"
    exit 1
fi

export TMPDIR=~/.tmp
$makeself --follow --nocomp "$workspace" "$targetDir/${projectNameVersion}.bin" "$projectNameVersion" ./setup.sh
