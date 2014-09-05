package org.Eaaa.MirrorApi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.ant.taskdefs.Sleep;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.Mirror.Timeline;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.Subscription;
import com.google.api.services.mirror.model.SubscriptionsListResponse;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.TimelineListResponse;
import com.google.api.services.mirror.model.UserAction;
import com.google.appengine.api.memcache.ErrorHandler;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.glassware.AuthUtil;

@SuppressWarnings("serial")
public class MirrorApiServlet extends HttpServlet
{

	private static String savedItem = "";

	Logger logger = Logger.getLogger("MyLogger");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		// get access to MirrorAPIT
		Mirror mirror = getMirror(req);

		// get access to the timeline
		Timeline timeline = mirror.timeline();

		List<TimelineItem> items = retrieveAllTimelineItems(mirror);
		boolean exists = false;

		for (int i = 0; i < items.size(); i++)
		{
			if (items.get(i).getSourceItemId() != "test")
			{
				exists = true;
			}
		}

		if (exists)
		{
			resp.setContentType("text/html; charset=utf-8");
			resp.getWriter()
					.println(
							"<html><head>"
									+ "<meta http-equiv=\"refresh\"content=\"3;url=/index.html\">"
									+ "</head>"
									+ "<body>A card already exists in your timeline.<br></body></html>");
		} else
		{
			logger.info("In doGet mirrorServlet");
			// print out results on the web browser
			resp.setContentType("text/html; charset=utf-8");
			resp.getWriter()
					.println(
							"<html><head>"
									+ "<meta http-equiv=\"refresh\"content=\"3;url=/index.html\">"
									+ "</head>"
									+ "<body>A card is inserted to your timeline.<br></body></html>");

			// html styled content
//			String html = "<article class=\"photo\">" 
////					+ "<img src=\"https://sixth-loader-676.Appspot.com/static/owm_01d.png\" width=\"100%\" height=\"100%\""
////					+ "<div class=\"photo-overlay\"></div>"
//					+ "<section><p class=\"text-auto-size\">"
//					+ "<em class\"yellow\" Hello World!></em><br>"
//					+ "Welcome to the <strong class=\"blue\">Mirror API test </strong> at Eaaa."
//					+ "</p></section></article>";
			
			String text = "Hello World, Welcome to \nMirror API Image Test";
			
			URL url = new URL("https://sixth-loader-676.Appspot.com/static/owm_01d.png");
			InputStreamContent attachment = new InputStreamContent("image/png", url.openStream());
			
			// create a timeline item (card)
			TimelineItem timelineitem = new TimelineItem()
					.setText(text)
//					.setHtml(html)
					.setDisplayTime(new DateTime(new Date()))
					.setNotification(new NotificationConfig().setLevel("DEFAULT"));

			// add menu items with built-in actions
			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			menuItemList.add(new MenuItem().setAction("READ_ALOUD"));
			// reads aloud the text of the timeline, unless it's html
			timelineitem.setSpeakableText("Welcome!"); // defines what is read
														// aloud
			menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED"));
			// pins the card on the left side of the start screen or unpins it
			// from there
			menuItemList.add(new MenuItem().setAction("DELETE"));
			// deletes the timeline

			// adding a custom menu item
			List<MenuValue> menuValues = new ArrayList<MenuValue>();
			menuValues.add(new MenuValue().setDisplayName("Change Text"));
			menuItemList.add(new MenuItem().setValues(menuValues)
					.setId("changeId").setAction("CUSTOM")
					.setPayload("UPDATE_TEXT"));

			timelineitem.setMenuItems(menuItemList);
			timelineitem.setSourceItemId("Test");
			Logger.getLogger("MyLogger").info(timelineitem.getSourceItemId());
			timelineitem.setIsDeleted(false);
			timelineitem.setIsPinned(true);
			saveTimelineItem(timelineitem);
			// insert the card into the timeline
			timeline.insert(timelineitem, attachment).execute();
		}
	}

	public static void saveTimelineItem(TimelineItem tli)
	{
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		Logger.getLogger("MyLogger").info(
				"timeline id " + tli.getSourceItemId());
		if (tli.getSourceItemId() != null)
		{
			savedItem = tli.getSourceItemId();
			byte[] value = savedItem.getBytes();
			syncCache.put(1, value);
		}
	}

	public static List<TimelineItem> retrieveAllTimelineItems(Mirror mirror)
	{
		List<TimelineItem> result = new ArrayList<TimelineItem>();
		try
		{
			Timeline.List request = mirror.timeline().list();

			do
			{
				request.setPinnedOnly(true);
				TimelineListResponse timelineItems = request.execute();
				if (timelineItems.getItems() != null
						&& timelineItems.getItems().size() > 0)
				{
					result.addAll(timelineItems.getItems());
					request.setPageToken(timelineItems.getNextPageToken());
				} else
				{
					break;
				}
			} while (request.getPageToken() != null
					&& request.getPageToken().length() > 0);
		} catch (IOException e)
		{
			Logger.getLogger("MyLogger").info("An error occurred " + e);
			return null;
		}
		for (int i = 0; i < result.size(); i++)
		{
			Logger.getLogger("MyLogger").info(
					"TimelineItemId " + result.get(i).getId());
		}
		return result;
	}

	public static String getSavedTimelineItem() throws IOException
	{
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers
				.getConsistentLogAndContinue(Level.INFO));
		byte[] value = (byte[]) syncCache.get(1);
		savedItem = new String(value);
		Logger.getLogger("MyLogger").info("timeline id " + savedItem);
		if (savedItem != null)
		{
			return savedItem;
		} else
		{
			return null;
		}
	}

	private Mirror getMirror(HttpServletRequest req) throws IOException
	{
		// get credential
		Credential credential = AuthUtil.getCredential(req);

		// build access to Mirror API
		return new Mirror.Builder(new UrlFetchTransport(),
				new JacksonFactory(), credential).setApplicationName(
				"Hello Glass!").build();
	}

}