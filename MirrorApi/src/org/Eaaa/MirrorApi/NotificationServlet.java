package org.Eaaa.MirrorApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.Mirror.Timeline;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.UserAction;
import com.google.glassware.AuthUtil;



@SuppressWarnings("serial")
public class NotificationServlet extends HttpServlet {
	
	Logger logger = Logger.getLogger("MyLogger");
	
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		logger.info("In the doPost");
		// Respond with OK and status 200 to prevent redelivery
		resp.setContentType("text/html");
		Writer writer = resp.getWriter();
		writer.append("OK");
		writer.close(); 
		
		// Get the notification object from the request body (into a string so we
	    // can log it)
	    BufferedReader notificationReader =
	        new BufferedReader(new InputStreamReader(req.getInputStream()));
	    String notificationString = "";

	    // Count the lines as a very basic way to prevent Denial of Service attacks
	    int lines = 0;
	    while (notificationReader.ready()) {
	      notificationString += notificationReader.readLine();
	      lines++;

	      // No notification would ever be this long. Something is very wrong.
	      if (lines > 1000) {
	        throw new IOException("Attempted to parse notification payload that was unexpectedly long.");
	      }
	    }
	    
	    logger.info("got raw notification " + notificationString);

	    JsonFactory jsonFactory = new JacksonFactory();

	    // If logging the payload is not as important, use
	    // jacksonFactory.fromInputStream instead.
	    Notification notification = jsonFactory.fromString(notificationString, Notification.class);

	    logger.info("Got a notification with ID: " + notification.getItemId());
		
		// Figure out the impacted user and get their credentials for API calls
		String userId = notification.getUserToken();
		Credential credential = AuthUtil.getCredential(userId);
		Mirror mirror = new Mirror.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
			.setApplicationName("Hello Glass!").build();
		
		if(notification.getCollection().equals("timeline"))
		{
			logger.info("In the collection=timeline if");
			// get the timeline item which triggered notification
			TimelineItem notifiedItem = mirror.timeline().get(notification.getItemId()).execute();
			if(notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload("changeId")))
			{
				
				logger.info("In the getUserActions if");
				// html styled content
				String html = 	"<article><section><p class=\"text-auto-size\">" +
								"<em class\"yellow\" Hello World! </em><br>" +
								"Welcome to the <strong class=\"blue\">Mirror API test opdateret igen </strong> at Eaaa." +
								"</p></section></article>";
				
				Timeline timeline = mirror.timeline();
//				
//				// create a timeline item (card)
//				TimelineItem timelineitem = new TimelineItem()
//					//.setText("Hello World!")
//					.setHtml(html).setDisplayTime(new DateTime(new Date()))
//					.setNotification(new NotificationConfig().setLevel("DEFAULT"));
//				
//				
//				// add menu items with built-in actions
//				List<MenuItem> menuItemList = new ArrayList<MenuItem>();
//				menuItemList.add(new MenuItem().setAction("READ_ALOUD")); // reads aloud the text of the timeline, unless it's html
//				notifiedItem.setSpeakableText("Welcome!"); // defines what is read aloud
//				menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED")); // pins the card on the left side of the start screen or unpins it from there
//				menuItemList.add(new MenuItem().setAction("DELETE")); // deletes the timeline
				
				/*
				// adding a custom menu item
				MenuItem customItem = new MenuItem();
				customItem.setId("change");
				customItem.setAction("CUSTOM");
				
				MenuValue menuValue = new MenuValue();
				menuValue.setDisplayName("Change Text");
				menuValue.setIconUrl("");
				
				customItem.setValues(Arrays.asList(menuValue));
				timelineitem.setMenuItems(Arrays.asList(customItem));
				
				menuItemList.add(customItem);
				*/
				
//				timelineitem.setMenuItems(menuItemList);
//				
				//timeline.insert(notifiedItem, attachment).execute();
				notifiedItem.setHtml(html).setDisplayTime(new DateTime(new Date())).setNotification(new NotificationConfig().setLevel("DEFAULT"));
				timeline.update(notifiedItem.getId(), notifiedItem).execute();
			} 
		}
	}
}


