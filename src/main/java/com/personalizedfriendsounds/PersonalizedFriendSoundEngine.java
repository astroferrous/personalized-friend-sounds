package com.personalizedfriendsounds;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

@Slf4j
@Singleton
public class PersonalizedFriendSoundEngine
{
    private final PersonalizedFriendSoundsConfig config;
    private final AudioPlayer audioPlayer;

    @Inject
    public PersonalizedFriendSoundEngine(
            PersonalizedFriendSoundsConfig config,
            AudioPlayer audioPlayer
    )
    {
        this.config = config;
        this.audioPlayer = audioPlayer;
    }

    public void playClip(String soundFile)
    {
        float gainDb = toGainDb(config.pluginVolume());

        try
        {
            PersonalizedFriendSoundFileManager.SoundHandle soundHandle =
                    PersonalizedFriendSoundFileManager.resolveSound(soundFile);

            if (soundHandle == null)
            {
                log.warn("Could not resolve sound '{}'", soundFile);
                return;
            }

            if (soundHandle.isExternalFile())
            {
                audioPlayer.play(soundHandle.getFile(), gainDb);
            }
            else
            {
                audioPlayer.play(
                        PersonalizedFriendSoundEngine.class,
                        soundHandle.getResourcePath(),
                        gainDb
                );
            }
        }
        catch (Exception e)
        {
            log.warn("Failed to play sound '{}'", soundFile, e);
        }
    }

    private float toGainDb(int volumePercent)
    {
        int clamped = Math.max(1, Math.min(100, volumePercent));
        float volume = clamped / 100f;
        return (float) Math.log10(volume) * 20f;
    }
}