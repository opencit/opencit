#!/bin/bash

# Preconditions:
# * http_proxy and https_proxy are already set, if required
# * date and time are synchronized with remote server, if using remote attestation service
# * the mtwilson linux util functions already sourced
#   (for add_package_repository, echo_success, echo_failure)
# * TRUSTAGENT_HOME is set, for example /opt/trustagent
# * TRUSTAGENT_INSTALL_LOG_FILE is set, for example /opt/trustagent/logs/install.log
# * TPM_VERSION is set, for example 1.2 or else it will be auto-detected

# Postconditions:
# * All messages logged to stdout/stderr; caller redirect to logfile as needed

# NOTE:  \cp escapes alias, needed because some systems alias cp to always prompt before override

# Outline:
# 1. Add epel-release-latest-7.noarch repository
# 2. Install redhat-lsb-core and other redhat-specific packages
# 3. Install tboot
# 4. Install trousers and trousers-devel packages (current is trousers-0.3.13-1.el7.x86_64)
# 5. Install the patched tpm-tools
# 6. Add grub menu item for tboot and select as default
# 7. Ask for reboot (only if we are not already in trusted boot)
# 8. Start tcsd (it already has an init script for next boot, but we need it now)

# source functions file
if [ -f functions ]; then . functions; fi

TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-/opt/trustagent}
LOGFILE=${TRUSTAGENT_INSTALL_LOG_FILE:-$TRUSTAGENT_HOME/logs/install.log}
mkdir -p $(dirname $LOGFILE)

# identify tpm version
# postcondition:
#   variable TPM_VERSION is set to 1.2 or 2.0
detect_tpm_version() {
  export TPM_VERSION
  if [[ -f "/sys/class/misc/tpm0/device/caps" || -f "/sys/class/tpm/tpm0/device/caps" ]]; then
    TPM_VERSION=1.2
  else
  #  if [[ -f "/sys/class/tpm/tpm0/device/description" && `cat /sys/class/tpm/tpm0/device/description` == "TPM 2.0 Device" ]]; then
    TPM_VERSION=2.0
  fi
}

if [ -z "$TPM_VERSION" ]; then
  detect_tpm_version
fi

################################################################################

if yum_detect; then
  # 1. Add epel-release-latest-7.noarch repository
  add_package_repository https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
  # 2. Install redhat-lsb-core and other redhat-specific packages
  yum -y install redhat-lsb redhat-lsb-core libvirt net-tools grub2-efi-modules > /dev/null 2>&1
#elif aptget_detect; then
#  
fi

# 3. Install tboot
# 4. Install trousers and trousers-devel packages (current is trousers-0.3.13-1.el7.x86_64)
# 5. Install the patched tpm-tools

# tpm 1.2
is_tboot_installed() {
  is_package_installed tboot
  result=$?
  if [ $result -ne 0 ]; then
    if [ -f /boot/tboot.gz ]; then
      return 0
    else
      return 1
    fi
  fi
}

# tpm 1.2
install_tboot() {
  TRUSTAGENT_TBOOT_YUM_PACKAGES="tboot"
  TRUSTAGENT_TBOOT_APT_PACKAGES="tboot"
  TRUSTAGENT_TBOOT_YAST_PACKAGES="tboot"
  TRUSTAGENT_TBOOT_ZYPPER_PACKAGES="tboot"
  auto_install "tboot" "TRUSTAGENT_TBOOT"
}

install_openssl() {
  TRUSTAGENT_OPENSSL_YUM_PACKAGES="openssl openssl-devel"
  TRUSTAGENT_OPENSSL_APT_PACKAGES="openssl libssl-dev"
  TRUSTAGENT_OPENSSL_YAST_PACKAGES="openssl libopenssl-devel"
  TRUSTAGENT_OPENSSL_ZYPPER_PACKAGES="openssl libopenssl-devel libopenssl1_0_0 openssl-certs"
  auto_install "openssl" "TRUSTAGENT_OPENSSL" > /dev/null 2>&1
}

# tpm 1.2
install_trousers() {
  TRUSTAGENT_TROUSERS_YUM_PACKAGES="trousers trousers-devel"
  TRUSTAGENT_TROUSERS_APT_PACKAGES="trousers trousers-dbg libtspi-dev libtspi1"
  TRUSTAGENT_TROUSERS_YAST_PACKAGES="trousers trousers-devel"
  TRUSTAGENT_TROUSERS_ZYPPER_PACKAGES="trousers trousers-devel"
  auto_install "trousers" "TRUSTAGENT_TROUSERS" > /dev/null 2>&1
}

# tpm 1.2
install_tpm_tools() {
  TRUSTAGENT_TPMTOOLS_YUM_PACKAGES="tpm-tools"
  TRUSTAGENT_TPMTOOLS_APT_PACKAGES="tpm-tools"
  TRUSTAGENT_TPMTOOLS_YAST_PACKAGES="tpm-tools"
  TRUSTAGENT_TPMTOOLS_ZYPPER_PACKAGES="tpm-tools"
  auto_install "tpm-tools" "TRUSTAGENT_TPMTOOLS" > /dev/null 2>&1
}

