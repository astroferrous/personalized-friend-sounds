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
		description = "Play custom sounds for specific nearby players"
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

		if (!(config.friendsList() || config.clanMembers()))
		{
			tickTimer = 5;
			return;
		}

		Map<String, String> mappings = getMappings();
		List<Player> players = client.getPlayers();

		for (Player player : players)
		{
			if (player == null || player == client.getLocalPlayer() || player.getName() == null)
			{
				continue;
			}

			boolean validPlayer =
					(player.isFriend() && config.friendsList())
							|| (player.isClanMember() && config.clanMembers());

			if (!validPlayer)
			{
				continue;
			}

			String normalizedName = normalizeName(player.getName());

			if (seenPlayers.contains(normalizedName))
			{
				continue;
			}

			String soundFile = mappings.get(normalizedName);
			if (soundFile == null || soundFile.isBlank())
			{
				continue;
			}

			seenPlayers.add(normalizedName);

			try
			{
				soundEngine.playClip(soundFile);
			}
			catch (Exception ex)
			{
				log.warn("Failed to play sound {} for player {}", soundFile, player.getName(), ex);
			}

			String overhead = config.overheadText();
			if (overhead != null && !overhead.isBlank())
			{
				client.getLocalPlayer().setOverheadText(overhead);
				client.getLocalPlayer().setOverheadCycle(200);

				player.setOverheadText(overhead);
				player.setOverheadCycle(200);
			}
		}

		tickTimer = 5;
	}

	private Map<String, String> getMappings()
	{
		Map<String, String> mappings = new HashMap<>();
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
			String soundFile = parts[1].trim();

			if (!username.isEmpty() && !soundFile.isEmpty())
			{
				mappings.put(username, soundFile);
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