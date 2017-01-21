package com.solambda.swiffer.api.model;

public class WorkflowTypeId {

	private DomainIdentifier domainId;
	private String name;
	private String version;

	public WorkflowTypeId(final DomainIdentifier domainId, final String name, final String version) {
		super();
		this.domainId = domainId;
		this.name = name;
		this.version = version;
	}

	public String name() {
		return name;
	}

	public String version() {
		return version;
	}

	public DomainIdentifier domainId() {
		return domainId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (domainId == null ? 0 : domainId.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (version == null ? 0 : version.hashCode());
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
		WorkflowTypeId other = (WorkflowTypeId) obj;
		if (domainId == null) {
			if (other.domainId != null) {
				return false;
			}
		} else if (!domainId.equals(other.domainId)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "WorkflowTypeId[domain=" + domainId + ", name=" + name + ", version=" + version + "]";
	}

}