# tpm 1.2
install_patched_tpm_tools() {
  local PATCHED_TPMTOOLS_BIN=`ls -1 patched-*.bin | head -n 1`
  if [ -n "$PATCHED_TPMTOOLS_BIN" ]; then
    chmod +x $PATCHED_TPMTOOLS_BIN
    ./$PATCHED_TPMTOOLS_BIN
  fi
}

# tpm 2.0
# install tboot 1.9.4 for tpm2
# NOTE: eventually these should be available via package managers on central
#       repositories, when that happens we can use code like install_tboot
install_tboot_tpm2() {
  if yum_detect; then
    local TBOOT_RPM=`ls -1 tboot-*.rpm | head -n 1`
    if [ -n "$TBOOT_RPM" ]; then
      yum -y install $TBOOT_RPM
    fi
  elif aptget_detect; then
    local TBOOT_DEB=`ls -1 tboot-*.deb | head -n 1`
    if [ -n "$TBOOT_DEB" ]; then
      dpkg -i $TBOOT_DEB
      apt-get install -f
    fi
  fi
}

# tpm 2.0
install_tss2_tpmtools2() {
  #install tpm2-tss, tpm2-tools for tpm2
  # (do not install trousers and its dev packages for tpm 2.0)
  ./mtwilson-tpm2-packages-*.bin
}

install_openssl

if [ "$TPM_VERSION" == "1.2" ]; then
  if is_tboot_installed; then
    echo "tboot already installed"
  else
    install_tboot
  fi
  install_trousers
  install_tpm_tools
  install_patched_tpm_tools
elif [ "$TPM_VERSION" == "2.0" ]; then
  if is_tboot_installed; then
    echo "tboot already installed"
  else
    #install_tboot_tpm2
    echo_failure "tboot 1.9.5 or later must be installed for TPM 2.0 functionality"
    exit 5
  fi
  install_tss2_tpmtools2
elif [ -z "$TPM_VERSION" ]; then
  echo "Cannot detect TPM version"
else
  echo "Unrecognized TPM version: $TPM_VERSION"
fi



# TODO TPM2.0
#    2. install tboot 1.9.4 rpm package 
#       ? Download tboot_1.9.4-3.hd.x86_64.rpm to the host from \\10.1.68.140\cit\2.2 
#       ? Install tboot ( #rpm -i tboot_1.9.4-3.hd.x86_64.rpm )
#       * change the boot entry to tboot by editting the line GRUB_DEFAULT in file /etc/defaut/grub
#       * Run "grub2-mkconfig -o /boot/grub2/grub.cfg" command
#       ? Reboot the host server and make sure TXT is enabled and tboot boots correctly

# 6. Add grub menu item for tboot and select as default

is_uefi_boot() {
  if [ -d /sys/firmware/efi ]; then
    return 0
  else
    return 1
  fi
}

define_grub_file() {
  if is_uefi_boot; then
    if [ -f "/boot/efi/EFI/redhat/grub.cfg" ]; then
      DEFAULT_GRUB_FILE="/boot/efi/EFI/redhat/grub.cfg"
    else
      DEFAULT_GRUB_FILE="/boot/efi/EFI/ubuntu/grub.cfg"
    fi
  else
    if [ -f "/boot/grub2/grub.cfg" ]; then
      DEFAULT_GRUB_FILE="/boot/grub2/grub.cfg"
    else
      DEFAULT_GRUB_FILE="/boot/grub/grub.cfg"
    fi
  fi
  GRUB_FILE=${GRUB_FILE:-$DEFAULT_GRUB_FILE}
}

configure_grub() {
  define_grub_file
  
  # /etc/default/grub appears in both ubuntu and redhat
  if [ -f /etc/default/grub ]; then
    update_property_in_file GRUB_DEFAULT /etc/default/grub 0
  else
    echo "cannot update grub default boot selection in /etc/default/grub"
  fi

  if [ -f /etc/grub.d/20_linux_tboot ]; then
    mv /etc/grub.d/20_linux_tboot /etc/grub.d/05_linux_tboot
  elif [ -f /etc/grub.d/05_linux_tboot ]; then
    echo "already moved tboot menuentry to first position in /etc/grub.d"
  else
    echo "cannot find tboot menuentry in /etc/grub.d"
  fi

  # copy grub2-efi-modules into the modules directory
  if [ -d /boot/efi/EFI/redhat ]; then
    mkdir -p /boot/efi/EFI/redhat/x86_64-efi
  fi
  if [ -f /usr/lib/grub/x86_64-efi/relocator.mod ] && [ -d /boot/efi/EFI/redhat/x86_64-efi ]; then
    \cp /usr/lib/grub/x86_64-efi/relocator.mod /boot/efi/EFI/redhat/x86_64-efi/
  fi
  if [ -f /usr/lib/grub/x86_64-efi/multiboot2.mod ] && [ -d /boot/efi/EFI/redhat/x86_64-efi ]; then
    \cp /usr/lib/grub/x86_64-efi/multiboot2.mod /boot/efi/EFI/redhat/x86_64-efi/
  fi

  grub2-mkconfig -o $GRUB_FILE
}

