#!/bin/bash
# makezip-auto.sh - create a zip archive
# how it works:
# 1. in the maven pom.xml, you add a maven-dependency-plugin to copy into the target folder ${project.build.directory} all the artifacts to install
# 2. in the maven pom.xml, you add a exec-maven-plugin to run this script
# 3. this script automatically looks for the latest rpm and deb file in the target folder. if it chooses the wrong file clean up your target folder.
# 4. this script runs the makeself command to create the self-extracting executable

# workspace is typically "target" and must contain the files to package in the installer including the setup script
workspace="${1}"
projectVersion="${2}"
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
chmod +x $workspace/*.sh 

# check for the makeself tool
makezip=`which zip`
if [ -z "$makezip" ]; then
    echo "Missing zip tool"
    exit 1
fi

# unzip the trustagent-3.0-SNAPSHOT.zip since we are going to zip it again
trustagentZip="trustagent-${projectVersion}.zip"
cd $targetDir/${projectNameVersion}
unzip -o ${trustagentZip}
rm -rf ${trustagentZip}
mv *.cmd bin/
mv *.exe bin/
mv logback.xml.base configuration/

export TMPDIR=~/.tmp

# instead of making a zip file, we run makesis to generate the trustagent windows installer
MAKENSIS=`which makensis`
if [ -z "$MAKENSIS" ]; then
    echo "Missing makensis tool"
    exit 1
fi

cd $targetDir
$MAKENSIS "${projectNameVersion}/nsis/trustagentinstallscript.nsi"
if [ $? -ne 0 ]; then echo "Failed to make the NSI trustagent install script"; exit 2; fi

if [ ! -f "${projectNameVersion}/nsis/Setup_TrustAgent.exe" ]; then
  echo "${projectNameVersion}/nsis/Setup_TrustAgent.exe file does not exist"
  exit 3
fi

mv "${projectNameVersion}/nsis/Setup_TrustAgent.exe" "${projectNameVersion}.exe"

# This is not necessary, but to zip it
$makezip -r "${projectNameVersion}.zip" "${projectNameVersion}"