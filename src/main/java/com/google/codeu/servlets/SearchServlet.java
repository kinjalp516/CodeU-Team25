package com.google.codeu.servlets;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Datastore;
import com.google.gson.Gson;

/**
 * Handles fetching all users for the community page.
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    List<String> users = datastore.searchUsers(request.getParameter("search"));
    Gson gson = new Gson();
    String json = gson.toJson(users);
    response.getOutputStream().println(json);
  }
}