package minigamemanager.config;

import java.io.File;

import org.apache.commons.lang.Validate;

import minigamemanager.api.config.CustomConfig;
import minigamemanager.core.MinigameManager;
import net.md_5.bungee.api.ChatColor;

/**
 * Class for getting the correct messages for languages
 * 
 * @author DonkeyCore
 */
public class MinigameMessages extends CustomConfig {
	
	/**
	 * Subfolder for languages to be saved in
	 */
	private static final String languagesSubFolder = "languages" + File.separator;
	/**
	 * Folder to save languages in
	 */
	private static final String languagesFolder = MinigameManager.getPlugin().getDataFolder() + File.separator + languagesSubFolder;
	
	/**
	 * Create a new instance of MinigameLocations
	 */
	public MinigameMessages() {
		super(MinigameManager.getPlugin(), languagesSubFolder, false, new File(languagesFolder), MinigameManager.getMinigameManager().getMinigameSettings().getLanguage() + ".yml");
	}
	
	/**
	 * Get the message for the specified key
	 * 
	 * @param type The type of message to get the message for
	 * 
	 * @return The message in the correct language
	 */
	public String getMessage(MessageType type) {
		Validate.notNull(type, "Key cannot be empty");
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString(type.toString()));
	}
	
}
