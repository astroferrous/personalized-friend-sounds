package com.personalizedfriendsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("personalizedfriendsounds")
public interface PersonalizedFriendSoundsConfig extends Config
{
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
			description = "Format: username=sound.wav|optional text"
	)
	default String userSoundMappings()
	{
		return "";
	}

}