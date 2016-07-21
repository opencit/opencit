#!/bin/bash
buildSpecsDirectory="/home/klocwork/kw_build_specs"
tablesDirectory="/home/klocwork/kwtables"
klocworkProject="dcg_security-mtwilson"
klocworkServerUrl="https://klocwork-jf18.devtools.intel.com:8160"

initialize() {
  mkdir -p "${buildSpecsDirectory}"
  mkdir -p "${tablesDirectory}"
}

generateBuildSpecs() {
  ant ready clean
  kwmaven --output "${buildSpecsDirectory}/mtwilson.out" -DskipTests=true install
  (cd installers/AttestationServiceLinuxInstaller/src/files/aikqverify && make clean)
  (cd installers/AttestationServiceLinuxInstaller/src/files/aikqverify && kwinject --output "${buildSpecsDirectory}/aikqverify.out" make)
  (cd services/aikqverify/src/main/resources && make clean)
  (cd services/aikqverify/src/main/resources && kwinject --output "${buildSpecsDirectory}/aikqverify_services.out" make)
  (cd installers/mtwilson-trustagent/src/files/hex2bin && make clean)
  (cd installers/mtwilson-trustagent/src/files/hex2bin && kwinject --output "${buildSpecsDirectory}/hex2bin_trustagent.out" make)
  (cd installers/mtwilson-trustagent/src/files/commands && make clean)
  (cd installers/mtwilson-trustagent/src/files/commands && kwinject --output "${buildSpecsDirectory}/aikquote_trustagent.out" make)
  
  #NIARL
  (cd trust-agent/TPMModule/plain/linux && make clean)
  (cd trust-agent/TPMModule/plain/linux && kwinject --output "${buildSpecsDirectory}/niarl_plain.out" make)
  (cd trust-agent/TPMModule/sha1/linux && make clean)
  (cd trust-agent/TPMModule/sha1/linux && kwinject --output "${buildSpecsDirectory}/niarl_sha1.out" make)
  
  if [ ! -d "trust-agent/mtwilson-tpm-commands/target" ]; then
    (cd trust-agent/mtwilson-tpm-commands && mvn install)
  fi
  (cd trust-agent/mtwilson-tpm-commands/target/hex2bin-master && make clean)
  (cd trust-agent/mtwilson-tpm-commands/target/hex2bin-master && kwinject --output "${buildSpecsDirectory}/hex2bin.out" make)
  (cd trust-agent/mtwilson-tpm-commands/target && make clean)
  (cd trust-agent/mtwilson-tpm-commands/target && kwinject --output "${buildSpecsDirectory}/tpm_cit_commands.out" make)
}

buildProject() {
  kwbuildproject --url "${klocworkServerUrl}/${klocworkProject}" --tables-directory "${tablesDirectory}" --force "${buildSpecsDirectory}/mtwilson.out" "${buildSpecsDirectory}/aikqverify.out" "${buildSpecsDirectory}/aikqverify_services.out" "${buildSpecsDirectory}/hex2bin_trustagent.out" "${buildSpecsDirectory}/aikquote_trustagent.out" "${buildSpecsDirectory}/niarl_plain.out" "${buildSpecsDirectory}/niarl_sha1.out" "${buildSpecsDirectory}/hex2bin.out" "${buildSpecsDirectory}/tpm_createkey.out" "${buildSpecsDirectory}/tpm_cit_commands.out"
}

uploadResults() {
  kwadmin --url "${klocworkServerUrl}" load "${klocworkProject}" "${tablesDirectory}"
}

initialize
generateBuildSpecs
buildProject
uploadResults