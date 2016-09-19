/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.codec.HexUtil;
import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.tpmmodules.Tpm;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */


public class GenerateQuoteCmd implements ICommand {
    Logger log = LoggerFactory.getLogger(getClass().getName());
    private Pattern PCR_LIST_SSV = Pattern.compile("^[0-9][0-9 ]*$");
    
    private TADataContext context;

    //using this variable to indicate if the AIK loaded to TPM. if not, load it; otherwise, skip loading
    private static boolean isAIKImported = false;
    
    public GenerateQuoteCmd(TADataContext context) {
        this.context = context;
    }
    
    protected static byte[] hexStringToByteArray(String s) {
        int len = s.length();
            
        
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String returnStr = "";
        for (int i = 0; i < b.length; i++) {
                String singleByte = Integer.toHexString(b[i] & 0xff);
                if (singleByte.length() != 2) singleByte = "0" + singleByte;
//			returnStr += singleByte;
                returnStr = sb.append(singleByte).toString();
        }
        return returnStr;
    }
    
    @Override
    public void execute() throws TAException {
        String identityAuthKey = context.getIdentityAuthKey();
        String selectedPcrs = context.getSelectedPCRs();
        
        if (!HexUtil.isHex(identityAuthKey)) {
            log.error("Aik secret password is not in hex format: {}", identityAuthKey);
            throw new IllegalArgumentException(String.format("Aik secret password is not in hex format."));
        }
        if (!PCR_LIST_SSV.matcher(selectedPcrs).matches()) {
            log.error("Selected PCRs do not match correct format: {}", selectedPcrs);
            throw new IllegalArgumentException(String.format("Selected PCRs do not match correct format."));
        }
                
        String osName = context.getOsName();
        String commandLine;
        String keyName = "HIS_Identity_Key";
        byte[] nonce = Base64.decodeBase64(context.getNonce());

        if (osName.toLowerCase().contains("windows")) {
            
            if (!isAIKImported) {
                // In the case of Windows, we import the AIK first
                String aikOpaqueFile = Folders.configuration() + File.separator + "aik.opaque";
                log.debug("AikOpaqueFile: " + aikOpaqueFile);

                String aikImportCmdLine = String.format("tpmtool.exe importaik \"%s\" HIS_Identity_Key", aikOpaqueFile);
                log.debug("cmd to run: ", aikImportCmdLine);
                try {
                    CommandUtil.runCommand(aikImportCmdLine);
                    isAIKImported = true;
                }catch (Exception e) {
                    throw new TAException(ErrorCode.COMMAND_ERROR, "Error while importing AIK" ,e);
                }
            }
            
            // format is: "tpmtool.exe [aik name] {attestation file} {nonce} {aikauth}" 
            commandLine = String.format("tpmtool.exe aikquote %s %s %s", // skip the authkey for now
                byteArrayToHexString(keyName.getBytes()),
                byteArrayToHexString(context.getQuoteFileName().getBytes()),
                byteArrayToHexString(nonce));
            /*
            commandLine = String.format("tpmtool.exe %s %s %s %s",
                byteArrayToHexString(keyName.getBytes()),
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getQuoteFileName()),
                byteArrayToHexString(nonce),
                identityAuthKey);
            */

            //debug
            log.debug("Command Line to be executed: " + commandLine);
            try {
                CommandUtil.runCommand(commandLine);
                log.debug("Create the quote {} ",
                context.getQuoteFileName());
                context.setTpmQuote(CommandUtil.readfile(context.getQuoteFileName()));
            }catch (Exception e) {
                            throw new TAException(ErrorCode.COMMAND_ERROR, "Error while generating quote" ,e);
            }
        } else {
            String tpmVersion = Tpm.getTpmVersion();
            log.debug("TPM version before calling: {} ", tpmVersion);
            if (tpmVersion.equals("2.0")) {
                try {
                    String selectedPcrList = selectedPcrs.replaceAll("\\s+", ","); //change the format to use ',' to seperate the list
                    String quoteAlgWithPcrs = "";
                    if (context.getSelectedPcrBanks() == null) {
                        quoteAlgWithPcrs = "0x0B:" + selectedPcrList;
                    } else {
                        String[] pcrBanks = context.getSelectedPcrBanks().split("\\s+");
                        //construct the list of algorithms and pcrs for quote
                        for (int i=0; i<pcrBanks.length; i++) {
                            if (i != 0)
                                quoteAlgWithPcrs += "+";                            
                            switch (pcrBanks[i]) {
                                case "SHA1":
                                    quoteAlgWithPcrs += "0x04" + ":" + selectedPcrList;
                                    break;
                                case "SHA256":
                                    quoteAlgWithPcrs += "0x0B" + ":" + selectedPcrList;
                                    break;
                                case "SHA384":
                                    quoteAlgWithPcrs += "0x0C" + ":" + selectedPcrList;
                                    break;
                                case "SHA512":
                                    quoteAlgWithPcrs += "0x0D" + ":" + selectedPcrList;
                                    break;
                                case "SM3_256":
                                    quoteAlgWithPcrs += "0x12" + ":" + selectedPcrList;
                                    break;
                                default:
                                    log.error("Unsupported pcrbank value: {}", pcrBanks[i]);
                                    if (i!=0)  //remove the "+" added at the beginning of the loop
                                        quoteAlgWithPcrs = quoteAlgWithPcrs.substring(0, quoteAlgWithPcrs.length()-1);
                                    break;
                            }
                        }
                    }

                    /* 1st: get pcrs - tpm2_listpcrs -g 0x4 -o pcrs.out
                     *      This commmand returns specified PCR bank pcr values (all 24 pcrs in the bank)
                    */
                    CommandLine command1 = new CommandLine("tpm2_listpcrs");
                    command1.addArgument("-L");
                    command1.addArgument(quoteAlgWithPcrs);
                    command1.addArgument("-o");
                    command1.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getPcrsFileName()));
                    Result result1 = ExecUtil.execute(command1);
                    if (result1.getExitCode() != 0) {
                        log.error("Error running command [{}]: {}", command1.getExecutable(), result1.getStderr());
                        throw new TAException(ErrorCode.ERROR, result1.getStderr());
                    }
                    log.debug("tpm2_listpcrs stdout: {}", result1.getStdout());
    
                    /* 2nd: get quote - tpm2_quote -k 0x80000001 -P abc123 -g 0x4 -l 16,17,18 -o outFile001 -X
                     * this command returns two structures together
                        * the quoted information
                        * signature over quoted information
                    */
                    TrustagentConfiguration TAconfig = TrustagentConfiguration.loadConfiguration();
                    CommandLine command = new CommandLine("tpm2_quote");
                    command.addArgument("-k");
                    command.addArgument(TAconfig.getAikHandle());
                    command.addArgument("-P");
	            command.addArgument(identityAuthKey);
                    command.addArgument("-L");
                    command.addArgument(quoteAlgWithPcrs);
                    command.addArgument("-q");
                    command.addArgument(TpmUtils.byteArrayToHexString(nonce));
                    //command.addArgument("-l");
                    //command.addArgument(selectedPcrs.replaceAll("\\s+", ","));
                    command.addArguments("-o");
                    command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getQuoteFileName()));
                    command.addArguments("-X");   
                    //command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getNonceFileName()));
                    //command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getAikBlobFileName()));
                    Result result = ExecUtil.execute(command);
                    if (result.getExitCode() != 0) {
	                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
	                throw new TAException(ErrorCode.ERROR, result.getStderr());
	            }
	            log.debug("tpm2_quote stdout: {}", result.getStdout());
	            log.debug("Create the quote {} ", context.getQuoteFileName());
                    
