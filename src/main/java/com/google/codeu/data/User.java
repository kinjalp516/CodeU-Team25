package com.google.codeu.data;

public class User {

  private String email;
  private String nickname;
  private String aboutMe;
  private String skillLevel;
  private String activity;

  public User(String email, String nickname, String activity, String skillLevel, String aboutMe) {
    this.email = email;
    this.nickname = nickname;
    this.activity = activity;
    this.skillLevel = skillLevel;
    this.aboutMe = aboutMe;
  }

  public String getEmail() {
    return email;
  }

  public String getNickname() {
    if (nickname == null) return email;
    return nickname;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public String getSkillLevel() {
    return skillLevel;
  }

  public String getActivity() {
    return activity;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setAboutMe(String aboutMe) {
    this.aboutMe = aboutMe;
  }

  public void setSkillLevel(String skillLevel) {
    this.skillLevel = skillLevel;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }
}