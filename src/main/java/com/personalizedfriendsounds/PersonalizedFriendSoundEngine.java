package com.personalizedfriendsounds;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Slf4j
@Singleton
public class PersonalizedFriendSoundEngine
{
    private final PersonalizedFriendSoundsConfig config;

    @Inject
    public PersonalizedFriendSoundEngine(PersonalizedFriendSoundsConfig config)
    {
        this.config = config;
    }

    public void playClip(String soundFile)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        File file = new File(soundFile);
        if (!file.exists())
        {
            throw new IOException("Sound file not found: " + soundFile);
        }

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file))
        {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            int volume = Math.max(0, Math.min(100, config.pluginVolume()));
            FloatControl gainControl = null;

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            {
                gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();

                float gain;
                if (volume == 0)
                {
                    gain = min;
                }
                else
                {
                    gain = min + (max - min) * (volume / 100f);
                }
                gainControl.setValue(gain);
            }

            clip.start();
        }
    }
}