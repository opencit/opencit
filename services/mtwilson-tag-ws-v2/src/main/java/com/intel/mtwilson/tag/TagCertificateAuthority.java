/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.*;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.model.x509.UTF8NameValueMicroformat;
import com.intel.mtwilson.tag.selection.SelectionBuilder;
import com.intel.mtwilson.tag.selection.xml.*;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Creates tag certificates for existing certificate requests in accordance with
 * the configured policy.
 *
 * When used in the provisioning service, this class is the "internal CA" for
 * automatic approval of certificate requests.
 *
 * Input: TagCertificateRequest Output: TagCertificate
 *
 * The only required parameter in the certificate request is the identity of the
 * subject (hardware uuid) being certified.
 *
 * TODO INSECURE: add shiro annotations to require the logged in user to have
 * asset tag certificate permission; for pull provisioning this permission must
 * be granted to the user whose credentials are used from the host that requests
 * the certificate
 *
 * @author jbuhacoff
 */
public class TagCertificateAuthority {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateAuthority.class);
    private TagConfiguration configuration;

    public TagCertificateAuthority(TagConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns a new SelectionsType instance with only the selections that are
     * valid today.
     */
    protected SelectionsType getCurrentSelections(SelectionsType selections) {
        GregorianCalendar today = new GregorianCalendar();
        today.setTime(new Date()); // http://docs.oracle.com/javase/7/docs/api/java/util/GregorianCalendar.html
        SelectionsType valid = new SelectionsType();
        for (SelectionType selection : selections.getSelection()) {
            // skip if the selection is not currently valid (notBefore<today<notAfter) ; if notBefore or notAfter are not defined, then validity is assumed
            if (selection.getNotBefore() != null && today.before(selection.getNotBefore().toGregorianCalendar())) {
                log.debug("skipping selection because of notBefore date");
                continue;
            }
            if (selection.getNotAfter() != null && today.after(selection.getNotAfter().toGregorianCalendar())) {
                log.debug("skipping selection because of notAfter date");
                continue;
            }
            valid.getSelection().add(selection);
        }
        // repeat the same action for the default selections
        if( selections.getDefault() != null ) {
            valid.setDefault(new DefaultType());
            for (SelectionType selection : selections.getDefault().getSelection()) {
                // skip if the selection is not currently valid (notBefore<today<notAfter) ; if notBefore or notAfter are not defined, then validity is assumed
                if (selection.getNotBefore() != null && today.before(selection.getNotBefore().toGregorianCalendar())) {
                    log.debug("skipping selection because of notBefore date");
                    continue;
                }
                if (selection.getNotAfter() != null && today.after(selection.getNotAfter().toGregorianCalendar())) {
                    log.debug("skipping selection because of notAfter date");
                    continue;
                }
                valid.getDefault().getSelection().add(selection);
            }
        }
        return valid;
    }

    /**
     * Looks up hardware uuid of host in mtwilson; host already be registered.
     *
     * @param ip address or hostname
     * @return
     */
    public String findSubjectHardwareUuid(String ip) throws Exception {
        List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(ip, true);
        if (hostList == null || hostList.size() < 1) {
            log.debug("host uuid didn't return back any results");
            //throw new ASException(new Exception("No host records found, please verify your host is in mtwilson or provide a hardware uuid in the subject field.")); // TODO: i18n  similar to  ErrorCode.AS_HOST_NOT_FOUND but with this custom message
            log.warn("No host records found, please verify your host is in mtwilson or provide a hardware uuid in the subject field.");
            return null;
        }
        log.debug("get host uuid returned " + hostList.get(0).Hardware_Uuid);
        return hostList.get(0).Hardware_Uuid;

    }

    /**
     * Looks up kv attributes by selection name and prepares a SelectionType
     * object corresponding to <selection name="name"/> in the selection xml
     * with all the attributes populated.
     *
     * NOTE: currently this only populates kv attributes.
     *
     */
    protected SelectionType findSelectionByName(String name) throws SQLException {
        try (SelectionDAO selectionDao = TagJdbi.selectionDao()) {
            Selection selection = selectionDao.findByName(name);
            if (selection != null) {
                try (SelectionKvAttributeDAO selectionKvAttributeDAO = TagJdbi.selectionKvAttributeDao()) {
                    List<SelectionKvAttribute> kvAttributes = selectionKvAttributeDAO.findBySelectionIdWithValues(selection.getId());
                    SelectionBuilder builder = SelectionBuilder.factory().selection();
                    for (SelectionKvAttribute kvAttribute : kvAttributes) {
                        builder.textAttributeKV(kvAttribute.getKvAttributeName(), kvAttribute.getKvAttributeValue());
                    }
                    return builder.build().getSelection().get(0);
                }
            }
        }
        return null;
    }

    protected SelectionType findSelectionById(String id) throws SQLException {
        try (SelectionDAO selectionDao = TagJdbi.selectionDao()) {
            Selection selection = selectionDao.findById(UUID.valueOf(id));
            if (selection != null) {
                try (SelectionKvAttributeDAO selectionKvAttributeDAO = TagJdbi.selectionKvAttributeDao()) {
                    SelectionBuilder builder = SelectionBuilder.factory().selection();
                    List<SelectionKvAttribute> kvAttributes = selectionKvAttributeDAO.findBySelectionIdWithValues(selection.getId());
                    for (SelectionKvAttribute kvAttribute : kvAttributes) {
                        builder.textAttributeKV(kvAttribute.getKvAttributeName(), kvAttribute.getKvAttributeValue());
                    }
                    return builder.build().getSelection().get(0);
                }
            }
        }
        return null;
    }

    protected SelectionType getInlineOrLookupSelection(SelectionType selection) throws SQLException {
        if (selection.getAttribute().isEmpty()) {
            if (selection.getName() != null) {
                return findSelectionByName(selection.getName());
            } else if (selection.getId() != null) {
                return findSelectionById(selection.getName());
            } else {
                // did not find a selection on the server and the selection xml is broken (selection/>)
                throw new IllegalArgumentException("Invalid selection");
            }
        }
        // if it's not empty we use the included attributes and do not need to look anything up on the server
        return selection;
    }

    /**
     * If there is a currently valid (notBefore<today<notAfter) selection that
     * mentions the subject, that selection will be used. Otherwise, if there is
     * a default selection (does not mention any subjects) that selection will
     * be used. Otherwise, if the server is configured with a default selection,
     * that selection will be used. Otherwise, an error will be returned because
     * a selection could not be found.
     *
     */
    protected SelectionType findSelectionForSubject(UUID targetSubject, SelectionsType selections) throws Exception {
        SelectionsType currentSelections = getCurrentSelections(selections);
        // first search by uuid
        for (SelectionType selection : currentSelections.getSelection()) {
            for (SubjectType subject : selection.getSubject()) {
                if (subject.getUuid() != null) {
                    log.debug("Does targetSubject [{}] = selectionSubject [{}]?", targetSubject.toString(),subject.getUuid().getValue().toLowerCase());
                    if (targetSubject.toString().equalsIgnoreCase(subject.getUuid().getValue().toLowerCase())) {
                        // found a selection with the target subject uuid
                        return getInlineOrLookupSelection(selection);
                    }
                }
            }
        }
        // second search by ip or name
        for (SelectionType selection : currentSelections.getSelection()) {
            for (SubjectType subject : selection.getSubject()) {
                if (subject.getIp() != null) {
                    String uuid = findSubjectHardwareUuid(subject.getIp().getValue());
                    if (uuid != null) {
                        log.debug("Does targetSubject [{}] = selectionSubject [{}]?", targetSubject.toString(),uuid);
                        if (targetSubject.toString().equalsIgnoreCase(uuid.toLowerCase())) {
                            // found a selection with the target subject uuid
                            return getInlineOrLookupSelection(selection);
                        }
                    }
                }
            }
        }
        // third search for default selection (no subjects declared) return the first one found
        if( currentSelections.getDefault() != null ) {
            for (SelectionType selection : currentSelections.getDefault().getSelection()) {
                log.debug("Using first currently valid default selection");
                return getInlineOrLookupSelection(selection); // get first default
            }
        }
        /*
        // fourth look for a server default selection - disabling this for now because it may be confusing to customers to have a server default behind their own selections file default because if they choose not to supply a default in their file they may still get one from the server.
        String defaultSelectionName = configuration.getTagProvisionSelectionDefault();
        if (defaultSelectionName != null && !defaultSelectionName.isEmpty()) {
            return findSelectionByName(defaultSelectionName);
        }
        */
        throw new IllegalArgumentException("No matching selection");
    }

    public byte[] createTagCertificate(UUID subject, SelectionsType selections) throws Exception {
        // check if we have a private key to use for signing
        PrivateKey cakey = Global.cakey();
        X509Certificate cakeyCert = Global.cakeyCert();
        if (cakey == null || cakeyCert == null) {
            throw new IllegalStateException("Missing tag certificate authority key");
        }
        X509AttrBuilder builder = X509AttrBuilder.factory()
                .issuerName(cakeyCert)
                .issuerPrivateKey(cakey)
                .dateSerial()
                .subjectUuid(subject)
                .expires(configuration.getTagValiditySeconds(), TimeUnit.SECONDS);

        SelectionType selection = findSelectionForSubject(subject, selections);
        for (AttributeType attribute : selection.getAttribute()) {
            // TODO:  oid support extensions
            if (attribute.getDer() != null) {
                ASN1Object asn1 = ASN1Object.fromByteArray(attribute.getDer().getValue());
                builder.attribute(new ASN1ObjectIdentifier(attribute.getOid()), asn1);
            } else if (attribute.getOid().equals("2.5.4.789.1")) {
                if (attribute.getText() != null) {
                    // TODO: move to a text parser for this oid
                    String[] parts = attribute.getText().getValue().split("=");  // name=value
                    builder.attribute(new ASN1ObjectIdentifier(UTF8NameValueMicroformat.OID), new UTF8NameValueMicroformat(parts[0], parts[1]));
                }
            } else if (attribute.getOid().equals("2.5.4.789.2")) {
                if (attribute.getText() != null) {
                    throw new UnsupportedOperationException("text format not implemented yet for 2.5.4.789.2"); // typically 2.5.4.789.2 would use der format anyway...
                }
            } else {
                throw new UnsupportedOperationException("text format not implemented yet for " + attribute.getOid());
            }
        }
        byte[] attributeCertificateBytes = builder.build();
        if (attributeCertificateBytes == null) {
            log.error("Cannot build attribute certificate");
            for (Fault fault : builder.getFaults()) {
                log.error(String.format("%s%s", fault.toString(), fault.getCause() == null ? "" : ": " + fault.getCause().getMessage()));
            }
            throw new IllegalArgumentException("Cannot build attribute certificate");
        }

        // if auto-import to mtwilson is enabled, do it here, but if there is an exception we only log it
        try {
            if (configuration.isTagProvisionAutoImport()) {
                String url = My.configuration().getAssetTagMtWilsonBaseUrl();
                if (url != null && !url.isEmpty()) {
                    AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
                    request.setCertificate(attributeCertificateBytes);
                    log.debug("import cert to MTW ");
                    Global.mtwilson().importAssetTagCertificate(request);
                }
            }
        } catch (IOException | ApiException | SignatureException e) {
            log.error("Failed to auto-import tag certificate to Mt Wilson", e);
        }

        return attributeCertificateBytes;



    }
}
