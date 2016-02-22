package me.donkeycore.minigamemanager.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Class for getting the correct messages for languages
 * 
 * @author DonkeyCore
 */
public class MinigameMessages {
	
	/**
	 * The FileConfiguration instance that can be manipulated
	 */
	private FileConfiguration config;
	/**
	 * The file that the config is saved to
	 */
	private File configFile;
	/**
	 * The language to use
	 */
	private final String lang = MinigameManager.getMinigameManager().getMinigameSettings().getLanguage();
	/**
	 * Subfolder for languages to be saved in
	 */
	private final String languagesSubFolder = "languages/";
	/**
	 * Folder to save languages in
	 */
	private final String languagesFolder = MinigameManager.getPlugin().getDataFolder() + File.separator + languagesSubFolder;
	
	/**
	 * Create a new instance of MinigameLocations
	 */
	public MinigameMessages() {
		saveDefaultConfig();
		reloadConfig();
	}
	
	/**
	 * Reload the locations.yml configuration file
	 */
	public void reloadConfig() {
		if (configFile == null)
			configFile = new File(languagesFolder, lang + ".yml");
		config = YamlConfiguration.loadConfiguration(new File(languagesFolder + configFile));
		Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(MinigameManager.getPlugin().getResource(languagesSubFolder + lang + ".yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/**
	 * Get the instance of FileConfiguration that handles the config
	 * 
	 * @return An instance of FileConfiguration representing the config
	 */
	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();
		return config;
	}
	
	/**
	 * Save any changes to the config to disk
	 */
	public void saveConfig() {
		if (config == null || configFile == null)
			return;
		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			MinigameManager.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
		}
	}
	
	/**
	 * Save the default config if the config file does not exist
	 */
	public void saveDefaultConfig() {
		if (configFile == null) {
			new File(languagesFolder).mkdirs();
			configFile = new File(languagesFolder, lang + ".yml");
		}
		if (!configFile.exists())
			MinigameManager.getPlugin().saveResource(languagesSubFolder + lang + ".yml", false);
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
		return getConfig().getString(type.toString());
	}
	
}
