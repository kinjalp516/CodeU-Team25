package com.google.codeu.data;

public class User {

  private String email;
  private String nickname;
  private String aboutMe;
  private String skillLevel;

  public User(String email, String nickname, String skillLevel, String aboutMe) {
    this.email = email;
    this.nickname = nickname;
    this.skillLevel = skillLevel;
    this.aboutMe = aboutMe;
  }

  public String getEmail() {
    return email;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public String getSkillLevel() {
    return skillLevel;
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
}