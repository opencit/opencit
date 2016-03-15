#!/bin/bash

# define action usage commands
usage() { echo "Usage: $0 [-v \"version\"]" >&2; exit 1; }

# set option arguments to variables and echo usage on failures
version=
while getopts ":v:" o; do
  case "${o}" in
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      ;;
    *)
      usage
      ;;
  esac
done

if [ -z "$version" ]; then
  echo "Version not specified" >&2
  exit 2
fi

changeVersionCommand="mvn versions:set -DnewVersion=${version}"
changeParentVersionCommand="mvn versions:update-parent -DnewVersion=${version}"
$changeVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven version at top level" >&2; exit 3; fi
(cd integration && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"integration\" folder" >&2; exit 3; fi
(cd services && $changeVersionCommand) # && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"services\" folder" >&2; exit 3; fi
