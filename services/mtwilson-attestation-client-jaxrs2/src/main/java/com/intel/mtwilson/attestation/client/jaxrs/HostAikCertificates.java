/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostAikCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostAikCertificates(URL url) throws Exception{
        super(url);
    }

    public HostAikCertificates(Properties properties) throws Exception {
        super(properties);
    }

    /**
     * Associates the host with a new AIK certificate specified.
     * @param obj HostAikCertificate object with the details of the AIK certificate to be associated with the host. 
     * @return HostAikCertificate created in the system.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_aik_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML 
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/d24dd52f-524e-43aa-8673-4013ecf64a4a/aik-certificates
     * Input: {"certificate":"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlDdlRDQ0FhV2dBd0lCQWdJR0FVZWE0TWt2TUEwR0NTcUdTSWIz
     *          RFFFQkJRVUFNQnN4R1RBWEJnTlZCQU1URUcxMGQybHNjMjl1DQpMWEJqWVMxaGFXc3dIaGNOTVRRd09EQXpNRGMxT0RRNFdoY05NalF3T0
     *          RBeU1EYzFPRFE0V2pBQU1JSUJJakFOQmdrcWhraUc5dzBCDQpBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF6amZLQTJxWXVaakc2anIvcTVX
     *          eHlWVWhYQW1BbXFtaC9pUmVnd040ZUZMa3NZR2NDY01hDQo5TlpLaTdnR3ZHSEhFZG1JMmQrV3Z0MTlEb0tFRU5pOTNVVmErVHdBUmw0NG
     *          JidTNzSlp5dzRLcThka1NjZ004U1ozVFMvcWhCWDdEDQpyaXRselZqN0ZQazRTanJWcndWaHIrWjdGQ1RuOTAvVkIzRVp0U0w5S2wzcnJY
     *          MDZRZEIvM3hFODlFOHdFckttWHZGby9wVVo5OHRIDQpwRHlHRVVyRFJvaGpLVkZCZEhSSUVWeGg1amZUV1FkRlNsVnlnRGhVSFBGZUdOWW
     *          Jldk5wbTloOVVWanNRV3Z6RFJLZUZWWEhONklFDQp4VUx3NWRPTjRxYm1kdHZjWHAvS1Ftb0orZ1JDeU4yVVBYM1M1SjN0TWNlaEliek9R
     *          ckl6MFZQOFI2RU15UUlEQVFBQm95SXdJREFlDQpCZ05WSFJFQkFmOEVGREFTZ1JCSVNWTWdTV1JsYm5ScGRIa2dTMlY1TUEwR0NTcUdTSWI
     *          zRFFFQkJRVUFBNElCQVFCYXFmT2Jvc29tDQpEcDhWTEczd2lLR05nVzZycHN0OGJsVkFSZGhVd2xCSHFuNjFDcUFOSkh2S09ld2hzd1BKWW
     *          Qzc0JoS1hHcUFMWXRGOE55b1JYQ05jDQp0SXlGU3hLekFySnprOEprZk5OdWhrUm13SmNLNjcyTW9hdDkxZzNUOVR6enpPcWdXa05xU1JiO
     *          TRLSXdOQjYvakRDa2hTWGx1RVhsDQpvUnFsczIzc1dUNy9FUlNEQVBTZWFnOE10RTBKRThDMVkydlBDdE9raFA0bWVnR0F3ZWQxcXpPbVNr
     *          WHBVdVJJczNxcXFScWFrTldtDQpFVTJubURCZWwzTHFNRFE3NTl4NCszRTNUOWMvNkdDcThKMlN6N1pwcWRjSFhvVDN1aDRlRSszWEpjS3p
     *          WYyszSkFhNDd4Z2o2VnpODQppbEhhVXJIRk1ER3ROcDJjb3JnYUxEeFFGVC9kDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo="}
     * 
     * Output: {"id":"c9a6e943-301c-4984-870c-ce3aafbe4e94","host_uuid":"d24dd52f-524e-43aa-8673-4013ecf64a4a","certificate":
     *          "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlDdlRDQ0FhV2dBd0lCQWdJR0FVZWE0TWt2TUEwR0NTcUdTSWIzRFFFQkJRVUFNQnN4
     *          R1RBWEJnTlZCQU1URUcxMGQybHNjMjl1DQpMWEJqWVMxaGFXc3dIaGNOTVRRd09EQXpNRGMxT0RRNFdoY05NalF3T0RBeU1EYzFPRFE0V2pBQ
     *          U1JSUJJakFOQmdrcWhraUc5dzBCDQpBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF6amZLQTJxWXVaakc2anIvcTVXeHlWVWhYQW1BbXFtaC9pUm
     *          Vnd040ZUZMa3NZR2NDY01hDQo5TlpLaTdnR3ZHSEhFZG1JMmQrV3Z0MTlEb0tFRU5pOTNVVmErVHdBUmw0NGJidTNzSlp5dzRLcThka1NjZ00
     *          4U1ozVFMvcWhCWDdEDQpyaXRselZqN0ZQazRTanJWcndWaHIrWjdGQ1RuOTAvVkIzRVp0U0w5S2wzcnJYMDZRZEIvM3hFODlFOHdFckttWHZG
     *          by9wVVo5OHRIDQpwRHlHRVVyRFJvaGpLVkZCZEhSSUVWeGg1amZUV1FkRlNsVnlnRGhVSFBGZUdOWWJldk5wbTloOVVWanNRV3Z6RFJLZUZWW
     *          EhONklFDQp4VUx3NWRPTjRxYm1kdHZjWHAvS1Ftb0orZ1JDeU4yVVBYM1M1SjN0TWNlaEliek9Rckl6MFZQOFI2RU15UUlEQVFBQm95SXdJRE
     *          FlDQpCZ05WSFJFQkFmOEVGREFTZ1JCSVNWTWdTV1JsYm5ScGRIa2dTMlY1TUEwR0NTcUdTSWIzRFFFQkJRVUFBNElCQVFCYXFmT2Jvc29tDQp
     *          EcDhWTEczd2lLR05nVzZycHN0OGJsVkFSZGhVd2xCSHFuNjFDcUFOSkh2S09ld2hzd1BKWWQzc0JoS1hHcUFMWXRGOE55b1JYQ05jDQp0SXlG
     *          U3hLekFySnprOEprZk5OdWhrUm13SmNLNjcyTW9hdDkxZzNUOVR6enpPcWdXa05xU1JiOTRLSXdOQjYvakRDa2hTWGx1RVhsDQpvUnFsczIzc
     *          1dUNy9FUlNEQVBTZWFnOE10RTBKRThDMVkydlBDdE9raFA0bWVnR0F3ZWQxcXpPbVNrWHBVdVJJczNxcXFScWFrTldtDQpFVTJubURCZWwzTH
     *          FNRFE3NTl4NCszRTNUOWMvNkdDcThKMlN6N1pwcWRjSFhvVDN1aDRlRSszWEpjS3pWYyszSkFhNDd4Z2o2VnpODQppbEhhVXJIRk1ER3ROcDJ
     *          jb3JnYUxEeFFGVC9kDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo="
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAikCertificates client = new HostAikCertificates(getClientProperties());
     *   HostAikCertificate aikCert = new HostAikCertificate();
     *   aikCert.setHostUuid("d24dd52f-524e-43aa-8673-4013ecf64a4a");
     *   aikCert.setCertificate(certificate);
     *   client.createHostAikCertificate(aikCert);
     * </pre>
     */    
    public HostAikCertificate createHostAikCertificate(HostAikCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", obj.getHostUuid());
        HostAikCertificate newObj = getTarget().path("hosts/{host_id}/aik-certificates").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), HostAikCertificate.class);
        return newObj;
    }
    
    /**
     * Searches for the host's AIK certificate with the specified criteria. Since the system currently supports only one AIK per host, only 
     * criteria that is supported is the UUID of the host for which the AIK certificate need to be retrieved.
     * @param criteria HostAikCertificateFilterCriteria object that specifies the search criteria.
     * @return HostAikCertificateCollection object with a list of AIK certificates that match the filter criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_aik_certificates:search
     * @mtwContentTypeReturned application/json OR application/x-pem-file (just Certificate in the PEM format would be returned)
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/d24dd52f-524e-43aa-8673-4013ecf64a4a/aik-certificates
     * Output: {"aik_certificates":[{"id":"d24dd52f-524e-43aa-8673-4013ecf64a4a","aik_sha1":"0dfa39952dec39848990acac56a7ec8787bef1d4",
     *          "certificate":"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlDdlRDQ0FhV2dBd0lCQWdJR0FV.....RCBDRVJUSUZJQ0FURS0tLS0tDQo="}]}
     * Output (ContentType- application/x-pem-file): -----BEGIN CERTIFICATE-----
     *          MIICvTCCAaWgAwIBAgIGAUea4MkvMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u
     *          LXBjYS1haWswHhcNMTQwODAzMDc1ODQ4WhcNMjQwODAyMDc1ODQ4WjAAMIIBIjANBgkqhkiG9w0B
     *          AQEFAAOCAQ8AMIIBCgKCAQEAzjfKA2qYuZjG6jr/q5WxyVUhXAmAmqmh/iRegwN4eFLksYGcCcMa
     *          9NZKi7gGvGHHEdmI2d+Wvt19DoKEENi93UVa+TwARl44bbu3sJZyw4Kq8dkScgM8SZ3TS/qhBX7D
     *          ritlzVj7FPk4SjrVrwVhr+Z7FCTn90/VB3EZtSL9Kl3rrX06QdB/3xE89E8wErKmXvFo/pUZ98tH
     *          pDyGEUrDRohjKVFBdHRIEVxh5jfTWQdFSlVygDhUHPFeGNYbevNpm9h9UVjsQWvzDRKeFVXHN6IE
     *          xULw5dON4qbmdtvcXp/KQmoJ+gRCyN2UPX3S5J3tMcehIbzOQrIz0VP8R6EMyQIDAQABoyIwIDAe
     *          BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQBaqfObosom
     *          Dp8VLG3wiKGNgW6rpst8blVARdhUwlBHqn61CqANJHvKOewhswPJYd3sBhKXGqALYtF8NyoRXCNc
     *          tIyFSxKzArJzk8JkfNNuhkRmwJcK672Moat91g3T9TzzzOqgWkNqSRb94KIwNB6/jDCkhSXluEXl
     *          oRqls23sWT7/ERSDAPSeag8MtE0JE8C1Y2vPCtOkhP4megGAwed1qzOmSkXpUuRIs3qqqRqakNWm
     *          EU2nmDBel3LqMDQ759x4+3E3T9c/6GCq8J2Sz7ZpqdcHXoT3uh4eE+3XJcKzVc+3JAa47xgj6VzN
     *          ilHaUrHFMDGtNp2corgaLDxQFT/d
     *          -----END CERTIFICATE-----
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAikCertificates client = new HostAikCertificates(getClientProperties());
     *   HostAikCertificateFilterCriteria criteria = new HostAikCertificateFilterCriteria();
     *   criteria.hostUuid = "d24dd52f-524e-43aa-8673-4013ecf64a4a";
     *   HostAikCertificateCollection searchHostAikCertificates = client.searchHostAikCertificates(criteria);
     * </pre>
     */    
    public HostAikCertificateCollection searchHostAikCertificates(HostAikCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", criteria.hostUuid);
        HostAikCertificateCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/aik-certificates", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAikCertificateCollection.class);
        return objCollection;
    }
    
    /**
     * Retrieves the AIK certificate details of the host with the specified UUID.
     * @param uuid - UUID of the Host for which the assoicated AIK certificate needs to be retrieved. 
     * @return HostAikCertificate retrieved from the system for the specified Host UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_aik_certificates:retrieve
     * @mtwContentTypeReturned application/json OR application/x-pem-file (just Certificate in the PEM format would be returned)
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/d24dd52f-524e-43aa-8673-4013ecf64a4a/aik-certificates
     * Output (ContentType- application/x-pem-file): -----BEGIN CERTIFICATE-----
     *          MIICvTCCAaWgAwIBAgIGAUea4MkvMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u
     *          LXBjYS1haWswHhcNMTQwODAzMDc1ODQ4WhcNMjQwODAyMDc1ODQ4WjAAMIIBIjANBgkqhkiG9w0B
     *          AQEFAAOCAQ8AMIIBCgKCAQEAzjfKA2qYuZjG6jr/q5WxyVUhXAmAmqmh/iRegwN4eFLksYGcCcMa
     *          9NZKi7gGvGHHEdmI2d+Wvt19DoKEENi93UVa+TwARl44bbu3sJZyw4Kq8dkScgM8SZ3TS/qhBX7D
     *          ritlzVj7FPk4SjrVrwVhr+Z7FCTn90/VB3EZtSL9Kl3rrX06QdB/3xE89E8wErKmXvFo/pUZ98tH
     *          pDyGEUrDRohjKVFBdHRIEVxh5jfTWQdFSlVygDhUHPFeGNYbevNpm9h9UVjsQWvzDRKeFVXHN6IE
     *          xULw5dON4qbmdtvcXp/KQmoJ+gRCyN2UPX3S5J3tMcehIbzOQrIz0VP8R6EMyQIDAQABoyIwIDAe
     *          BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQBaqfObosom
     *          Dp8VLG3wiKGNgW6rpst8blVARdhUwlBHqn61CqANJHvKOewhswPJYd3sBhKXGqALYtF8NyoRXCNc
     *          tIyFSxKzArJzk8JkfNNuhkRmwJcK672Moat91g3T9TzzzOqgWkNqSRb94KIwNB6/jDCkhSXluEXl
     *          oRqls23sWT7/ERSDAPSeag8MtE0JE8C1Y2vPCtOkhP4megGAwed1qzOmSkXpUuRIs3qqqRqakNWm
     *          EU2nmDBel3LqMDQ759x4+3E3T9c/6GCq8J2Sz7ZpqdcHXoT3uh4eE+3XJcKzVc+3JAa47xgj6VzN
     *          ilHaUrHFMDGtNp2corgaLDxQFT/d
     *          -----END CERTIFICATE-----
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAikCertificates client = new HostAikCertificates(getClientProperties());
     *   HostAikCertificate retrieveHostAikCertificate = client.retrieveHostAikCertificate("d24dd52f-524e-43aa-8673-4013ecf64a4a");
     * </pre>
    */    
    public HostAikCertificate retrieveHostAikCertificate(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", hostUuid);
        // We are passing the host UUID to "id" also even though it will not be used (without this framework treats this call as a 
        // search call instead of a retrieve call. Currently we support only one aik certificate for a host, we can retrieve
        // the aik certificate for the host uniquely with the host uuid itself.
        map.put("id", hostUuid); 
        HostAikCertificate obj = getTarget().path("hosts/{host_id}/aik-certificates/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAikCertificate.class);
        return obj;
    }
    
}
