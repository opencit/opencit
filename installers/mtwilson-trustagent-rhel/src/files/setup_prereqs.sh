#!/bin/bash

# Preconditions:
# * http_proxy and https_proxy are already set, if required
# * date and time are synchronized with remote server, if using remote attestation service
# * the mtwilson linux util functions already sourced
#   (for add_package_repository, echo_success, echo_failure)
# * TRUSTAGENT_HOME is set, for example /opt/trustagent
# * TRUSTAGENT_INSTALL_LOG_FILE is set, for example /opt/trustagent/logs/install.log

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
# 7. Reboot (only if we are not already in trusted boot)
# 8. Start tcsd (it already has an init script for next boot, but we need it now)
# 9. Run mtwilson-trustagent-rhel.bin

# source functions file
if [ -f functions ]; then . functions; fi

TRUSTAGENT_HOME=${TRUSTAGENT_HOME:-/opt/trustagent}
LOGFILE=${TRUSTAGENT_INSTALL_LOG_FILE:-$TRUSTAGENT_HOME/logs/install.log}
mkdir -p $(dirname $LOGFILE)

TRUSTAGENT_RESUME_FLAG=no
if [ "$1" == "--resume" ]; then
  TRUSTAGENT_RESUME_FLAG=yes
fi

################################################################################

# 1. Add epel-release-latest-7.noarch repository

add_package_repository https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm 

# 2. Install redhat-lsb-core and other redhat-specific packages

if yum_detect; then
  yum -y install redhat-lsb redhat-lsb-core libvirt net-tools grub2-efi-modules > /dev/null 2>&1
fi

# 3. Install tboot
# 4. Install trousers and trousers-devel packages (current is trousers-0.3.13-1.el7.x86_64)
# 5. Install the patched tpm-tools

is_tboot_installed() {
  is_package_installed tboot
}

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

install_trousers() {
  TRUSTAGENT_TROUSERS_YUM_PACKAGES="trousers trousers-devel"
  TRUSTAGENT_TROUSERS_APT_PACKAGES="trousers trousers-dbg libtspi-dev libtspi1"
  TRUSTAGENT_TROUSERS_YAST_PACKAGES="trousers trousers-devel"
  TRUSTAGENT_TROUSERS_ZYPPER_PACKAGES="trousers trousers-devel"
  auto_install "trousers" "TRUSTAGENT_TROUSERS" > /dev/null 2>&1
}

install_tpm_tools() {
  TRUSTAGENT_TPMTOOLS_YUM_PACKAGES="tpm-tools"
  TRUSTAGENT_TPMTOOLS_APT_PACKAGES="tpm-tools"
  TRUSTAGENT_TPMTOOLS_YAST_PACKAGES="tpm-tools"
  TRUSTAGENT_TPMTOOLS_ZYPPER_PACKAGES="tpm-tools"
  auto_install "tpm-tools" "TRUSTAGENT_TPMTOOLS" > /dev/null 2>&1
}

install_patched_tpm_tools() {
  local PATCHED_TPMTOOLS_BIN=`ls -1 patched-*.bin | head -n 1`
  if [ -n "$PATCHED_TPMTOOLS_BIN" ]; then
    chmod +x $PATCHED_TPMTOOLS_BIN
    ./$PATCHED_TPMTOOLS_BIN
  fi
}

if is_tboot_installed; then
    echo "tboot already installed"
else
    install_tboot
fi

install_openssl
install_trousers
install_tpm_tools
install_patched_tpm_tools

# 6. Add grub menu item for tboot and select as default

is_uefi_boot() {
  if [ -d /sys/firmware/efi ]; then
    return 0
  else
    return 1
  fi
}

