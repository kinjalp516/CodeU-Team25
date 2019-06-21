package com.google.codeu.data;

public class User {

  private String email;
  private String nickname;
  private String aboutMe;

  public User(String email, String nickname, String aboutMe) {
    this.email = email;
    this.nickname = nickname;
    this.aboutMe = aboutMe;
  }

  public String getEmail(){
    return email;
  }

  public String getNickname(){
    return nickname;
  }

  public String getAboutMe(){
    return aboutMe;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setAboutMe(String aboutMe) {
    this.aboutMe = aboutMe;
  }
}