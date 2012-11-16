
package gov.niarl.his.webservices.hisPrivacyCAWebService2.server.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "identityRequestSubmitResponseResponse", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identityRequestSubmitResponseResponse", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
public class IdentityRequestSubmitResponseResponse {

    @XmlElement(name = "encryptedCertificate", namespace = "")
    private gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray encryptedCertificate;

    /**
     * 
     * @return
     *     returns ByteArray
     */
    public gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray getEncryptedCertificate() {
        return this.encryptedCertificate;
    }

    /**
     * 
     * @param encryptedCertificate
     *     the value for the encryptedCertificate property
     */
    public void setEncryptedCertificate(gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray encryptedCertificate) {
        this.encryptedCertificate = encryptedCertificate;
    }

}
