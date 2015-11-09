/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.HostUuidCollection;
import com.intel.mtwilson.tag.model.HostUuidFilterCriteria;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
public class HostUuids extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public HostUuids(URL url) throws Exception{
        super(url);
    }

    public HostUuids(Properties properties) throws Exception {
        super(properties);
    }    
    /**
     * Given the IP address, this helper function retrieves the hardware UUID of the host. Only constraint is that the
     * host should already be registered with the system.
     * @param criteria HostUuidFilterCriteria object specifying the filter criteria. The 
     * only search option currently supported is the hostNameEqualTo, which is either the IP address or the FQDN name of the host. Note that
     * this name has to match with what is being registered with the system.
     * @return HostUuidCollection object with the hardware uuid of the host matching the specified filter criteria. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_uuids:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-uuids?hostNameEqualTo=192.168.0.1
     * Output: {"host_uuids":[{"hardware_uuid":"064866ea-620d-11e0-b1a9-001e671043c4"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  HostUuids client = new HostUuids(My.configuration().getClientProperties());
     *  HostUuidFilterCriteria criteria = new HostUuidFilterCriteria();
     *  criteria.hostNameEqualTo = "192.168.0.1";
     *  HostUuidCollection objCollection = client.searchHostUuids(criteria);
     * </pre>
     */
    public HostUuidCollection searchHostUuids(HostUuidFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostUuidCollection objCollection = getTargetPathWithQueryParams("host-uuids", criteria).request(MediaType.APPLICATION_JSON).get(HostUuidCollection.class);
        return objCollection;
    }
    
    
}
