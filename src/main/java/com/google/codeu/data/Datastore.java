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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

   /**
   * Converts list of strings to list of UUIDs.
   */
  public static List<UUID> convertStringsToUuids(List<String> uuidStrings) {
    if (uuidStrings == null) {
      return null;
    }
    return uuidStrings.stream().map(UUID::fromString).collect(Collectors.toList());
  }

  /**
   * Converts list of UUIDs to list of strings.
   */
  public static List<String> convertUuidsToStrings(List<UUID> uuids) {
    if (uuids == null) {
      return null;
    }
    return uuids.stream().map(UUID::toString).collect(Collectors.toList());
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("timestamp", message.getTimestamp());
    messageEntity.setProperty("commentIDsAsStrings", convertUuidsToStrings(message.getCommentIDs()));
    messageEntity.setProperty("likeEmails", message.getLikeEmails());

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
    userEntity.setProperty("avatar", user.getAvatar());
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
    String avatar = (String) userEntity.getProperty("avatar");
    User user = new User(email, nickname, activity, skillLevel, aboutMe);
    user.setAvatar(avatar);
    
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
    // I declare the list I will use to return the search results
    List<String> users = new ArrayList<>();
    
    // Create first query that will look for users by email
    Query queryByEmail = new Query("User")
      .setFilter(new Query.FilterPredicate("email", FilterOperator.EQUAL, searchInput));
    PreparedQuery result = datastore.prepare(queryByEmail);
    Entity userEntity = result.asSingleEntity();

    // If there was no user found with the email entered or the input was not an email
    if(userEntity == null) {
      // Create second query that will look for users by nickname, since there can be multiple users with same nicknames, I use a list
      Query queryByNickname = new Query("User")
      .setFilter(new Query.FilterPredicate("nickname", FilterOperator.EQUAL, searchInput));
      List<Entity> results2 = datastore.prepare(queryByNickname).asList(FetchOptions.Builder.withDefaults());;
      for (Entity userFound : results2) {
        String email = (String) userFound.getProperty("email");    
        users.add(email);
      }
      
      // If there was no users found either by email or nickname I now look for users by activity
      if (results2.isEmpty()) {
        Query queryByActivity = new Query("User")
          .setFilter(new Query.FilterPredicate("activity", FilterOperator.EQUAL, searchInput));
        List<Entity> results = datastore.prepare(queryByActivity).asList(FetchOptions.Builder.withDefaults());
        for (Entity userFound : results) {
          String email = (String) userFound.getProperty("email");    
          users.add(email);
        }
      }
      return users;
    }
    
    String email = (String) userEntity.getProperty("email");
    users.add(email);
    return users;
    
  }

   /*
    Function that returns a list of users that match the input by email, nickname or activity
  */
  public List<String> recommendUsers(String searchInput) {
    // I declare the list I will use to return the search results
    List<String> users = new ArrayList<>();
    // Find users by activity
    Query queryByActivity = new Query("User")
      .setFilter(new Query.FilterPredicate("activity", FilterOperator.EQUAL, searchInput));
    List<Entity> results = datastore.prepare(queryByActivity).asList(FetchOptions.Builder.withDefaults());
    for (Entity userFound : results) {      
      String email = (String) userFound.getProperty("email");    
      users.add(email);      
    }
    
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
  
  /** Fetches markers for user from Datastore. */
  public List<Marker> getUserMarkers(String user) {
	  List<Marker> markers = new ArrayList<>();
	  if(user.equals("null")) {
		  markers = getMarkers();
		  return markers;
	  }
	  
	  Query query = new Query("Marker");
	  PreparedQuery results = datastore.prepare(query);
	  for (Entity entity : results.asIterable()) {
  		if(entity.getProperty("userName").equals(user)) {
  		  double lat = (double) entity.getProperty("lat");
  	      double lng = (double) entity.getProperty("lng");
  	      String content = (String) entity.getProperty("content");
  	      String userName = (String) entity.getProperty("userName");
  	      String skillLevel = (String) entity.getProperty("skillLevel");

  	      Marker marker = new Marker(lat, lng, content, userName, skillLevel);
  	      markers.add(marker);
  		}
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
  
  /** Deletes all Markers */
  public void deleteMarkers() {
	  List<Marker> markers = new ArrayList<>();

	    Query query = new Query("Marker");
	    PreparedQuery results = datastore.prepare(query);

	    for (Entity entity : results.asIterable()) {
	    		Key key = entity.getKey();
	    		datastore.delete(key);
	    }
  }

  /**
   * @param messageId
   * @return Message corresponding to the given id
   */
  public Message getMessage(String messageId) throws Exception {
    Entity entity = datastore.get(KeyFactory.createKey("Message", messageId));
    return convertMessageFromEntity(entity);
  }

  public Message convertMessageFromEntity(Entity entity) throws Exception {
    String idString = entity.getKey().getName();
    UUID id = UUID.fromString(idString);
    String user = (String) entity.getProperty("user");
    long timestamp = (long) entity.getProperty("timestamp");
    String text = (String) entity.getProperty("text");
    Message message = new Message(id, user, text, timestamp);

    if (entity.hasProperty("commentIDsAsStrings")) {
      message.setCommentIDs(convertStringsToUuids(
              (List<String>) entity.getProperty("commentIDsAsStrings")
      ));
    }

    if (entity.hasProperty("likeEmails")) {
      message.setLikeEmails((List<String>) entity.getProperty("likeEmails"));
    }

    return message;
  }

  public void addComment(String messageID, Comment comment) {
    try {
      // Add new comment ID to message.
      Message message = getMessage(messageID);
      List<UUID> commentIDs = message.getCommentIDs();

      if (commentIDs == null) {
        commentIDs = new ArrayList<>();
      }
      commentIDs.add(comment.getId());
      message.setCommentIDs(commentIDs);
      storeMessage(message);

      // Store comment in Datastore.
      storeComment(comment);
    } catch (Exception e) {
      System.err.println("Error adding comment to message.");
      System.err.println(comment.toString());
      e.printStackTrace();
    }
  }

  /*
    Stores comment in datastore
  */
  public void storeComment(Comment comment) {
    Entity commentEntity = new Entity("Comment", comment.getId().toString());
    commentEntity.setProperty("user", comment.getUser());
    commentEntity.setProperty("text", comment.getText());
    commentEntity.setProperty("timestamp", comment.getTimestamp());

    datastore.put(commentEntity);
  }

  /**
   * Gets comments posted on a specific message.
   * @return list of comments on a message
   */
  public List<Comment> getCommentsForMessage(String messageId) {
    List<Comment> commentsForMessage = new ArrayList<>();

    try {
      Message message = getMessage(messageId);
      if (message.getCommentIDs() == null || message.getCommentIDs().isEmpty()) {
        return commentsForMessage;
      }

      List<Key> keysForComments = new ArrayList<>();
      for (String commentID: convertUuidsToStrings(message.getCommentIDs())) {
        keysForComments.add(KeyFactory.createKey("Comment", commentID));
      }

      Query query = new Query("Comment")
              .setFilter(new Query.FilterPredicate(
                      Entity.KEY_RESERVED_PROPERTY,
                      FilterOperator.IN,
                      keysForComments)
              );

      PreparedQuery results = datastore.prepare(query);
      commentsForMessage = convertCommentsFromQuery(results);
    } catch (Exception e) {
      System.err.println("Error getting comments for message.");
      e.printStackTrace();
    }

    return commentsForMessage;
  }

  /**
   * Convert query to comments.
   */
  public List<Comment> convertCommentsFromQuery(PreparedQuery results) {
    List<Comment> comments = new ArrayList<>();

    for (Entity entity: results.asIterable()) {
      try {
        Comment comment = convertCommentFromEntity(entity);
        comments.add(comment);
      } catch (Exception e) {
        System.err.println("Error reading comment.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return comments;
  }

  /**
   * Converts message entity to Comment.
   */
  public Comment convertCommentFromEntity(Entity entity) {
    String commentIDString = entity.getKey().getName();
    UUID commentID = UUID.fromString(commentIDString);
    String user = (String) entity.getProperty("user");
    long timestamp = (long) entity.getProperty("timestamp");
    String text = (String) entity.getProperty("text");

    Comment comment = new Comment(commentID, user, text, timestamp);

    return comment;
  }

  /**
   * Adds the email of the user who just liked the message.
   */
  public void addLikedUserEmailToMessage(String email, String messageID) {
    try {
      // Add ID of the user who just liked the message.
      Message message = getMessage(messageID);
      List<String> likeEmails = message.getLikeEmails();

      if (likeEmails == null) {
        likeEmails = new ArrayList<>();
      }

      message.setLikeEmails(toggleStringInList(likeEmails, email));
      storeMessage(message);

    } catch (Exception e) {
      System.err.println("Error adding user who liked to message.");
      e.printStackTrace();
    }
  }

  /**
   * Add or remove a string in a list.
   */
  public static List<String> toggleStringInList(List<String> list, String element) {
    if (list.contains(element)) {
      list.remove(element);
    } else {
      list.add(element);
    }
    return list;
  }


}
