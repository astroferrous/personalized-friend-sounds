package com.personalizedfriendsounds;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PersonalizedFriendSoundsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PersonalizedFriendSoundsPlugin.class);
		RuneLite.main(args);
	}
}