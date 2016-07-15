#!/bin/bash
# copy over the tss2 library and executables

#install libs
 /bin/mkdir -p '/usr/local/lib'
 /usr/bin/install -c lib/libtss2.la lib/libtctidevice.la lib/libtctisocket.la '/usr/local/lib'
 /usr/bin/install -c lib/libtss2.so.0.0.0 /usr/local/lib/libtss2.so.0.0.0
 (cd /usr/local/lib && { ln -s -f libtss2.so.0.0.0 libtss2.so.0 || { rm -f libtss2.so.0 && ln -s libtss2.so.0.0.0 libtss2.so.0; }; })
 (cd /usr/local/lib && { ln -s -f libtss2.so.0.0.0 libtss2.so || { rm -f libtss2.so && ln -s libtss2.so.0.0.0 libtss2.so; }; })
 /usr/bin/install -c lib/libtss2.la /usr/local/lib/libtss2.la
 /usr/bin/install -c lib/libtctidevice.so.0.0.0 /usr/local/lib/libtctidevice.so.0.0.0
 (cd /usr/local/lib && { ln -s -f libtctidevice.so.0.0.0 libtctidevice.so.0 || { rm -f libtctidevice.so.0 && ln -s libtctidevice.so.0.0.0 libtctidevice.so.0; }; })
 (cd /usr/local/lib && { ln -s -f libtctidevice.so.0.0.0 libtctidevice.so || { rm -f libtctidevice.so && ln -s libtctidevice.so.0.0.0 libtctidevice.so; }; })
 /usr/bin/install -c lib/libtctidevice.la /usr/local/lib/libtctidevice.la
 /usr/bin/install -c lib/libtctisocket.so.0.0.0 /usr/local/lib/libtctisocket.so.0.0.0
 (cd /usr/local/lib && { ln -s -f libtctisocket.so.0.0.0 libtctisocket.so.0 || { rm -f libtctisocket.so.0 && ln -s libtctisocket.so.0.0.0 libtctisocket.so.0; }; })
 (cd /usr/local/lib && { ln -s -f libtctisocket.so.0.0.0 libtctisocket.so || { rm -f libtctisocket.so && ln -s libtctisocket.so.0.0.0 libtctisocket.so; }; })
 /usr/bin/install -c lib/libtctisocket.la /usr/local/lib/libtctisocket.la
 /usr/bin/install -c lib/libtss2.a /usr/local/lib/libtss2.a
 chmod 644 /usr/local/lib/libtss2.a
 ranlib /usr/local/lib/libtss2.a
 /usr/bin/install -c lib/libtctidevice.a /usr/local/lib/libtctidevice.a
 chmod 644 /usr/local/lib/libtctidevice.a
 ranlib /usr/local/lib/libtctidevice.a
 /usr/bin/install -c lib/libtctisocket.a /usr/local/lib/libtctisocket.a
 chmod 644 /usr/local/lib/libtctisocket.a
 ranlib /usr/local/lib/libtctisocket.a
 export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/sbin"
 ldconfig -n /usr/local/lib

#install resourcemgr
 /bin/mkdir -p '/usr/local/sbin'
 /usr/bin/install -c sbin/resourcemgr '/usr/local/sbin'

#install header files
 /bin/mkdir -p '/usr/local/include/tcti'
 /usr/bin/install -c -m 644 include/tcti/common.h include/tcti/tcti_device.h include/tcti/tcti_socket.h '/usr/local/include/tcti'
 /bin/mkdir -p '/usr/local/include/tss2'
 /usr/bin/install -c -m 644 include/tss2/implementation.h include/tss2/sys_api_part3.h include/tss2/tpm20.h include/tss2/tpmb.h include/tss2/tss2_common.h include/tss2/tss2_sys.h include/tss2/tss2_tcti.h include/tss2/tss2_tpm2_types.h '/usr/local/include/tss2'

# install the tcsd2.service
cp tcsd2.service /lib/systemd/system
systemctl enable tcsd2.service
systemctl start tcsd2.service