configure_grub() {
  if is_uefi_boot; then
    DEFAULT_GRUB_FILE=/boot/efi/EFI/redhat/grub.cfg
  else
    DEFAULT_GRUB_FILE=/boot/grub2/grub.cfg  
  fi

  GRUB_FILE=${GRUB_FILE:-$DEFAULT_GRUB_FILE}

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

configure_grub

# 7. Reboot (only if we are not already in trusted boot)

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

migrate_to_local() {
  local script_path
  if [ "$0" == "-bash" ]; then
    # sourced from shell, so path is current directory
    script_path=$(pwd)
  else
    # sourced from setup.sh or executed from shell
    script_path=$(dirname $(realpath $0))
  fi
  # so if we are not already running from trustagent home, copy everything to it
  if [ "$script_path" != "$TRUSTAGENT_HOME/installer" ]; then
    rm -rf $TRUSTAGENT_HOME/installer
    mkdir -p $TRUSTAGENT_HOME/installer
    \cp -r $script_path/* $TRUSTAGENT_HOME/installer/
  fi
}

# precondition:
#   the script and all adjacent files are already copied to trustagent home from where it can run after reboot
#   the variable TRUSTAGENT_REBOOT is set to 'no' if user wants to disable automatic
resume_after_reboot() {
    touch /tmp/trustagent.crontab && chmod 600 /tmp/trustagent.crontab
    echo "@reboot $TRUSTAGENT_HOME/installer/setup_prereqs.sh --resume >> $LOGFILE 2>&1" > /tmp/trustagent.crontab
    # remove any existing lines with setup_prereqs and then append new lines with setup_prereqs we just prepared
    crontab -u root -l 2>/dev/null | grep -v setup_prereqs | cat - /tmp/trustagent.crontab | crontab -u root - 2>/dev/null
}

no_resume_after_reboot() {
    # remove any existing lines with setup_prereqs
    crontab -u root -l 2>/dev/null | grep -v setup_prereqs | crontab -u root - 2>/dev/null
}


is_reboot_required() {
  local should_reboot=yes
  if is_txtstat_installed; then
    if is_measured_launch; then
      echo "already in measured launch environment"
      if is_tpm_driver_loaded; then
        echo "TPM driver is already loaded"
        should_reboot=no
      fi
    fi
  fi
  if [ "$should_reboot" == "yes" ] && [ "$TRUSTAGENT_RESUME_FLAG" == "yes" ]; then
    echo "already rebooted once, disabling automatic reboot"
    should_reboot=no
  fi
  if [ "$should_reboot" == "yes" ]; then
    if [ "${TRUSTAGENT_REBOOT:-no}" == "no" ]; then
      return 255
    else
      return 0
    fi
  else
    return 1
  fi
}

reboot_maybe() {
  is_reboot_required
  local result=$?
  if [ $result -eq 0 ]; then
    # reboot is required
      migrate_to_local
      resume_after_reboot
      echo "Rebooting in 60 seconds... kill $$ to cancel";
      sleep 10
      shutdown --reboot now
      exit 0
  elif [ $result -eq 255 ]; then
      # reboot is required but will not be automatic
      echo "reboot is required; please reboot and run installer again"
  # else reboot is not required
  fi
}

reboot_maybe

# if we got here, then we're not rebooting, so make sure that we clear any
# crontab entry we may have already created
no_resume_after_reboot


# 8. Start tcsd (it already has an init script for next boot, but we need it now)

is_tcsd_running() {
  local tcsd_pid=$(ps aux | grep tcsd | grep -v grep)
  if [ -n "$tcsd_pid" ]; then
    return 0
  else
    return 1
  fi
}

start_tcsd() {
  local tcsd_cmd=$(which tcsd)
  if [ -n "$tcsd_cmd" ]; then
    echo "starting tcsd"
    tcsd
  fi
}

if is_tcsd_running; then
  echo "tcsd already running"
else
  start_tcsd
fi

# 9. Run mtwilson-trustagent-rhel.bin

next_step() {
  local script_path
  if [ "$0" == "-bash" ]; then
    # sourced from shell, so path is current directory
    script_path=$(pwd)
  else
    # sourced from setup.sh or executed from shell
    script_path=$(dirname $(realpath $0))
  fi
  if [ "$TRUSTAGENT_RESUME_FLAG" == "yes" ]; then
    echo "continuing CIT Agent installation after reboot"
    (cd $script_path && export TRUSTAGENT_SETUP_PREREQS=no && ./setup.sh)
  #else
    # do nothing; either setup.sh called us and will continue when we exit,
    # or user called us directly from shell and expects us to exit when done.
  fi
}

next_step
