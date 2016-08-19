#!/bin/bash
# copy over the tss2 library and executables

#install libs
 /bin/mkdir -p '/usr/local/lib'
 /usr/bin/install -c lib/libsapi.la lib/libtcti-device.la lib/libtcti-socket.la '/usr/local/lib'

 /usr/bin/install -c lib/libsapi.so.0.0.0 /usr/local/lib/libsapi.so.0.0.0
 (cd /usr/local/lib && { ln -s -f libsapi.so.0.0.0 libsapi.so.0 || { rm -f libsapi.so.0 && ln -s libsapi.so.0.0.0 libsapi.so.0; }; })
 (cd /usr/local/lib && { ln -s -f libsapi.so.0.0.0 libsapi.so || { rm -f libsapi.so && ln -s libsapi.so.0.0.0 libsapi.so; }; })
 /usr/bin/install -c lib/libsapi.la /usr/local/lib/libsapi.la

 /usr/bin/install -c lib/libtcti-device.so.0.0.0 /usr/local/lib/libtcti-device.so.0.0.0
 (cd /usr/local/lib && { ln -s -f libtcti-device.so.0.0.0 libtcti-device.so.0 || { rm -f libtcti-device.so.0 && ln -s libtcti-device.so.0.0.0 libtcti-device.so.0; }; })
 (cd /usr/local/lib && { ln -s -f libtcti-device.so.0.0.0 libtcti-device.so || { rm -f libtcti-device.so && ln -s libtcti-device.so.0.0.0 libtcti-device.so; }; })
 /usr/bin/install -c lib/libtcti-device.la /usr/local/lib/libtcti-device.la

 /usr/bin/install -c lib/libtcti-socket.so.0.0.0 /usr/local/lib/libtcti-socket.so.0.0.0
 (cd /usr/local/lib && { ln -s -f libtcti-socket.so.0.0.0 libtcti-socket.so.0 || { rm -f libtcti-socket.so.0 && ln -s libtcti-socket.so.0.0.0 libtcti-socket.so.0; }; })
 (cd /usr/local/lib && { ln -s -f libtcti-socket.so.0.0.0 libtcti-socket.so || { rm -f libtcti-socket.so && ln -s libtcti-socket.so.0.0.0 libtcti-socket.so; }; })
 /usr/bin/install -c lib/libtcti-socket.la /usr/local/lib/libtcti-socket.la

 /usr/bin/install -c lib/libsapi.a /usr/local/lib/libsapi.a
 chmod 644 /usr/local/lib/libsapi.a
 ranlib /usr/local/lib/libsapi.a
 /usr/bin/install -c lib/libtcti-device.a /usr/local/lib/libtcti-device.a
 chmod 644 /usr/local/lib/libtcti-device.a
 ranlib /usr/local/lib/libtcti-device.a
 /usr/bin/install -c lib/libtcti-socket.a /usr/local/lib/libtcti-socket.a
 chmod 644 /usr/local/lib/libtcti-socket.a
 ranlib /usr/local/lib/libtcti-socket.a
 export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/sbin"

#Add /usr/local/lib to Library Path
echo "/usr/local/lib" > /etc/ld.so.conf.d/tss2.conf
ldconfig

#install resourcemgr
 /bin/mkdir -p '/usr/local/sbin'
 /usr/bin/install -c -m 755 sbin/resourcemgr '/usr/local/sbin'

#install header files
 /bin/mkdir -p '/usr/local/include/tcti'
 /usr/bin/install -c -m 644 include/tcti/common.h include/tcti/tcti_device.h include/tcti/tcti_socket.h '/usr/local/include/tcti'
 /bin/mkdir -p '/usr/local/include/sapi'
 /usr/bin/install -c -m 644 include/sapi/implementation.h include/sapi/sys_api_part3.h include/sapi/tpm20.h include/sapi/tpmb.h include/sapi/tss2_common.h include/sapi/tss2_sys.h include/sapi/tss2_tcti.h include/sapi/tss2_tpm2_types.h '/usr/local/include/sapi'

# install the tcsd2.service
cp tcsd2.service /lib/systemd/system
systemctl enable tcsd2.service
systemctl daemon-reload
#systemctl stop tcsd2.service
systemctl start tcsd2
