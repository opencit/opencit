/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tls.policy.repository;

import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicy;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyLocator;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyDAO;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyJdbiFactory;
import com.intel.mtwilson.tls.policy.jdbi.TlsPolicyRecord;
import com.intel.mtwilson.tls.policy.reader.impl.JsonTlsPolicyReader;
import com.intel.mtwilson.tls.policy.reader.impl.JsonTlsPolicyWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyRepository implements SimpleRepository<HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, HostTlsPolicyLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTlsPolicyRepository.class);

    @Override
    @RequiresPermissions("host_tls_policies:search")
    public HostTlsPolicyCollection search(HostTlsPolicyFilterCriteria criteria) {
        HostTlsPolicyCollection objCollection = new HostTlsPolicyCollection();

        // always populate the meta element in the result set with the current server default or global policies so client knows what it will get if it doesn't specify any policy or that a global policy is in effect so specifying a policy would not have an effect other than to store it for when the global policy is removed
        objCollection.getMeta().put("allow", My.configuration().getTlsPolicyAllow());
        objCollection.getMeta().put("default", My.configuration().getDefaultTlsPolicyId());
        objCollection.getMeta().put("global", My.configuration().getGlobalTlsPolicyId());

        try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
            List<TlsPolicyRecord> result = new ArrayList<>();
            if (criteria.filter) {
                // id search
                if (criteria.id != null) {
                    String[] ids = criteria.id.split("\\s*,\\s*");
                    log.debug("HostTlsPolicyRepository search ids: {}", (Object[]) ids);
                    HashSet<String> set = new HashSet<>(Arrays.asList(ids));
                    List<TlsPolicyRecord> tlsPolicyRecords = dao.findTlsPolicyByIds(set);
                    if (tlsPolicyRecords != null) {
                        result.addAll(tlsPolicyRecords);
                    }
                } else if (criteria.hostId != null) {
                    TlsPolicyRecord tlsPolicyRecord = dao.findTlsPolicyByHostId(criteria.hostId);
                    if (tlsPolicyRecord != null) {
                        result.add(tlsPolicyRecord);
                    }
                } else if (criteria.privateEqualTo != null) {
                    List<TlsPolicyRecord> tlsPolicyRecords = dao.findTlsPolicyByPrivateEqualTo(criteria.privateEqualTo.booleanValue());
                    if (tlsPolicyRecords != null) {
                        result.addAll(tlsPolicyRecords);
                    }
                } else if (criteria.nameEqualTo != null) {
                    TlsPolicyRecord tlsPolicyRecord = dao.findTlsPolicyByNameEqualTo(criteria.nameEqualTo);
                    if (tlsPolicyRecord != null) {
                        result.add(tlsPolicyRecord);
                    }
                } else if (criteria.nameContains != null) {
                    List<TlsPolicyRecord> tlsPolicyRecords = dao.findTlsPolicyByNameContains(criteria.nameContains);
                    if (tlsPolicyRecords != null) {
                        result.addAll(tlsPolicyRecords);
                    }
                } else if (criteria.commentEqualTo != null) {
                    TlsPolicyRecord tlsPolicyRecord = dao.findTlsPolicyByCommentEqualTo(criteria.commentEqualTo);
                    if (tlsPolicyRecord != null) {
                        result.add(tlsPolicyRecord);
                    }
                } else if (criteria.commentContains != null) {
                    List<TlsPolicyRecord> tlsPolicyRecords = dao.findTlsPolicyByCommentContains(criteria.commentContains);
                    if (tlsPolicyRecords != null) {
                        result.addAll(tlsPolicyRecords);
                    }
                }
            } else {
                // no filter, get all records
                List<TlsPolicyRecord> tlsPolicyRecords = dao.findAllTlsPolicy();
                if (tlsPolicyRecords != null) {
                    result.addAll(tlsPolicyRecords);
                }
            }
            for (TlsPolicyRecord record : result) {
                HostTlsPolicy policy = convert(record);
                objCollection.getTlsPolicies().add(policy);
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return objCollection;
    }
    /*

     TblHostsJpaController jpaController = My.jpa().mwHosts();
     // when searching for a private policy, the policy name is the host uuid
     if (criteria.privateEqualTo != null && criteria.privateEqualTo.booleanValue() && criteria.nameEqualTo != null) {
     TblHosts obj = jpaController.findHostByUuid(criteria.nameEqualTo.toString());
     if (obj != null) {
     objCollection.getTlsPolicies().add(convert(obj));
     }
     }
     * 
     */

    @Override
    @RequiresPermissions("host_tls_policies:retrieve")
    public HostTlsPolicy retrieve(HostTlsPolicyLocator locator) {
        if (locator.id == null) {
            return null;
        }
        try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
            TlsPolicyRecord policyRecord = dao.findTlsPolicyById(locator.id);
            if (policyRecord != null) {
                HostTlsPolicy policy = convert(policyRecord);
                return policy;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    @Override
    @RequiresPermissions("host_tls_policies:store")
    public void store(HostTlsPolicy item) {
        if (item.getId() == null) {
            return;
        }
        try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
            TlsPolicyRecord record = convert(item);
            dao.updateTlsPolicy(record);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }
    /*

     try {
     TblHostsJpaController jpaController = My.jpa().mwHosts();
     TblHosts obj = jpaController.findHostByUuid(item.getHostUuid()); 
     if (obj != null) {
     obj.setTlsPolicyName(item.getName());
     //                obj.setTlsKeystore(item.getKeyStore());
     jpaController.edit(obj);
     }
     } catch (ASException aex) {
     throw aex;            
     } catch (Exception ex) {
     log.error("Error during update of host Tls Policy.", ex);
     throw new ASException(ErrorCode.AS_UPDATE_HOST_ERROR, ex.getClass().getSimpleName());
     */

    @Override
    @RequiresPermissions("host_tls_policies:create")
    public void create(HostTlsPolicy item) {
        if (item.getId() == null) {
            return;
        }
        try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
            TlsPolicyRecord record = convert(item);
            dao.insertTlsPolicy(record);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @RequiresPermissions("host_tls_policies:delete")
    public void delete(HostTlsPolicyLocator locator) {
        if (locator.id == null) {
            return;
        }
        try (TlsPolicyDAO dao = TlsPolicyJdbiFactory.tlsPolicyDAO()) {
            dao.deleteTlsPolicyById(locator.id);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @RequiresPermissions("host_tls_policies:delete,search")
    public void delete(HostTlsPolicyFilterCriteria criteria) {
        HostTlsPolicyCollection result = search(criteria);
        for (HostTlsPolicy hostTlsPolicy : result.getTlsPolicies()) {
            HostTlsPolicyLocator locator = new HostTlsPolicyLocator();
            locator.id = hostTlsPolicy.getId();
            delete(locator);
        }
    }

    /*
     private HostTlsPolicy convert(TblHosts obj) {
     HostTlsPolicy convObj = new HostTlsPolicy();
     //        convObj.setHostUuid(obj.getUuid_hex());
     convObj.setName(obj.getTlsPolicyName());
     //        convObj.setKeyStore(obj.getTlsKeystore());
     return convObj;
     }
     */
    private HostTlsPolicy convert(TlsPolicyRecord policyRecord) {
        HostTlsPolicy policy = new HostTlsPolicy();
        policy.setId(policyRecord.getId());
        policy.setName(policyRecord.getName());
        policy.setPrivate(policyRecord.isPrivate());
        policy.setComment(policyRecord.getComment());
        JsonTlsPolicyReader reader = new JsonTlsPolicyReader();
        if (reader.accept(policyRecord.getContentType()) && policyRecord.getContent() != null && policyRecord.getContent().length > 0) {
            TlsPolicyDescriptor descriptor = reader.read(policyRecord.getContent());
            policy.setDescriptor(descriptor);
        }
        return policy;
    }

    private TlsPolicyRecord convert(HostTlsPolicy policy) {
        TlsPolicyRecord record = new TlsPolicyRecord();
        record.setId(policy.getId());
        record.setName(policy.getName());
        record.setPrivate(policy.isPrivate());
        record.setComment(policy.getComment());
        JsonTlsPolicyWriter writer = new JsonTlsPolicyWriter();
        record.setContentType("application/json"); //  ;charset=utf-8
        record.setContent(writer.write(policy.getDescriptor()));
        return record;
    }
}
