package com.solambda.swiffer.api.internal;

import com.solambda.swiffer.api.internal.utils.SWFUtils;

public class VersionedName {

	private String name;
	private String version;

	public VersionedName(final String name, final String version) {
		super();
		this.name = SWFUtils.checkId(name);
		this.version = SWFUtils.checkVersion(version);
	}

	public String name() {
		return this.name;
	}

	public String version() {
		return this.version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		result = prime * result + (this.version == null ? 0 : this.version.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final VersionedName other = (VersionedName) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!this.version.equals(other.version)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "['" + this.name + "','" + this.version + "']";
	}

}
