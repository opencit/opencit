package com.intel.mtwilson.audit.controller.exceptions;

import java.util.ArrayList;
import java.util.List;

public class AuditDataException extends Exception {
    private List<String> messages;
    public AuditDataException(List<String> messages) {
        super((messages != null && messages.size() > 0 ? messages.get(0) : null));
        if (messages == null) {
            this.messages = new ArrayList<String>();
        }
        else {
            this.messages = messages;
        }
    }
    public AuditDataException(Exception ex) {
		super(ex);
	}
	public List<String> getMessages() {
        return messages;
    }
}