# TODO TPM2.0 :
#    1. tpm2.0 driver: enable tpm2.0 driver on the host
#       First check if /dev/tpm0 exist. if yes, go to next step, otherwise, follow the instruction below
#       NOTE: tpm2.0 driver is not loaded correctly by the default OS installation. need to manually enable it
#       * edit /etc/default/grub to add the parameter "tpm_tis.force=1" at the end of the line GRUB_CMDLINE_LINUX
#         for example: GRUB_CMDLINE_LINUX="crashkernel=auto rd.lvm.lv=rhel/root rd.lvm.lv=rhel/swap rhgb quiet tpm_tis.force=1"
#       * grub2-mkconfig -o /boot/grub2/grub.cfg
#       * reboot the host
#       * check if /dev/tpm0 exist. if so, go to next step. otherwise, stop and look for help


configure_grub

# 7. Ask for reboot (only if we are not already in trusted boot)

is_txtstat_installed() {
  is_command_available txt-stat
}

is_measured_launch() {
  local mle=$(txt-stat | grep 'TXT measured launch: TRUE')
  if [ -n "$mle" ]; then
    return 0
  else
    return 1
  fi
}

is_tpm_driver_loaded() {
  define_grub_file
  
  if [ ! -e /dev/tpm0 ]; then
    local is_tpm_tis_force=$(grep '^GRUB_CMDLINE_LINUX' /etc/default/grub | grep 'tpm_tis.force=1')
    local is_tpm_tis_force_any=$(grep '^GRUB_CMDLINE_LINUX' /etc/default/grub | grep 'tpm_tis.force')
    if [ -n "$is_tpm_tis_force" ]; then
      echo "TPM driver not loaded, tpm_tis.force=1 already in /etc/default/grub"
    elif [ -n "$is_tpm_tis_force_any" ]; then
      echo "TPM driver not loaded, tpm_tis.force present but disabled in /etc/default/grub"
    else
      #echo "TPM driver not loaded, adding tpm_tis.force=1 to /etc/default/grub"
      sed -i -e '/^GRUB_CMDLINE_LINUX/ s/"$/ tpm_tis.force=1"/' /etc/default/grub
      is_tpm_tis_force=$(grep '^GRUB_CMDLINE_LINUX' /etc/default/grub | grep 'tpm_tis.force=1')
      if [ -n "$is_tpm_tis_force" ]; then
        echo "TPM driver not loaded, added tpm_tis.force=1 to /etc/default/grub"
        grub2-mkconfig -o $GRUB_FILE
      else
        echo "TPM driver not loaded, failed to add tpm_tis.force=1 to /etc/default/grub"
      fi
    fi
    return 1
  fi
  return 0
}

install_rsync() {
  TRUSTAGENT_RSYNC_YUM_PACKAGES="rsync"
  TRUSTAGENT_RSYNC_APT_PACKAGES="rsync"
  TRUSTAGENT_RSYNC_YAST_PACKAGES="rsync"
  TRUSTAGENT_RSYNC_ZYPPER_PACKAGES="rsync"
  auto_install "rsync" "TRUSTAGENT_RSYNC" > /dev/null 2>&1
}

is_reboot_required() {
  local should_reboot=no
  
  if is_txtstat_installed; then
    if ! is_measured_launch; then
      echo "Not in measured launch environment, reboot required"
      should_reboot=yes
    else
      echo "Already in measured launch environment"
    fi
  fi
  
  if ! is_tpm_driver_loaded; then
    echo "TPM driver is not loaded, reboot required"
    should_reboot=yes
  else
    echo "TPM driver is already loaded"
  fi
  
  if [ "$should_reboot" == "yes" ]; then
    return 0
  else
    return 1
  fi
}

if is_reboot_required; then
    echo
    echo "CIT Trust Agent: A reboot is required. Please reboot and run installer again."
    echo
    exit 255
fi

# 8. Start tcsd (it already has an init script for next boot, but we need it now)

# tpm 1.2
is_tcsd_running() {
  local tcsd_pid=$(ps aux | grep tcsd | grep -v grep)
  if [ -n "$tcsd_pid" ]; then
    return 0
  else
    return 1
  fi
}

# tpm 1.2
start_tcsd() {
  local tcsd_cmd=$(which tcsd 2>/dev/null)
  if [ -n "$tcsd_cmd" ]; then
    echo "starting tcsd"
    tcsd
  fi
}

# tpm 2.0
is_tcsd2_running() {
  systemctl status tcsd2 >/dev/null 2>&1
}

start_tcsd2() {
  systemctl start tcsd2 >/dev/null 2>&1
}

if [ "$TPM_VERSION" == "1.2" ]; then
  if is_tcsd_running; then
    echo "tcsd already running"
  else
    start_tcsd
  fi
elif [ "$TPM_VERSION" == "2.0" ]; then
  if is_tcsd2_running; then
    echo "tcsd2 already running"
  else
    start_tcsd2
  fi
elif [ -z "$TPM_VERSION" ]; then
  echo "Cannot detect TPM version"
else
  echo "Unrecognized TPM version: $TPM_VERSION"
fi
