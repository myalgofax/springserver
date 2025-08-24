package com.myalgofax.constants;

public final class BrokersApiUrls {
	private BrokersApiUrls() {
		// Utility class - prevent instantiation
	}
	public static final String KOTAK_BASE_URL = "https://napi.kotaksecurities.com";
    public static final String KOTAK_OAUTH_TOKEN = KOTAK_BASE_URL + "/oauth2/token";

}
