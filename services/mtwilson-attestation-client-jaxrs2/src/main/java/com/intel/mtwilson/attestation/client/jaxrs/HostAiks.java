/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostAik;
import com.intel.mtwilson.as.rest.v2.model.HostAikCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
public class HostAiks extends MtWilsonClient {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAiks.class);

    public HostAiks(URL url)throws Exception {
        super(url);
    }

    public HostAiks(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the AIK Sha1 and the AIK public key details for the specified host. Note that currently only one AIK is supported per host.
     * @param uuid - UUID of the Host for which the AIK details need to be retrieved. 
     * @return Host AIK details retrieved from the system with the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_aiks:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/d24dd52f-524e-43aa-8673-4013ecf64a4a/aiks/d24dd52f-524e-43aa-8673-4013ecf64a4a
     * Output: {"id":"d24dd52f-524e-43aa-8673-4013ecf64a4a","aik_sha1":"0dfa39952dec39848990acac56a7ec8787bef1d4",
     * "aik_public_key":"-----BEGIN PUBLIC KEY-----\r\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzjfKA2qYuZjG6jr/q5WxyVUhXAmAmqmh\r\n/i
     * RegwN4eFLksYGcCcMa9NZKi7gGvGHHEdmI2d+Wvt19DoKEENi93UVa+TwARl44bbu3sJZyw4Kq\r\n8dkScgM8SZ3TS/qhBX7DritlzVj7FPk4SjrVrwVhr+Z7FCTn90/
     * VB3EZtSL9Kl3rrX06QdB/3xE8\r\n9E8wErKmXvFo/pUZ98tHpDyGEUrDRohjKVFBdHRIEVxh5jfTWQdFSlVygDhUHPFeGNYbevNpm9h9\r\nUVjsQWvzDRKeFVXHN6IEx
     * ULw5dON4qbmdtvcXp/KQmoJ+gRCyN2UPX3S5J3tMcehIbzOQrIz0VP8\r\nR6EMyQIDAQAB\r\n-----END PUBLIC KEY-----\r\n"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAiks client = new HostAiks(getClientProperties());
     *   HostAik retrieveHostAik = client.retrieveHostAik("d24dd52f-524e-43aa-8673-4013ecf64a4a");
     * </pre>
    */    
    public HostAik retrieveHostAik(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", hostUuid);
        // We are passing the host UUID to "id" also even though it will not be used (without this framework treats this call as a 
        // search call instead of a retrieve call. Since there will be only one aik for a host, we can retrieve
        // the aik for the host uniquely with the host uuid itself.
        map.put("id", hostUuid); 
        HostAik obj = getTarget().path("hosts/{host_id}/aiks/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAik.class);
        return obj;
    }
    
    /**
     * Searches for the host AIKs with the specified criteria. Since the system currently supports only one AIK per host, only 
     * criteria that is supported is the UUID of the host for which the AIK details need to be retrieved.
     * @param criteria HostFilterCriteria object that specifies the search criteria.
     * @return HostAikCollection object with a list of AIKs that match the filter criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_aiks:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/d24dd52f-524e-43aa-8673-4013ecf64a4a/aiks
     * Output: {"aiks":[{"id":"d24dd52f-524e-43aa-8673-4013ecf64a4a","aik_sha1":"0dfa39952dec39848990acac56a7ec8787bef1d4",
     * "aik_public_key":"-----BEGIN PUBLIC KEY-----\r\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzjfKA2qYuZjG6jr/q5WxyVUhXAmAmqm
     * h\r\n/iRegwN4eFLksYGcCcMa9NZKi7gGvGHHEdmI2d+Wvt19DoKEENi93UVa+TwARl44bbu3sJZyw4Kq\r\n8dkScgM8SZ3TS/qhBX7DritlzVj7FPk4SjrVrw
     * Vhr+Z7FCTn90/VB3EZtSL9Kl3rrX06QdB/3xE8\r\n9E8wErKmXvFo/pUZ98tHpDyGEUrDRohjKVFBdHRIEVxh5jfTWQdFSlVygDhUHPFeGNYbevNpm9h9\r\nU
     * VjsQWvzDRKeFVXHN6IExULw5dON4qbmdtvcXp/KQmoJ+gRCyN2UPX3S5J3tMcehIbzOQrIz0VP8\r\nR6EMyQIDAQAB\r\n-----END PUBLIC KEY-----\r\n"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAiks client = new HostAiks(getClientProperties());
     *   HostAik retrieveHostAik = client.retrieveHostAik("d24dd52f-524e-43aa-8673-4013ecf64a4a");
     *   HostAikFilterCriteria criteria = new HostAikFilterCriteria();
     *   criteria.hostUuid = "d24dd52f-524e-43aa-8673-4013ecf64a4a";
     *   HostAikCollection searchHostAiks = client.searchHostAiks(criteria);
     * </pre>
     */    
    public HostAikCollection searchHostAiks(HostAikFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", criteria.hostUuid);
        HostAikCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/aiks", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAikCollection.class);
        return objCollection;
    }
}
