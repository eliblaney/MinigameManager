package minigamemanager.api.profile;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import minigamemanager.api.db.MySQL;
import minigamemanager.config.MinigameSettings;
import minigamemanager.core.MinigameManager;

public class ProfileDatabase implements Closeable {
	
	private final Object lock = new Object();
	private final Connection conn;
	private final Statement s;
	
	public ProfileDatabase(String ip, int port, String database) throws SQLException {
		this(ip, port, database, null, null);
	}
	
	public ProfileDatabase(String ip, int port, String database, String username, String password) throws SQLException {
		this.conn = new MySQL(ip, port, database).connect(username, password);
		this.s = conn.createStatement();
		create();
	}
	
	private void create() throws SQLException {
		MinigameSettings set = MinigameManager.getMinigameManager().getMinigameSettings();
		String sql = "CREATE TABLE IF NOT EXISTS " + set.mysqlTables().get("profiles") + " (uuid VARCHAR(36) NOT NULL, " + "elo BIGINT, " + "currency DECIMAL, " + "gamesPlayed BIGINT, PRIMARY KEY(UUID));";
		s.executeUpdate(sql);
	}
	
	public int saveProfile(PlayerProfile profile) throws SQLException {
		synchronized (lock) {
			ProfileData data = profile.getData();
			String sql = "INSERT INTO " + MinigameManager.getMinigameManager().getMinigameSettings().mysqlTables().get("profiles") + " (uuid, elo, currency, gamesPlayed) VALUES (\"" + profile.getUUID() + "\", " + data.getELO() + ", " + data.getCurrency() + ", " + data.getGamesPlayed() + ") ON DUPLICATE KEY UPDATE elo=" + data.getELO() + ", currency=" + data.getCurrency() + ", gamesPlayed=" + data.getGamesPlayed() + ";";
			return s.executeUpdate(sql);
		}
	}
	
	public PlayerProfile getProfile(UUID uuid) throws SQLException {
		synchronized (lock) {
			String sql = "SELECT elo, currency, gamesPlayed from " + MinigameManager.getMinigameManager().getMinigameSettings().mysqlTables().get("profiles") + " WHERE uuid=\"" + uuid + "\";";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				ProfileData data = new ProfileData();
				data.setELO(rs.getLong("elo"));
				data.setCurrency(rs.getDouble("currency"));
				data.setGamesPlayed(rs.getLong("gamesPlayed"));
				try {
					Class<PlayerProfile> clazz = PlayerProfile.class;
					Constructor<PlayerProfile> cons = clazz.getDeclaredConstructor(UUID.class, ProfileData.class);
					cons.setAccessible(true);
					return cons.newInstance(uuid, data);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			} else
				return null;
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
}
