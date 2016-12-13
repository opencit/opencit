#!/bin/sh

# the cit-bkc-tool installer outline:
# 1. create a destination folder, copy all installer contents to this destination folder
# 2. run the "install.sh" script from the destination folder
#    that script will run subordinate installers and automatically resume after reboot

source ./cit-bkc-tool.env

chmod +x *.sh *.bin

pwd=$(pwd)
if [ "$pwd" != "$CIT_BKC_PACKAGE_PATH" ]; then
    rm -rf $CIT_BKC_PACKAGE_PATH
    mkdir -p $CIT_BKC_PACKAGE_PATH
    cp * $CIT_BKC_PACKAGE_PATH
fi

mkdir -p $CIT_BKC_BIN_PATH
rm -f $CIT_BKC_BIN_PATH/cit-bkc-tool
cp cit-bkc-tool.sh $CIT_BKC_BIN_PATH/cit-bkc-tool

mkdir -p $CIT_BKC_CONF_PATH
if [ -f $CIT_BKC_CONF_PATH/cit-bkc-tool.env ]; then
  cp cit-bkc-tool.env $CIT_BKC_CONF_PATH/cit-bkc-tool.env.new
else
  cp cit-bkc-tool.env $CIT_BKC_CONF_PATH
fi

$CIT_BKC_BIN_PATH/cit-bkc-tool
