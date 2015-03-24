package com.intel.mtwilson.trustagent.vrtmclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TCBuffer {
	public static final int SIZE = 20;
	private static final String EMPTY = "EMPTY"; 
	private ByteBuffer brpId;
	private ByteBuffer brpcCallIndex;
	private ByteBuffer brpcPayloadSize;
	private ByteBuffer brpcCallStatus;
	private ByteBuffer boriginalRpId;
	private byte[]     rpcPayload;
	
	public TCBuffer(){
		brpId 		  = ByteBuffer.allocate(4);
		brpcCallIndex = ByteBuffer.allocate(4);
		brpcPayloadSize  = ByteBuffer.allocate(4);
		brpcCallStatus   = ByteBuffer.allocate(4);
		boriginalRpId = ByteBuffer.allocate(4);
		rpcPayload    =  new String("EMPTY").getBytes();
		// set buffer to little endian format
		brpId.order(ByteOrder.LITTLE_ENDIAN);
		brpcCallIndex.order(ByteOrder.LITTLE_ENDIAN);
		brpcPayloadSize.order(ByteOrder.LITTLE_ENDIAN);
		brpcCallStatus.order(ByteOrder.LITTLE_ENDIAN);
		boriginalRpId.order(ByteOrder.LITTLE_ENDIAN);
	}

	public TCBuffer(int rpId, int rpcCallIndex, int rpcCallStatus, int originalRpId, String rpcPayload){
		this();
		setRpId(rpId);
		setRPCCallIndex(rpcCallIndex);
		setRPCPayloadSize(rpcPayload.length());
		setRPCCallStatus(rpcCallStatus);
		setOriginalRpId(originalRpId);
		setRPCPayload(rpcPayload.getBytes());
	}

	public TCBuffer(int rpId, int rpcCallIndex, int rpcCallStatus, int originalRpId){
		this(rpId, rpcCallIndex, rpcCallStatus, originalRpId, EMPTY);
	}
	
	public String getRPCPayload() {
		return new String(rpcPayload);
	}

	public void setRPCPayload(byte[] rpcPayload) {
		this.rpcPayload = rpcPayload;
		setRPCPayloadSize(this.rpcPayload.length);
	}

	public int getRpId() {
		if(null != brpId) {
			brpId.rewind();
			return brpId.getInt();
		}
		return -1;
	}
	public void setRpId(int rpId) {
		brpId.clear();
		brpId.putInt(rpId);
	}
	
	public int getRPCCallIndex() {
		if(null != brpcCallIndex){
			brpcCallIndex.rewind();
			return brpcCallIndex.getInt();
		}
		return -1;
	}
	public void setRPCCallIndex(int rpcCallIndex) {
		brpcCallIndex.clear();
		brpcCallIndex.putInt(rpcCallIndex);
	}
	
	public int getRPCCallStatus() {
		if( null != brpcCallStatus) {
			brpcCallStatus.rewind();
			return brpcCallStatus.getInt();
		}
		return -1;
	}
	public void setRPCCallStatus(int callStatus) {
		brpcCallStatus.clear();
		brpcCallStatus.putInt(callStatus);
	}
	
	public int getOriginalRpId() {
		if(null != boriginalRpId){
			boriginalRpId.rewind();
			return boriginalRpId.getInt();
		}
		return -1;
	}
	public void setOriginalRpId(int originalRpId) {
		boriginalRpId.clear();
		boriginalRpId.putInt(originalRpId);
	}
	
	
	public void setRPCPayloadSize(int payloadSize){
		brpcPayloadSize.clear();
		brpcPayloadSize.putInt(payloadSize);
	}
	public int getRPCPayloadSize(){
		if(null != brpcPayloadSize){
			brpcPayloadSize.rewind();
			return brpcPayloadSize.getInt();
		}
		return -1;
	}
	
	
	public void serializeTCBuffer(OutputStream out) throws IOException{
		out.write(brpId.array());
		out.write(brpcCallIndex.array());
		out.write(brpcPayloadSize.array());
		out.write(brpcCallStatus.array());
		out.write(boriginalRpId.array());
		out.write(rpcPayload);
		out.flush();
	}
	private void setInternalValues(ByteBuffer buffer, byte[] b) {
		buffer.clear();
		buffer.put(b);
	}
	/**
	 * This function only set first 20 byte of TCBuffer
	 * @param tcBufferByteStream
	 * @throws IOException
	 */
	public void deSerializeTCBuffer(InputStream in) throws IOException{
		byte[] bigBytes = new byte[4];
		in.read(bigBytes);
		setInternalValues(brpId, bigBytes);
		in.read(bigBytes);
		setInternalValues(brpcCallIndex, bigBytes);
		in.read(bigBytes);
		setInternalValues(brpcPayloadSize, bigBytes);
		in.read(bigBytes);
		setInternalValues(brpcCallStatus, bigBytes);
		in.read(bigBytes);
		setInternalValues(boriginalRpId, bigBytes);
		rpcPayload = new byte[getRPCPayloadSize()];
		in.read(rpcPayload);
	}
}
