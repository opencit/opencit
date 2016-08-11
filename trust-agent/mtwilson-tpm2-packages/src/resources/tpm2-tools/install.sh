#!/bin/bash

# copy over the tpm2-tools to /usr/local/sbin
chmod 0755 sbin/*
cp -f sbin/* /usr/local/sbin/
chmod 0755 /usr/local/sbin/tpm2*