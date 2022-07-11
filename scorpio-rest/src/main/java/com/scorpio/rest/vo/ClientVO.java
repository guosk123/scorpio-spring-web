package com.scorpio.rest.vo;

import java.io.Serializable;
import java.util.List;

public class ClientVO implements Serializable{

	private static final long serialVersionUID = -8109117239471899537L;

	private String name;
	private String token;
	private String description;
	private List<Perm> perms;

	public ClientVO(String name, String token, String description, List<Perm> perms) {
		super();
		this.name = name;
		this.token = token;
		this.description = description;
		this.perms = perms;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Perm> getPerms() {
		return perms;
	}

	public void setPerms(List<Perm> perms) {
		this.perms = perms;
	}

	public static class Perm implements Serializable{

		private static final long serialVersionUID = 5562427494699879801L;

		private String zoneId;
		private Integer perm;
		
		public Perm(String zoneId, Integer perm) {
			super();
			this.zoneId = zoneId;
			this.perm = perm;
		}

		public String getZoneId() {
			return zoneId;
		}
		public void setZoneId(String zoneId) {
			this.zoneId = zoneId;
		}
		public Integer getPerm() {
			return perm;
		}
		public void setPerm(Integer perm) {
			this.perm = perm;
		}
		
		
	}
}
