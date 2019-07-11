package com.google.codeu.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.codeu.data.Marker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.User;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

@WebServlet("/markers")
public class MarkerServlet extends HttpServlet {

	private Datastore datastore;

	  @Override
  public void init() {
    datastore = new Datastore();
  }
	
  /** Responds with a JSON array containing marker data. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    response.setContentType("application/json");
	    
	    String user = request.getParameter("user");
	    
	    List<Marker> markers = new ArrayList<>();

	    markers = datastore.getUserMarkers(user);
	    //Marker m = new Marker(37.423829, -122.092154, user,"Google West Campus",
	    //        "Google West Campus is home to YouTube and Maps.");
	    //markers.add(m);
	    Gson gson = new Gson();
	    String json = gson.toJson(markers);

	    response.getOutputStream().println(json);
	  }

  /** Accepts a POST request containing a new marker. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    double lat = Double.parseDouble(request.getParameter("lat"));
    double lng = Double.parseDouble(request.getParameter("lng"));
    String content = Jsoup.clean(request.getParameter("content"), Whitelist.none());
    
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }
    
    String user = userService.getCurrentUser().getEmail(); //request.getParameter("user"); 
    User userData = datastore.getUser(user);
    String userName = userData.getEmail(); //if nickname not changed from email, gets error... why?
    String skillLevel = userData.getSkillLevel();
    content = userName + " : " + content;
    String url;
    if(skillLevel.contains("Beginner")){
      url = "http://maps.google.com/mapfiles/ms/icons/green-dot.png";
    }
    else if(skillLevel.contains("Intermediate")){
      url = "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";
    }
    else{
      url = "http://maps.google.com/mapfiles/ms/icons/orange-dot.png";
    }

    Marker marker = new Marker(lat, lng, content, userName, url);
    datastore.storeMarker(marker);
  }
  
  /* Delete previously saved markers.*/
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    datastore.deleteMarkers();
  }
}