package com.intel.mountwilson.datamodel;



/**
*
* @author Yuvraj Singh
*/

public class MleDetailsEntityVO {
	
	
	private Integer mleId;
    private String mleName;
	private String mleVersion;
	private String attestationType;
	private String mleType;
	private String manifestList;
	private String mleDescription;
	private String osName;
	private String osVersion;
	private String oemName;

	/**
	 * @return the mleId
	 */
	public Integer getMleId() {
		return mleId;
	}

	/**
	 * @return the mleName
	 */
	public String getMleName() {
		return mleName;
	}

	/**
	 * @return the mleVersion
	 */
	public String getMleVersion() {
		return mleVersion;
	}

	/**
	 * @return the attestationType
	 */
	public String getAttestationType() {
		return attestationType;
	}

	/**
	 * @return the mleType
	 */
	public String getMleType() {
		return mleType;
	}

	/**
	 * @return the manifestList
	 */
	public String getManifestList() {
		return manifestList;
	}

	/**
	 * @return the mleDescription
	 */
	public String getMleDescription() {
		return mleDescription;
	}

	/**
	 * @return the osName
	 */
	public String getOsName() {
		return osName;
	}

	/**
	 * @return the osVersion
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 * @return the oemName
	 */
	public String getOemName() {
		return oemName;
	}

	/**
	 * @param mleId the mleId to set
	 */
	public void setMleId(Integer mleId) {
		this.mleId = mleId;
	}

	/**
	 * @param mleName the mleName to set
	 */
	public void setMleName(String mleName) {
		this.mleName = mleName;
	}

	/**
	 * @param mleVersion the mleVersion to set
	 */
	public void setMleVersion(String mleVersion) {
		this.mleVersion = mleVersion;
	}

	/**
	 * @param attestationType the attestationType to set
	 */
	public void setAttestationType(String attestationType) {
		this.attestationType = attestationType;
	}

	/**
	 * @param mleType the mleType to set
	 */
	public void setMleType(String mleType) {
		this.mleType = mleType;
	}

	/**
	 * @param manifestList the manifestList to set
	 */
	public void setManifestList(String manifestList) {
		this.manifestList = manifestList;
	}

	/**
	 * @param mleDescription the mleDescription to set
	 */
	public void setMleDescription(String mleDescription) {
		this.mleDescription = mleDescription;
	}

	/**
	 * @param osName the osName to set
	 */
	public void setOsName(String osName) {
		this.osName = osName;
	}

	/**
	 * @param osVersion the osVersion to set
	 */
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	/**
	 * @param oemName the oemName to set
	 */
	public void setOemName(String oemName) {
		this.oemName = oemName;
	}

	
	@Override
	public String toString() {
		return "MleDetailsEntityVO [mleId=" + mleId + ", mleName=" + mleName
				+ ", mleVersion=" + mleVersion + ", attestationType="
				+ attestationType + ", mleType=" + mleType + ", manifestList="
				+ manifestList + ", mleDescription=" + mleDescription
				+ ", osName=" + osName + ", osVersion=" + osVersion
				+ ", oemName=" + oemName + "]";
	}
	
	
}
