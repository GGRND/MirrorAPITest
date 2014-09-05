package com.google.glassware;

import java.util.Arrays;
import java.util.List;

public class AuthSettings {
	public static String CLIENT_ID = "741868011361-213v6ra2sp5rhu9is1c6psd8v7ltnsd4.apps.googleusercontent.com";
	public static String CLIENT_SECRET = "yG3eg61WPXoKqpgy5_eN7hCR";
	
	public static final List<String> GLASS_SCOPE = 
			  Arrays.asList("https://www.googleapis.com/auth/glass.timeline",
					  	"https://www.googleapis.com/auth/glass.location",
					  	"https://www.googleapis.com/auth/userinfo.profile");
}
