package com.personalizedfriendsounds;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
public final class PersonalizedFriendSoundFileManager
{
	private static final String RESOURCE_PREFIX = "/sounds/";

	private PersonalizedFriendSoundFileManager()
	{

	}

	public static InputStream getSoundStream(String soundFile) throws FileNotFoundException
	{
		File externalFile = new File(getUserSoundDirectory(), soundFile);
		if (externalFile.exists() && externalFile.isFile())
		{
			log.debug("Loading external sound file: {}", externalFile.getAbsolutePath());
			return new FileInputStream(externalFile);
		}

		String resourcePath = RESOURCE_PREFIX + soundFile;
		InputStream stream = PersonalizedFriendSoundFileManager.class.getResourceAsStream(resourcePath);
		if (stream != null)
		{
			log.debug("Loading bundled sound resource: {}", resourcePath);
			return stream;
		}

		throw new FileNotFoundException(
				"Could not find sound '" + soundFile + "' in "
						+ getUserSoundDirectory().getAbsolutePath()
						+ " or bundled resources at " + resourcePath
		);
	}

	public static File getUserSoundDirectory()
	{
		File dir = new File(System.getProperty("user.home"), ".runelite/personalized-friend-sounds");
		if (!dir.exists() && !dir.mkdirs())
		{
			log.warn("Failed to create sound directory: {}", dir.getAbsolutePath());
		}
		return dir;
	}
}