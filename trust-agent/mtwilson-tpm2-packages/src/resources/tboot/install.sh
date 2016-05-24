#!/bin/bash

# install tboot.gz and grub conf
[ -d //boot ] || install -d -m0755 -p //boot
install -m0644 -p tboot/tboot.gz /boot/tboot.gz
install -m0644 -p tboot/tboot-syms /boot/tboot-syms
[ -d /etc/grub.d ] || install -d -m0755 -p /etc/grub.d
install -m755 -t /etc/grub.d tboot/20*

# install utils
[ -d /usr/sbin ] || install -d -m0755 -p /usr/sbin
install -s -m0755 -p -t //usr/sbin utils/txt-stat
[ -d //usr/sbin ] || install -d -m0755 -p /usr/sbin
install -s -m0755 -p -t //usr/sbin utils/parse_err
[ -d /usr/sbin ] || install -d -m0755 -p /usr/sbin
install -s -m0755 -p -t //usr/sbin utils/acminfo


