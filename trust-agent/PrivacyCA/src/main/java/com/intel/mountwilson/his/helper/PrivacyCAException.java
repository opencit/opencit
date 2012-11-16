package com.intel.mountwilson.his.helper;

import java.io.FileNotFoundException;



public class PrivacyCAException extends Exception {

	public PrivacyCAException(String message, Exception e) {
		super(message,e);
	}

	public PrivacyCAException(String message) {
		super(message);
	}

	public PrivacyCAException(Exception e) {
		super(e);
	}

}