                    // 3rd: concatate the two output together and set the tpm quote return
	            byte [] pcrs;
                    byte [] quoteResult;
                    try (InputStream in = new FileResource(new File(context.getPcrsFileName())).getInputStream()) {
	                pcrs = IOUtils.toByteArray(in);
	            }
                    try (InputStream in = new FileResource(new File(context.getQuoteFileName())).getInputStream()) {
	                quoteResult = IOUtils.toByteArray(in);
	            }
                    //log.debug("pcrs: {}", pcrs.toString());
                    //log.debug("quote result: {}", quoteResult.toString());
                    
                    byte[] combined = new byte[pcrs.length + quoteResult.length];
                    System.arraycopy(quoteResult, 0, combined, 0, quoteResult.length);
                    System.arraycopy(pcrs, 0, combined, quoteResult.length, pcrs.length);
                    context.setTpmQuote(combined);
                    
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(GenerateQuoteCmd.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {   
                        CommandLine command = new CommandLine("/opt/trustagent/bin/aikquote");
                        command.addArgument("-p");
                        command.addArgument(identityAuthKey);
                        command.addArgument("-c");
                        command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getNonceFileName()));
                        command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getAikBlobFileName()));
                        command.addArguments(selectedPcrs.split("\\s+"));
                        command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getQuoteFileName()));
                        Result result = ExecUtil.execute(command);
                        if (result.getExitCode() != 0) {
                            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                            throw new TAException(ErrorCode.ERROR, result.getStderr());
                        }
                        log.debug("command stdout: {}", result.getStdout());
                        log.debug("Create the quote {} ", context.getQuoteFileName());
                        try (InputStream in = new FileResource(new File(context.getQuoteFileName())).getInputStream()) {
                            context.setTpmQuote(IOUtils.toByteArray(in));
                        }
                } catch (Exception e) {
                    throw new TAException(ErrorCode.COMMAND_ERROR, "Error while generating quote", e);
                }
            }
        }
    }   
}
