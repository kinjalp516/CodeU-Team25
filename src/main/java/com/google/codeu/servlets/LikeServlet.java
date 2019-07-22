package com.google.codeu.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.gson.Gson;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles recording liked users for a message.
 */
@WebServlet("/like")
public class LikeServlet extends HttpServlet {

  private Datastore datastore;

  private class LikeMessageRequestBody {
    String userEmail;
    String messageID;

    public String getUserEmail() {
        return userEmail;
    }
    public String getMessageID() {
        return messageID;
    }
  }

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Stores a new user email to the likedUserEmail of the message.
   * Request body should contain userEmail and messageId.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LikeMessageRequestBody requestBody = new Gson().fromJson(
        request.getReader(),
        LikeMessageRequestBody.class
    );

    String userEmail = requestBody.getUserEmail();
    String messageID = requestBody.getMessageID();

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail()
        .equals(userEmail)) {
      response.sendRedirect("/index.html");
      return;
    }

    datastore.addLikedUserEmailToMessage(userEmail, messageID);
  }

}