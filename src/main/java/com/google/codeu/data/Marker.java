package com.google.codeu.data;

public class Marker {

	  private double lat;
	  private double lng;
	  private String content;
	  private String userName;
	  private String skillLevel;

	  public Marker(double lat, double lng, String content, String userName,String skillLevel) {
	    this.lat = lat;
	    this.lng = lng;
	    this.content = content;
	    this.userName = userName;
	    this.skillLevel = skillLevel;
	    
	  }

	  public double getLat() {
	    return lat;
	  }

	  public double getLng() {
	    return lng;
	  }

	  public String getContent() {
	    return content;
	  }
	  
	  public String getUserName() {
	    return userName;
	  }
	  
	  public String getSkillLevel() {
		return skillLevel;
	  }
	}