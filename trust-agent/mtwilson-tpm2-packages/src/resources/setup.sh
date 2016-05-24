#!/bin/bash

#install tboot
cd tboot
./install.sh
cd ..

#install tss2
cd tss2
./install.sh
cd ..

#install tpm2 tools
cd tpm2-tools
./install.sh
cd ..

