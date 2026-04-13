package com.personalizedfriendsounds;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import net.runelite.client.RuneLite;

public final class PersonalizedFriendSoundFileManager
{
    private static final String PLUGIN_DIR_NAME = "personalized-friend-sounds";
    private static final Path SOUND_DIR = RuneLite.RUNELITE_DIR.toPath().resolve(PLUGIN_DIR_NAME);

    private PersonalizedFriendSoundFileManager()
    {

    }

    public static SoundHandle resolveSound(String soundFile)
    {
        if (soundFile == null)
        {
            return null;
        }

        String trimmed = soundFile.trim();
        if (trimmed.isEmpty())
        {
            return null;
        }

        ensureSoundDirectoryExists();

        File externalFile = SOUND_DIR.resolve(trimmed).toFile();
        if (externalFile.exists() && externalFile.isFile())
        {
            return SoundHandle.forExternalFile(externalFile);
        }

        if (resourceExists(trimmed))
        {
            return SoundHandle.forBundledResource(trimmed);
        }

        return null;
    }

    public static Path getSoundDirectory()
    {
        ensureSoundDirectoryExists();
        return SOUND_DIR;
    }

    private static void ensureSoundDirectoryExists()
    {
        try
        {
            Files.createDirectories(SOUND_DIR);
        }
        catch (Exception ignored)
        {

        }
    }

    private static boolean resourceExists(String resourceName)
    {
        return PersonalizedFriendSoundFileManager.class.getResourceAsStream(resourceName) != null;
    }

    @Getter
    public static class SoundHandle
    {
        private final File file;
        private final String resourcePath;

        private SoundHandle(File file, String resourcePath)
        {
            this.file = file;
            this.resourcePath = resourcePath;
        }

        public static SoundHandle forExternalFile(File file)
        {
            return new SoundHandle(file, null);
        }

        public static SoundHandle forBundledResource(String resourcePath)
        {
            return new SoundHandle(null, resourcePath);
        }

        public boolean isExternalFile()
        {
            return file != null;
        }
    }
}