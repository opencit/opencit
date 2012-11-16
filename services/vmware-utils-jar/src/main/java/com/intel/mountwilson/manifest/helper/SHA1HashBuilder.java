package com.intel.mountwilson.manifest.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.ErrorCode;

public class SHA1HashBuilder {
	private byte[] _data = null;

	public SHA1HashBuilder() {
		initialize();
	}

	public void append(byte[] dataToAppend) {
//		checkIsInitialized();

		if (dataToAppend != null) {

			byte[] combined = new byte[getLength() + dataToAppend.length];

			System.arraycopy(_data, 0, combined, 0, getLength());
			System.arraycopy(dataToAppend, 0, combined, getLength(),
					dataToAppend.length);

			_data = sha1(combined);
		}
	}

	private int getLength() {
		// TODO Auto-generated method stub
		return _data.length;
	}

	private  byte[] sha1(final byte[] input)
	{
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(input);
			byte[] digest = md.digest();
			return digest;
		} catch (NoSuchAlgorithmException e) {
			throw new ASException(e);
		}
	}
	private void initialize() {

		if (_data == null) {
			_data = new byte[20];
			for(int i = 0; i< 20 ; i++ )
				_data[i] =0;
		}

	}

	public byte[] get_data() {
		return _data;
	}

}
