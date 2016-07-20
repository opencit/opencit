#!/bin/bash
buildSpecsDirectory="/home/klocwork/kw_build_specs"
tablesDirectory="/home/klocwork/kwtables"
klocworkProject="dcg_security-mtwilson"
klocworkServerUrl="https://klocwork-jf18.devtools.intel.com:8160"

generateBuildSpecs() {
  kwmaven --output "${buildSpecsDirectory}/mtwilson.out" -DskipTests=true install
  (cd installers/AttestationServiceLinuxInstaller/src/files/aikqverify && make clean)
  (cd installers/AttestationServiceLinuxInstaller/src/files/aikqverify && kwinject --output "${buildSpecsDirectory}/aikqverify.out" make)
  (cd services/aikqverify/src/main/resources && make clean)
  (cd services/aikqverify/src/main/resources && kwinject --output "${buildSpecsDirectory}/aikqverify_services.out" make)
  (cd installers/mtwilson-trustagent-rhel/src/files/hex2bin && make clean)
  (cd installers/mtwilson-trustagent-rhel/src/files/hex2bin && kwinject --output "${buildSpecsDirectory}/hex2bin_rhel.out" make)
  (cd installers/mtwilson-trustagent-rhel/src/files/commands && make clean)
  (cd installers/mtwilson-trustagent-rhel/src/files/commands && kwinject --output "${buildSpecsDirectory}/aikquote_rhel.out" make)
  (cd installers/mtwilson-trustagent-ubuntu/src/files/hex2bin && make clean)
  (cd installers/mtwilson-trustagent-ubuntu/src/files/hex2bin && kwinject --output "${buildSpecsDirectory}/hex2bin_ubuntu.out" make)
  (cd installers/mtwilson-trustagent-ubuntu/src/files/commands && make clean)
  (cd installers/mtwilson-trustagent-ubuntu/src/files/commands && kwinject --output "${buildSpecsDirectory}/aikquote_ubuntu.out" make)
  
  #NIARL
  (cd trust-agent/TPMModule/plain/linux && make clean)
  (cd trust-agent/TPMModule/plain/linux && kwinject --output "${buildSpecsDirectory}/niarl_plain.out" make)
  (cd trust-agent/TPMModule/sha1/linux && make clean)
  (cd trust-agent/TPMModule/sha1/linux && kwinject --output "${buildSpecsDirectory}/niarl_sha1.out" make)
  
  if [ ! -d "trust-agent/mtwilson-tpm-commands/target" ]; then
    (cd trust-agent/mtwilson-tpm-commands && mvn install)
  fi
  (cd trust-agent/mtwilson-tpm-commands/target/tpm-tools-1.3.8 && make clean)
  (cd trust-agent/mtwilson-tpm-commands/target/tpm-tools-1.3.8 && ./configure LDFLAGS="-L/usr/local/lib" --prefix=/usr/local --with-openssl=/usr/local/ssl && kwinject --output "${buildSpecsDirectory}/tpm_tools.out" make)
  (cd trust-agent/mtwilson-tpm-commands/target/hex2bin-master && make clean)
  (cd trust-agent/mtwilson-tpm-commands/target/hex2bin-master && kwinject --output "${buildSpecsDirectory}/hex2bin.out" make)
  (cd trust-agent/mtwilson-tpm-commands/target && make clean)
  (cd trust-agent/mtwilson-tpm-commands/target && kwinject --output "${buildSpecsDirectory}/tpm_cit_commands.out" make)
}

buildProject() {
  kwbuildproject --url "${klocworkServerUrl}/${klocworkProject}" --tables-directory "${tablesDirectory}" --force "${buildSpecsDirectory}/mtwilson.out" "${buildSpecsDirectory}/aikqverify.out" "${buildSpecsDirectory}/aikqverify_services.out" "${buildSpecsDirectory}/hex2bin_rhel.out" "${buildSpecsDirectory}/aikquote_rhel.out" "${buildSpecsDirectory}/hex2bin_ubuntu.out" "${buildSpecsDirectory}/aikquote_ubuntu.out" "${buildSpecsDirectory}/niarl_plain.out" "${buildSpecsDirectory}/niarl_sha1.out" "${buildSpecsDirectory}/tpm_tools.out" "${buildSpecsDirectory}/hex2bin.out" "${buildSpecsDirectory}/tpm_createkey.out" "${buildSpecsDirectory}/tpm_cit_commands.out"
}

uploadResults() {
  kwadmin --url "${klocworkServerUrl}" load "${klocworkProject}" "${tablesDirectory}"
}

generateBuildSpecs
buildProject
uploadResults