
package gov.niarl.his.webservices.hisPrivacyCAWebService2.server.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

@XmlRootElement(name = "getHisPrivacyCAWebService2Response", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getHisPrivacyCAWebService2Response", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
public class GetHisPrivacyCAWebService2Response {

    @XmlElement(name = "return", namespace = "")
    private W3CEndpointReference _return;

    /**
     * 
     * @return
     *     returns W3CEndpointReference
     */
    public W3CEndpointReference getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(W3CEndpointReference _return) {
        this._return = _return;
    }

}
