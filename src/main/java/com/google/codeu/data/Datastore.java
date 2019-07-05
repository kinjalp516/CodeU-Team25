/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("timestamp", message.getTimestamp());

    datastore.put(messageEntity);
  }

  /**
   * Gets messages posted by a specific user.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessages(String user) {
    List<Message> messages = new ArrayList<>();

    Query query =
        new Query("Message")
            .setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
            .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");

        Message message = new Message(id, user, text, timestamp);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

   /** Stores the User in Datastore. */
  public void storeUser(User user) {
    Entity userEntity = new Entity("User", user.getEmail());
    userEntity.setProperty("email", user.getEmail());
    userEntity.setProperty("nickname", user.getNickname());
    userEntity.setProperty("activity", user.getActivity());
    userEntity.setProperty("skillLevel", user.getSkillLevel());
    userEntity.setProperty("aboutMe", user.getAboutMe());
    datastore.put(userEntity);
  }
  
  /**
  * Returns the User owned by the email address, or
  * null if no matching User was found.
  */
  public User getUser(String email) {

    Query query = new Query("User")
      .setFilter(new Query.FilterPredicate("email", FilterOperator.EQUAL, email));
    PreparedQuery results = datastore.prepare(query);
    Entity userEntity = results.asSingleEntity();
    if(userEntity == null) {
    return null;
    }
    
    String nickname = (String) userEntity.getProperty("nickname");
    String activity = (String) userEntity.getProperty("activity");
    String skillLevel = (String) userEntity.getProperty("skillLevel");
    String aboutMe = (String) userEntity.getProperty("aboutMe");
    User user = new User(email, nickname, activity, skillLevel, aboutMe);
    
    return user;
  }
  
  public List<Message> getAllMessages(){
	  List<Message> messages = new ArrayList<>();

	  Query query = new Query("Message")
	    .addSort("timestamp", SortDirection.DESCENDING);
	  PreparedQuery results = datastore.prepare(query);

	  for (Entity entity : results.asIterable()) {
	   try {
	    String idString = entity.getKey().getName();
	    UUID id = UUID.fromString(idString);
	    String user = (String) entity.getProperty("user");
	    String text = (String) entity.getProperty("text");
	    long timestamp = (long) entity.getProperty("timestamp");

	    Message message = new Message(id, user, text, timestamp);
	    messages.add(message);
	   } catch (Exception e) {
	    System.err.println("Error reading message.");
	    System.err.println(entity.toString());
	    e.printStackTrace();
	   }
	  }

	  return messages;
  }
  
  public Set<String> getUsers(){
	  Set<String> users = new HashSet<>();
	  Query query = new Query("Message");
	  PreparedQuery results = datastore.prepare(query);
	  for(Entity entity : results.asIterable()) {
	    users.add((String) entity.getProperty("user"));
	  }
	  return users;
	}

  /*
    Function that returns a list of users that match the input by email, nickname or activity
  */
  public List<String> searchUsers(String searchInput) {
    List<String> users = new ArrayList<>();
    Query queryByEmail = new Query("User")
      .setFilter(new Query.FilterPredicate("email", FilterOperator.EQUAL, searchInput));
    PreparedQuery result = datastore.prepare(queryByEmail);
    Entity userEntity = result.asSingleEntity();
    if(userEntity == null) {
      Query queryByNickname = new Query("User")
      .setFilter(new Query.FilterPredicate("nickname", FilterOperator.EQUAL, searchInput));
      result = datastore.prepare(queryByNickname);
      userEntity = result.asSingleEntity();
      if (userEntity == null) {
        Query queryByActivity = new Query("User")
          .setFilter(new Query.FilterPredicate("activity", FilterOperator.EQUAL, searchInput));
        List<Entity> results = datastore.prepare(queryByActivity).asList(FetchOptions.Builder.withDefaults());
        for (Entity userFound : results) {
          String email = (String) userFound.getProperty("email");    
          users.add(email);
        }
        return users;
      }
    }
    
    String email = (String) userEntity.getProperty("email");
    users.add(email);
    return users;
    
  }
  
  /** Fetches markers from Datastore. */
  public List<Marker> getMarkers() {
    List<Marker> markers = new ArrayList<>();

    Query query = new Query("Marker");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      double lat = (double) entity.getProperty("lat");
      double lng = (double) entity.getProperty("lng");
      String content = (String) entity.getProperty("content");
      String userName = (String) entity.getProperty("userName");
      String skillLevel = (String) entity.getProperty("skillLevel");

      Marker marker = new Marker(lat, lng, content, userName, skillLevel);
      markers.add(marker);
    }
    return markers;
  }

  /** Stores a marker in Datastore. */
  public void storeMarker(Marker marker) {
    Entity markerEntity = new Entity("Marker");
    markerEntity.setProperty("lat", marker.getLat());
    markerEntity.setProperty("lng", marker.getLng());
    markerEntity.setProperty("content", marker.getContent());
    markerEntity.setProperty("userName", marker.getUserName());
    markerEntity.setProperty("skillLevel", marker.getSkillLevel());

    datastore.put(markerEntity);
  }
  
  public void deleteMarkers() {
	  List<Marker> markers = new ArrayList<>();

	    Query query = new Query("Marker");
	    PreparedQuery results = datastore.prepare(query);

	    for (Entity entity : results.asIterable()) {
	    		Key key = entity.getKey();
	    		datastore.delete(key);
	    }
  }
}
