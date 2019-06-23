package com.google.codeu.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.User;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Handles fetching and saving user data.
 */
@WebServlet("/nickname")
public class NicknameServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with the "about me" section for a particular user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    response.setContentType("text/html");

    String user = request.getParameter("user");

    if(user == null || user.equals("")) {
      // Request is invalid, return empty response
      return;
    }

    User userData = datastore.getUser(user);

    if(userData == null || userData.getNickname() == null) {
      return;
    }

    response.getOutputStream().println(userData.getNickname());
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();
    User user = datastore.getUser(userEmail);
    if (user == null) {
      String nickname = Jsoup.clean(request.getParameter("nickname"), Whitelist.none());
      String aboutme = null; 
      user = new User(userEmail, nickname, aboutme);
    }
    else {
      user.setNickname(Jsoup.clean(request.getParameter("nickname"), Whitelist.none()));
    }

    datastore.storeUser(user);
    response.sendRedirect("/user-page.html?user=" + userEmail);
  }
}