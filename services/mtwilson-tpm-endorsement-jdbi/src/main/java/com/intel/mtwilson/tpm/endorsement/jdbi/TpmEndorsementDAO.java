package com.intel.mtwilson.tpm.endorsement.jdbi;

import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import java.io.Closeable;
import java.util.List;
import java.util.Set;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

/**
 * <pre>
CREATE TABLE mw_tpm_endorsement (
  id char(36) NOT NULL,
  issuer varchar(255) NOT NULL,
  revoked boolean NOT NULL DEFAULT FALSE,
  certificate blob NOT NULL,
  comment text NULL,
  PRIMARY KEY (id)
);
 * </pre>
 * @author jbuhacoff
 */
@UseStringTemplate3StatementLocator
@RegisterArgumentFactory({UUIDArgument.class})
@RegisterMapper({TpmEndorsementResultMapper.class,})
public interface TpmEndorsementDAO extends Closeable {
    @SqlUpdate("insert into mw_tpm_endorsement (id, hardware_uuid, issuer, revoked, certificate, comment) values (:id, :hardwareUuid, :issuer, :revoked, :certificate, :comment)")
     void insertTpmEndorsement(@BindBean TpmEndorsement TpmEndorsementRecord);
//    void insertTpmEndorsement(@Bind("id") String id, @Bind("issuer") String issuer, @Bind("impl") String impl, @Bind("certificate_type") String certificateType, @Bind("certificate") byte[] certificate, @Bind("comment") String comment);

    @SqlUpdate("update mw_tpm_endorsement set hardware_uuid=:hardwareUuid, issuer=:issuer, revoked=:revoked, certificate=:certificate, comment=:comment where id=:id")
    void updateTpmEndorsement(@BindBean TpmEndorsement TpmEndorsementRecord);
//    void updateTpmEndorsement(@Bind("id") String id, @Bind("issuer") String issuer, @Bind("impl") String impl, @Bind("certificate_type") String certificateType, @Bind("certificate") byte[] certificate, @Bind("comment") String comment);
    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement")
    List<TpmEndorsement> findAllTpmEndorsement();
    
    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where id=:id")
    TpmEndorsement findTpmEndorsementById(@Bind("id") UUID id);

    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where id in ( <ids> )")
    List<TpmEndorsement> findTpmEndorsementByIds(@BindIn("ids") Set<String> ids);

    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where hardware_uuid=:hardware_uuid")
    TpmEndorsement findTpmEndorsementByHardwareUuidEqualTo(@Bind("hardware_uuid") String hardwareUuid);
        
    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where issuer=:issuer")
    TpmEndorsement findTpmEndorsementByIssuerEqualTo(@Bind("issuer") String issuer);

    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where issuer like concat('%',:issuer,'%')")
    List<TpmEndorsement> findTpmEndorsementByIssuerContains(@Bind("issuer") String issuer);

    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where revoked=:revoked")
    List<TpmEndorsement> findTpmEndorsementByRevokedEqualTo(@Bind("revoked") boolean revoked);

    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where comment=:comment")
    TpmEndorsement findTpmEndorsementByCommentEqualTo(@Bind("comment") String comment);

    @SqlQuery("select id, hardware_uuid, issuer, revoked, certificate, comment from mw_tpm_endorsement where comment like concat('%',:comment,'%')")
    List<TpmEndorsement> findTpmEndorsementByCommentContains(@Bind("comment") String comment);
    
    @SqlUpdate("delete from mw_tpm_endorsement where id=:id")
    void deleteTpmEndorsementById(@Bind("id") UUID id);

}
