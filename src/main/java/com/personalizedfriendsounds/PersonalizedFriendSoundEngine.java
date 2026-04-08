package com.personalizedfriendsounds;

import javax.inject.Singleton;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Singleton
public class PersonalizedFriendSoundEngine
{
    public void playClip(String soundFile)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        String resourcePath = "/sounds/" + soundFile;

        try (InputStream rawStream = getClass().getResourceAsStream(resourcePath))
        {
            if (rawStream == null)
            {
                throw new IOException("Sound resource not found: " + resourcePath);
            }

            try (BufferedInputStream bufferedStream = new BufferedInputStream(rawStream);
                 AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream))
            {
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            }
        }
    }
}