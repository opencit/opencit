-- created 2013-04-30

-- This script adds another entry into the event type table that would be used for Open Source module attestation. 
-- Unlike VMware, open source hypervisors do not support events. So, we are using a generic name.

INSERT INTO mw_event_type (ID, Name, FieldName) VALUES (5,'OpenSource.EventName','OpenSource');

INSERT INTO mw_changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20130430154900,NOW(),'patch for adding a new entry into the event type table for open source module attestation.');
