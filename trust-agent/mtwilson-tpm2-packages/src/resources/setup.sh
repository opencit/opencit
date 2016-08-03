#!/bin/bash

#skip the installation of tboot. it should be installed and tested before TA is installed.
#cd tboot
#./install.sh
#cd ..

#install tss2
cd tss2
./install.sh
cd ..

#install tpm2 tools
cd tpm2-tools
./install.sh
cd ..

