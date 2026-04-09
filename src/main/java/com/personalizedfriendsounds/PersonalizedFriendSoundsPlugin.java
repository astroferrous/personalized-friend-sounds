package com.personalizedfriendsounds;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@PluginDescriptor(
		name = "Personalized Friend Sounds",
		description = "Play custom sounds and overhead text for nearby friends"
)
public class PersonalizedFriendSoundsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PersonalizedFriendSoundsConfig config;

	@Inject
	private PersonalizedFriendSoundEngine soundEngine;

	private final Set<String> seenPlayers = new HashSet<>();
	private int tickTimer = 5;

	@Override
	protected void startUp()
	{
		log.info("Personalized Friend Sounds started");
		log.info("Custom sound directory: {}", PersonalizedFriendSoundFileManager.getUserSoundDirectory().getAbsolutePath());
	}

	@Override
	protected void shutDown()
	{
		seenPlayers.clear();
		log.info("Personalized Friend Sounds stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.HOPPING
				|| event.getGameState() == GameState.LOGIN_SCREEN
				|| event.getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR)
		{
			seenPlayers.clear();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.LOGINLOGOUTNOTIFICATION)
		{
			String message = event.getMessage();
			if (message.contains(" has logged out."))
			{
				String name = normalizeName(message.replace(" has logged out.", ""));
				seenPlayers.remove(name);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (tickTimer > 0)
		{
			tickTimer--;
			return;
		}

		Map<String, PersonalizedFriendMapping> mappings = getMappings();
		if (mappings.isEmpty())
		{
			tickTimer = 5;
			return;
		}

		List<Player> players = client.getPlayers();
		if (players == null)
		{
			tickTimer = 5;
			return;
		}

		for (Player player : players)
		{
			if (player == null || player == client.getLocalPlayer() || player.getName() == null)
			{
				continue;
			}

			// 🔥 FRIENDS ONLY
			if (!player.isFriend())
			{
				continue;
			}

			String normalizedName = normalizeName(player.getName());

			if (seenPlayers.contains(normalizedName))
			{
				continue;
			}

			PersonalizedFriendMapping mapping = mappings.get(normalizedName);
			if (mapping == null)
			{
				continue;
			}

			seenPlayers.add(normalizedName);

			try
			{
				soundEngine.playClip(mapping.getSoundFile());
			}
			catch (Exception ex)
			{
				log.warn("Failed to play sound {} for player {}", mapping.getSoundFile(), player.getName(), ex);
			}

			// 🔥 per-user overhead text (fallback to global)
			String overhead = mapping.getOverheadText();

			if (overhead != null && !overhead.isBlank())
			{
				player.setOverheadText(overhead);
				player.setOverheadCycle(200);
			}
		}

		tickTimer = 5;
	}

	private Map<String, PersonalizedFriendMapping> getMappings()
	{
		Map<String, PersonalizedFriendMapping> mappings = new HashMap<>();
		String raw = config.userSoundMappings();

		if (raw == null || raw.isBlank())
		{
			return mappings;
		}

		for (String line : raw.split("\\r?\\n"))
		{
			String trimmed = line.trim();
			if (trimmed.isEmpty() || !trimmed.contains("="))
			{
				continue;
			}

			String[] parts = trimmed.split("=", 2);
			String username = normalizeName(parts[0]);
			String value = parts[1].trim();

			String soundFile;
			String overheadText = null;

			if (value.contains("|"))
			{
				String[] split = value.split("\\|", 2);
				soundFile = split[0].trim();
				overheadText = split[1].trim();
			}
			else
			{
				soundFile = value;
			}

			if (!username.isEmpty() && !soundFile.isEmpty())
			{
				mappings.put(username, new PersonalizedFriendMapping(soundFile, overheadText));
			}
		}

		return mappings;
	}

	private String normalizeName(String name)
	{
		if (name == null)
		{
			return "";
		}

		return name
				.replaceAll("\\P{Print}", " ")
				.replace('\u00A0', ' ')
				.trim()
				.toLowerCase();
	}

	@Provides
	PersonalizedFriendSoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PersonalizedFriendSoundsConfig.class);
	}
}