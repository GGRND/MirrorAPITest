package org.Eaaa.MirrorApi;



import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.Mirror.Timeline;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.TimelineListResponse;
import com.google.glassware.AuthUtil;

@SuppressWarnings("serial")
public class ManageTimelineServlet extends HttpServlet {

	int update = 0;
	Logger logger = Logger.getLogger("MyLogger");
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.setContentType("text/html; charset=utf-8");
		resp.getWriter().println(
				"<html><head>" +
				"<meta http-equiv=\"refresh\"content=\"3;url=/index.html\">" +
				"</head>" +
				"<body>A card is updated in your timeline.<br></body></html>" );
		
		
		// get access to Mirror API
		Mirror mirror = getMirror(req);

		// get access to the timeline
		Timeline timeline = mirror.timeline();
		
		// create a timeline item (card)
		TimelineItem timelineitem = new TimelineItem()
			//.setText("Hello World!")
			.setDisplayTime(new DateTime(new Date()))
			.setNotification(new NotificationConfig().setLevel("DEFAULT"));
			
		// add menu items with built-in actions
		List<MenuItem> menuItemList = new ArrayList<MenuItem>();
		menuItemList.add(new MenuItem().setAction("READ_ALOUD")); // reads aloud the text of the timeline, unless it's html
		timelineitem.setSpeakableText("Welcome!"); // defines what is read aloud
		menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED")); // pins the card on the left side of the start screen or unpins it from there
		menuItemList.add(new MenuItem().setAction("DELETE")); // deletes the timeline
		
		// adding a custom menu item
		List<MenuValue> menuValues = new ArrayList<MenuValue>();
		menuValues.add(new MenuValue().setDisplayName("Change Text"));
		menuItemList.add(new MenuItem().setValues(menuValues).setId("changeId").setAction("CUSTOM").setPayload("UPDATE_TEXT"));
			
		timelineitem.setMenuItems(menuItemList);
		
		
		
		TimelineItem oldtli = null; 
		List<TimelineItem> items = MirrorApiServlet.retrieveAllTimelineItems(mirror);
		for(int i = 0; i < items.size(); i++)
		{
			if(items.get(i).getSourceItemId() != MirrorApiServlet.getSavedTimelineItem())
			{
				oldtli = items.get(i);
				break;
			}
		}
		if(oldtli != null)
		{	
			Logger.getLogger("MyLogger").info(oldtli.toString());
			Logger.getLogger("MyLogger").info("timelineitem is pinned " + oldtli.getIsPinned());
			Logger.getLogger("MyLogger").info("timelineitem is deleted " + oldtli.getIsDeleted());
			if(oldtli.getIsPinned())
			{
				Logger.getLogger("MyLogger").info("Inde i if sætningen");
				String html = 	"<article><section><p class=\"text-auto-size\">" +
						"<em class\"yellow\" Hello World! </em><br>" +
						"Welcome to the <strong class=\"blue\">Mirror API test " + (update+=2) + "</strong> at Eaaa." +
						"</p></section></article>";
				timelineitem.setHtml(html);
				timelineitem.setIsPinned(true);
				timeline.update(oldtli.getId(), timelineitem).execute();
			}
		}
		
	}



	private Mirror getMirror(HttpServletRequest req) throws IOException {
		// get credential
		Credential credential = AuthUtil.getCredential(req);

		// build access to Mirror API
		return new Mirror.Builder(new UrlFetchTransport(), new JacksonFactory(), credential).setApplicationName("Hello World").build();
	}

}
