#!/bin/sh

# This script uses monitor.sh to show a progress bar while running install.sh
mkdir -p /tmp/cit/monitor/install-bkc-tool

# Combine the marker files for Attestation Service and Trust Agent
cat cit-service.mark cit-agent.mark > /tmp/cit/monitor/.markers

# Run the installer with console progress bar
echo "Installing BKC Tool for Cloud Integrity Technology (R)..."
chmod +x monitor.sh install.sh
./monitor.sh install.sh /tmp/cit/monitor/.markers /tmp/cit/monitor/install-bkc-tool

# after installation is complete, run the bkc tool (unless user/developer has asked for interactive mode)
if [ $? -eq 0 ]; then
  if [ -n "$CIT_BKC_INTERACTIVE" ]; then
    echo "Interactive mode"
    echo "Run 'cit-bkc-tool --help' to see available commands"
  else
    # the 'cit-bkc' tool should be in /usr/local/bin after successful install
    cit-bkc-tool clear
    cit-bkc-tool --reboot
  fi
  exit 0
else
  echo "Installation failed"
  echo "Log file: /tmp/cit/monitor/install-bkc-tool/stdout"
  exit 1
fi
