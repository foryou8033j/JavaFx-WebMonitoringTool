package mil.navy.monitoring.util;

import java.util.prefs.Preferences;

import mil.navy.monitoring.MainApp;

public class Regedit 
{

	static Preferences userRootPrefs = Preferences.userNodeForPackage(MainApp.class);
	
	
	public static boolean isContains(String key)
	{
		return userRootPrefs.get(key, null) != null;
	}
	
	public static void addRegistry(String key, String value)
	{
		try
		{
			if(isContains(key))
				userRootPrefs.put(key, value);
			else
				userRootPrefs.put(key, value);
		}catch (Exception e)
		{
			
		}
	}
	
	public static String getRegistry(String key)
	{
		try
		{
			return userRootPrefs.get(key, "");
		}catch (Exception e)
		{
			return null;
		}
		
	}
	
	
}
