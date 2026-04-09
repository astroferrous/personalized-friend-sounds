package com.personalizedfriendsounds;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;

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

    public void playClip(String soundFile) throws Exception
    {
        try (InputStream rawStream = PersonalizedFriendSoundFileManager.getSoundStream(soundFile);
             BufferedInputStream bufferedStream = new BufferedInputStream(rawStream);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream))
        {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            applyVolume(clip);

            clip.start();

            new Thread(() ->
            {
                try
                {
                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                }
                catch (InterruptedException ignored) {}

                clip.close();
            }).start();
        }
    }

    private void applyVolume(Clip clip)
    {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
        {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        int volume = Math.max(0, Math.min(100, config.pluginVolume()));

        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        float gain = (volume == 0) ? min : (min + (max - min) * (volume / 100.0f));

        gainControl.setValue(gain);
    }
}