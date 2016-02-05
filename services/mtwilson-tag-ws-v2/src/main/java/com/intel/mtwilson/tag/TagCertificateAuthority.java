/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.*;
import com.intel.mtwilson.tag.model.*;
import com.intel.mtwilson.tag.selection.SelectionBuilder;
import com.intel.mtwilson.tag.selection.SelectionUtil;
import com.intel.mtwilson.tag.selection.xml.*;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * @author jbuhacoff
 */
public class TagCertificateAuthority {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateAuthority.class);
    private TagConfiguration configuration;

    public TagCertificateAuthority(TagConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Looks up hardware uuid of host in mtwilson; host already be registered.
     *
     * @param ip address or hostname
     * @return
     */
    public String findSubjectHardwareUuid(String ip) throws IOException, ApiException, SignatureException {
        log.debug("Querying host {} in Mt Wilson", ip);
        List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(ip, true);
        if (hostList == null || hostList.isEmpty()) {
            log.debug("host uuid lookup didn't return back any results");
            //throw new ASException(new Exception("No host records found, please verify your host is in mtwilson or provide a hardware uuid in the subject field.")); // 
            log.warn("No host records found for {}, please verify your host is in mtwilson or provide a hardware uuid in the subject field", ip);
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
            log.debug("Selection does not have inline attributes");
            if (selection.getId() != null) {
                SelectionType found = findSelectionById(selection.getId());
                if (found != null) {
                    log.debug("Found selection by id {}", selection.getId());
                    return found;
                }
            }
            if (selection.getName() != null) {
                SelectionType found = findSelectionByName(selection.getName());
                if (found != null) {
                    log.debug("Found selection by name {}", selection.getName());
                    return found;
                }
            }
            // if there are no inline <attribute>...</attribute> tags then either <selection name="..."/> or <selection id="..."/> xml attributes must be specified so we can look up the selected attributes in the database
            if( selection.getId() == null && selection.getName() == null ) {
                throw new IllegalArgumentException("Empty selection with no id or name");
            }
            // if id or name attributes were specified but we didn't find them in the database, it's also an error
            throw new IllegalArgumentException("Cannot find selection by id or name");
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
    public SelectionType findCurrentSelectionForSubject(UUID targetSubject, SelectionsType selections) 
            throws SQLException, IOException, ApiException, SignatureException {
        log.debug("findSelectionForSubject {}", targetSubject.toString());
        SelectionsType currentSelections = SelectionUtil.copySelectionsValidOn(selections, new Date());
        // first search by host uuid
        for (SelectionType selection : currentSelections.getSelection()) {
            for (SubjectType subject : selection.getSubject()) {
                if (subject.getUuid() != null) {
                    log.debug("comparing to selection subject uuid {}", subject.getUuid().getValue().toLowerCase());
                    if (targetSubject.toString().equalsIgnoreCase(subject.getUuid().getValue().toLowerCase())) {
                        // found a selection with the target subject uuid
                        return getInlineOrLookupSelection(selection);
                    }
                }
            }
        }
        // second search by host ip or host name
        for (SelectionType selection : currentSelections.getSelection()) {
            for (SubjectType subject : selection.getSubject()) {
                if (subject.getIp() != null) {
                    log.debug("looking up uuid for host with ip {}", subject.getIp().getValue());
                    String uuid = findSubjectHardwareUuid(subject.getIp().getValue()); 
                    if (uuid != null) {
                        log.debug("comparing to found selection subject uuid {}", uuid);
                        if (targetSubject.toString().equalsIgnoreCase(uuid.toLowerCase())) {
                            // found a selection with the target subject uuid
                            return getInlineOrLookupSelection(selection);
                        }
                    }
                }
            }
        }
        // third search for default selection (no subjects declared) return the first one found
        if (currentSelections.getDefault() != null) {
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
        //throw new IllegalArgumentException("No matching selection");
        return null; // no matching selection - let the caller decide if it's an error or not
    }

    /**
     * Finds the best matching selection for the give subject in the provided
     * selections object, based on validity dates and specified subjects and
     * using the default selections if provided and no better match can be
     * found.
     *
     * @param subject
     * @param selections representing an entire "xml selections file"
     * @return
     * @throws Exception
     */
    public byte[] createTagCertificate(UUID subject, SelectionsType selections) throws SQLException, IOException, ApiException, SignatureException {
        SelectionType selection = findCurrentSelectionForSubject(subject, selections);
        if( selection == null ) {
            throw new IllegalArgumentException("No matching selection");
        }
        return createTagCertificate(subject, selection);
    }
    

    /**
     * Does not attempt to match the subject to the selection. Do not call
     * directly unless you have already verified that you want to create a
     * certificate for the given subject with the given selection with no
     * further checks.
     *
     * @param subject
     * @param selection element representing a set of host attributes by
     * reference via the selection uuid or selection name or inline via the
     * attribute elements
     * @return
     * @throws Exception
     */
    public byte[] createTagCertificate(UUID subject, SelectionType selection) throws IOException {
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

        for (AttributeType attribute : selection.getAttribute()) {
            X509AttrBuilder.Attribute oidAndValue = Util.toAttributeOidValue(attribute);
            builder.attribute(oidAndValue.oid, oidAndValue.value);
        }
        byte[] attributeCertificateBytes = builder.build();
        if (attributeCertificateBytes == null) {
            log.error("Cannot build attribute certificate");
            for (Fault fault : builder.getFaults()) {
                log.error(String.format("%s: %s", fault.getClass().getName(), fault.toString()));
            }
            throw new IllegalArgumentException("Cannot build attribute certificate");
        }

        // if auto-import to mtwilson is enabled, do it here, but if there is an exception we only log it
        try {
            log.debug("Tag certificate auto-import enabled: {}", configuration.isTagProvisionAutoImport());
            if (configuration.isTagProvisionAutoImport()) {
                String url = My.configuration().getAssetTagMtWilsonBaseUrl();
                log.debug("Mt Wilson URL: {}", url);
                if (url != null && !url.isEmpty()) {
                    AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
                    request.setCertificate(attributeCertificateBytes);
                    log.debug("Importing tag certificate to Mt Wilson");
                    Global.mtwilson().importAssetTagCertificate(request);
                }
            }
        } catch (IOException | ApiException | SignatureException e) {
            log.error("Failed to auto-import tag certificate to Mt Wilson", e);
        }

        return attributeCertificateBytes;



    }
}
