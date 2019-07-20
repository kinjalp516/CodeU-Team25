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

package com.google.codeu.servlets;

import org.jsoup.nodes.Document.OutputSettings;
import java.util.StringTokenizer;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/** Handles fetching and saving {@link Message} instances. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Message} data for a specific user. Responds with
   * an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    String user = request.getParameter("user");

    if (user == null || user.equals("")) {
      // Request is invalid, return empty array
      response.getWriter().println("[]");
      return;
    }

    List<Message> messages = datastore.getMessages(user);
    Gson gson = new Gson();
    String json = gson.toJson(messages);

    response.getWriter().println(json);
  }

  public boolean validImageVideoCheck (String messageURL) {
	  
	  //checks if link is video/image
	  if (!(messageURL.contains("https://www.youtube.com/") || messageURL.contains("https://youtu.be/") || messageURL.contains(".png") || messageURL.contains(".jpg") || messageURL.contains(".gif"))) {
		  	return false;
	  }
	  
	  //checks connectivity of link
	  try {
		    URL url = new URL(messageURL);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		    return true;
		} catch (MalformedURLException e) {
		    // the URL is not in a valid form
			return false;
		} catch (IOException e) {
		    // the connection couldn't be established
			return false;
		}
  }
  
  public String styledTextCheck (String line) {
	  
	  //builds AST
	  Parser parser = Parser.builder().build();
	  
	  //creates string HTML syntax
	  Node document = parser.parse(line);
	  HtmlRenderer renderer = HtmlRenderer.builder().build();
	  
	  //trim spaces + enclosing <p> tag
	  String word = renderer.render(document).trim();
	  word = word.substring(3, word.length() - 4);
	  
	  return word;  
  }
  
  /** Stores a new {@link Message}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	
	Message message; 
	
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String text = Jsoup.clean(request.getParameter("text"), "", Whitelist.none(), new OutputSettings().prettyPrint(false));
   
    String replaced = "";
    
    
 	//checked for styled text
    text = styledTextCheck (text);
    
    StringTokenizer st = new StringTokenizer(text, " \n", true);
    
    while (st.hasMoreTokens()) {
    	
    	String word = st.nextToken();
    	
    	//checks to see if token is valid + video/image
    	if (!validImageVideoCheck(word)) {
    		//continue
    	} else if (word.contains("https://www.youtube.com/") || word.contains("https://youtu.be/")) {
    	    //link is video, only works for youtube
    		String embed = word.substring(word.length() - 11, word.length());
    		word = "<iframe src= \"https://www.youtube.com/embed/" +  embed + "\"></iframe>";
    	} else if (word.contains(".png") || word.contains(".jpg") || word.contains(".gif")) {
        	//link is image
    		String replacement = "<img src=\"$1\" />";
    		String regex = "(https?://\\S+\\.(png|jpg|gif))";
    		word = word.replaceAll(regex, replacement);
    	}
	    	
    	//preserves new lines
    	if (word.equals("\n")) {
    		word = "<br>";
    	}
	  
    	replaced = replaced + word;
	}
    
    //if message isn't empty, post
    if (!replaced.equals("")) {
    	message = new Message(user, replaced);
    	datastore.storeMessage(message);
    }
    
    response.sendRedirect("/profile.html?user=" + user);
    
  }
}
