package com.personalizedfriendsounds;

import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class PersonalizedFriendSoundFileManager
{
	public static InputStream getSoundStream(String soundFile) throws FileNotFoundException
	{
		InputStream stream = PersonalizedFriendSoundFileManager.class.getResourceAsStream("/" + soundFile);

		if (stream == null)
		{
			throw new FileNotFoundException("Could not find sound resource: " + soundFile);
		}

		return stream;
	}
}