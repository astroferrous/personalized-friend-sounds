package com.personalizedfriendsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("personalizedfriendsounds")
public interface PersonalizedFriendSoundsConfig extends Config
{
	@ConfigItem(
			keyName = "friends",
			name = "Friends List",
			description = "Play sounds when you see configured players on your friends list."
	)
	default boolean friendsList()
	{
		return true;
	}

	@ConfigItem(
			keyName = "clan",
			name = "Clan Members",
			description = "Play sounds when you see configured players in your clan."
	)
	default boolean clanMembers()
	{
		return true;
	}

	@ConfigItem(
			keyName = "volume",
			name = "Volume",
			description = "Volume of sounds generated from plugin."
	)
	default int pluginVolume()
	{
		return 100;
	}

	@ConfigItem(
			keyName = "userSoundMappings",
			name = "Username sound mappings",
			description = "One per line in the format username=soundfile.wav"
	)
	default String userSoundMappings()
	{
		return "";
	}

	@ConfigItem(
			keyName = "overheadText",
			name = "Overhead text",
			description = "Optional overhead text shown when a configured player is seen"
	)
	default String overheadText()
	{
		return "";
	}
}