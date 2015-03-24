package com.intel.mtwilson.trustagent.vrtmclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RPClient {
	private InetSocketAddress rpcoreEndpoint;
	private Socket rpSock;

	public RPClient(String hostName, int port){
		this.rpcoreEndpoint = new InetSocketAddress(hostName, port);
		this.rpSock = new Socket();
	}
	
	public TCBuffer send(TCBuffer outTCBuffer) throws IOException {
		if(!rpSock.isConnected()){
			rpSock.connect(rpcoreEndpoint);
		}
		String rpId = String.valueOf(outTCBuffer.getRpId());
		ByteArrayOutputStream rpOutStream = new ByteArrayOutputStream();
		byte[] arr = rpId.getBytes();
		rpOutStream.write(arr);
		rpOutStream.write('\0');

		OutputStream rpcoreOut = rpSock.getOutputStream();
		rpOutStream.writeTo(rpcoreOut);
		rpOutStream.flush();
		rpcoreOut.flush();
		
		outTCBuffer.serializeTCBuffer(rpcoreOut);
		
		InputStream rpcoreIn = rpSock.getInputStream();
		TCBuffer inTCBuffer = new TCBuffer();
		inTCBuffer.deSerializeTCBuffer(rpcoreIn);
		
		return inTCBuffer;
	}
	
	public void close(){
		try{
			rpSock.shutdownOutput();
			rpSock.shutdownInput();
			rpSock.close();
		}catch(Exception e){
			// we don't need to care about it 
		}
	}
	
}
