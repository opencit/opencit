#!/bin/bash

# Preconditions:
# 1. CIT_BKC_PACKAGE_PATH is defined (for example /usr/local/share/cit-bkc-tool)
#    and contains all the cit-bkc-tool ancillary files

# This script uses monitor.sh to show a progress bar while running install.sh

is_active() {
  local status=$(./monitor.sh --status $1)
  result=$?
  if [ $result -eq 0 ] && [ "$status" == "ACTIVE" ]; then
    return 0
  fi
  return 1
}

is_done() {
  local status=$(./monitor.sh --status $1)
  result=$?
  if [ $result -eq 0 ] && [ "$status" == "DONE" ]; then
    return 0
  fi
  return 1
}

# Run the installer with console progress bar, using a combined marker file
# for both Attestation Service and Trust Agent
install_bkc_tool() {
  echo "Installing BKC Tool for Cloud Integrity Technology (R)..."
  rm -rf /tmp/cit/monitor/install-bkc-tool
  mkdir -p /tmp/cit/monitor/install-bkc-tool
  cat cit-service.mark cit-agent.mark > /tmp/cit/monitor/install-bkc-tool/.markers
  ./monitor.sh ./install.sh /tmp/cit/monitor/install-bkc-tool/.markers /tmp/cit/monitor/install-bkc-tool
}

run_bkc_tool() {
  echo "Running BKC Tool for Cloud Integrity Technology (R)..."
  rm -rf /tmp/cit/monitor/run-bkc-tool
  mkdir -p /tmp/cit/monitor/run-bkc-tool
  cp cit-bkc-tool.mark /tmp/cit/monitor/run-bkc-tool/.markers
  ./monitor.sh cit-bkc-tool /tmp/cit/monitor/run-bkc-tool/.markers /tmp/cit/monitor/run-bkc-tool
}

# Check if installation completed successfully
if is_done /tmp/cit/monitor/install-bkc-tool; then
  echo "BKC Tool is already installed"
elif is_active /tmp/cit/monitor/install-bkc-tool; then
  # monitor the other process
  ./monitor.sh --noexec /tmp/cit/monitor/install-bkc-tool
else
  install_bkc_tool
  result=$?
  if [ $result -ne 0 ]; then
    echo "Installation failed"
    echo "Log file: /tmp/cit/monitor/install-bkc-tool/stdout"
    exit $result
  fi
fi


#  if [ -n "$CIT_BKC_INTERACTIVE" ]; then
#    echo "Interactive mode"
#    echo "Run 'cit-bkc-tool --help' to see available commands"
#    exit 0
#  fi

# After installation is complete, run the bkc tool
# (unless user/developer has asked for interactive mode)
# CIT_BKC_INTERACTIVE and CIT_BKC_REBOOT
# The cit-bkc-tool should be in /usr/local/bin after installation
run_bkc_tool
result=$?
if [ $result -eq 0 ]; then
  cit-bkc-tool report
  exit 0
else
  echo "Unable to complete CIT BKC testing"
  exit 1
fi
