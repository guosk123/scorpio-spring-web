package com.scorpio.rest.vo;

import java.io.Serializable;

public class ZoneVO implements Serializable{

	private static final long serialVersionUID = -89041730259989314L;

	private String name;
	private String key;
	private Integer capacity;
	private Integer saveDays;
	private String lowCapacityAlarm;
	private Double lowCapacityProportion;
	private String description;

	public ZoneVO(String name, String key, Integer capacity, Integer saveDays, String lowCapacityAlarm,
			Double lowCapacityProportion, String description) {
		super();
		this.name = name;
		this.key = key;
		this.capacity = capacity;
		this.saveDays = saveDays;
		this.lowCapacityAlarm = lowCapacityAlarm;
		this.lowCapacityProportion = lowCapacityProportion;
		this.description = description;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Integer getCapacity() {
		return capacity;
	}
	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}
	public Integer getSaveDays() {
		return saveDays;
	}
	public void setSaveDays(Integer saveDays) {
		this.saveDays = saveDays;
	}
	public String getLowCapacityAlarm() {
		return lowCapacityAlarm;
	}
	public void setLowCapacityAlarm(String lowCapacityAlarm) {
		this.lowCapacityAlarm = lowCapacityAlarm;
	}
	public Double getLowCapacityProportion() {
		return lowCapacityProportion;
	}
	public void setLowCapacityProportion(Double lowCapacityProportion) {
		this.lowCapacityProportion = lowCapacityProportion;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "ZoneVO [name=" + name + ", key=" + key + ", capacity=" + capacity + ", saveDays=" + saveDays
				+ ", lowCapacityAlarm=" + lowCapacityAlarm + ", lowCapacityProportion=" + lowCapacityProportion
				+ ", description=" + description + "]";
	}

	
}
