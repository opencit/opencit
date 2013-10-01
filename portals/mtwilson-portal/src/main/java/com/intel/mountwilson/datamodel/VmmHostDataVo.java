/**
 * 
 */
package com.intel.mountwilson.datamodel;

import java.util.List;

/**
 * @author yuvrajsx
 *
 */
public class VmmHostDataVo {

		private String hostOS;
		private String hostVersion;
		private List<String> vmmNames;
		private String attestationType;
		/**
		 * @return the hostOS
		 */
		public String getHostOS() {
			return hostOS;
		}
		/**
		 * @return the hostVersion
		 */
		public String getHostVersion() {
			return hostVersion;
		}
		/**
		 * @param hostVersion the hostVersion to set
		 */
		public void setHostVersion(String hostVersion) {
			this.hostVersion = hostVersion;
		}
		/**
		 * @return the vmmNames
		 */
		public List<String> getVmmNames() {
			return vmmNames;
		}
		/**
		 * @return the isModuleBased
		 */
		public String getAttestationType() {
			return attestationType;
		}
		/**
		 * @param hostOS the hostOS to set
		 */
		public void setHostOS(String hostOS) {
			this.hostOS = hostOS;
		}
		/**
		 * @param vmmNames the vmmNames to set
		 */
		public void setVmmNames(List<String> vmmNames) {
			this.vmmNames = vmmNames;
		}
		/**
		 * @param isModuleBased the isModuleBased to set
		 */
		public void setAttestationType(String isModuleBased) {
			this.attestationType = isModuleBased;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "VmmHostDataVo [hostOS=" + hostOS + ", hostVersion="
					+ hostVersion + ", vmmNames=" + vmmNames
					+ ", attestationType=" + attestationType + "]";
		}
		
		
		
}
