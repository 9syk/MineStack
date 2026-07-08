package link.syk9.mineStack.storage;

import link.syk9.mineStack.config.PluginConfig;
import link.syk9.mineStack.model.AutoStoreMode;
import link.syk9.mineStack.model.PlayerStore;
import link.syk9.mineStack.model.SortMode;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public final class StorageRepository {
    private static final DateTimeFormatter BACKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final File databaseFile;
    private final File backupDirectory;
    private Connection connection;

    public StorageRepository(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.databaseFile = new File(plugin.getDataFolder(), "minestack.db");
        this.backupDirectory = new File(plugin.getDataFolder(), "backups");
    }

    public void init() {
        try {
            if (!plugin.getDataFolder().isDirectory() && !plugin.getDataFolder().mkdirs()) {
                throw new IOException("Could not create plugin data folder.");
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("PRAGMA journal_mode=WAL");
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS player_settings (
                            uuid TEXT PRIMARY KEY,
                            auto_store_mode TEXT NOT NULL,
                            sort_mode TEXT NOT NULL,
                            signature TEXT NOT NULL
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS player_items (
                            uuid TEXT NOT NULL,
                            material TEXT NOT NULL,
                            amount TEXT NOT NULL,
                            signature TEXT NOT NULL,
                            PRIMARY KEY (uuid, material)
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS player_recent (
                            uuid TEXT NOT NULL,
                            material TEXT NOT NULL,
                            position INTEGER NOT NULL,
                            PRIMARY KEY (uuid, material)
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS shared_items (
                            material TEXT PRIMARY KEY,
                            amount TEXT NOT NULL,
                            signature TEXT NOT NULL
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS shared_recent (
                            material TEXT PRIMARY KEY,
                            position INTEGER NOT NULL
                        )
                        """);
            }
        } catch (IOException | SQLException exception) {
            plugin.getLogger().severe("Failed to initialize SQLite storage: " + exception.getMessage());
        }
    }

    public void load(Map<UUID, PlayerStore> stores, PlayerStore sharedStore) {
        try {
            loadShared(sharedStore);
            loadPlayers(stores);
        } catch (SQLException | GeneralSecurityException exception) {
            plugin.getLogger().severe("Failed to load SQLite storage: " + exception.getMessage());
        }
    }

    public void saveAll(Map<UUID, PlayerStore> stores, PlayerStore sharedStore) {
        saveShared(sharedStore);
        for (Map.Entry<UUID, PlayerStore> entry : stores.entrySet()) {
            savePlayer(entry.getKey(), entry.getValue());
        }
    }

    public void savePlayer(UUID uuid, PlayerStore store) {
        try {
            savePlayerInternal(uuid, store);
        } catch (SQLException | GeneralSecurityException exception) {
            plugin.getLogger().severe("Failed to save MineStack player storage: " + exception.getMessage());
        }
    }

    public void saveShared(PlayerStore sharedStore) {
        try {
            saveSharedInternal(sharedStore);
        } catch (SQLException | GeneralSecurityException exception) {
            plugin.getLogger().severe("Failed to save MineStack shared storage: " + exception.getMessage());
        }
    }

    public void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to close SQLite storage: " + exception.getMessage());
        }
    }

    public void createBackup() {
        if (!databaseFile.isFile()) {
            return;
        }
        try {
            if (!backupDirectory.isDirectory() && !backupDirectory.mkdirs()) {
                throw new IOException("Could not create backup folder.");
            }
            File backup = new File(backupDirectory, "minestack-" + BACKUP_TIME_FORMAT.format(LocalDateTime.now()) + ".db");
            Files.deleteIfExists(backup.toPath());
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("VACUUM INTO '" + backup.getAbsolutePath().replace("'", "''") + "'");
            }
            trimBackups();
        } catch (IOException | SQLException exception) {
            plugin.getLogger().severe("Failed to create SQLite data backup: " + exception.getMessage());
        }
    }

    private void loadPlayers(Map<UUID, PlayerStore> stores) throws SQLException, GeneralSecurityException {
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT uuid, auto_store_mode, sort_mode, signature FROM player_settings")) {
            while (results.next()) {
                String uuidString = results.getString("uuid");
                String autoStoreMode = results.getString("auto_store_mode");
                String sortMode = results.getString("sort_mode");
                String signature = results.getString("signature");
                if (!verifySignature(signature, "settings", uuidString, autoStoreMode, sortMode)) {
                    plugin.getLogger().warning("Skipped tampered MineStack settings for " + uuidString);
                    continue;
                }
                PlayerStore store = new PlayerStore();
                store.setAutoStoreMode(AutoStoreMode.fromName(autoStoreMode));
                store.setSortMode(SortMode.fromName(sortMode));
                stores.put(UUID.fromString(uuidString), store);
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT uuid, material, amount, signature FROM player_items")) {
            while (results.next()) {
                String uuidString = results.getString("uuid");
                String materialName = results.getString("material");
                String amount = results.getString("amount");
                String signature = results.getString("signature");
                if (!verifySignature(signature, "player_item", uuidString, materialName, amount)) {
                    plugin.getLogger().warning("Skipped tampered MineStack item row for " + uuidString + " / " + materialName);
                    continue;
                }
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    stores.computeIfAbsent(UUID.fromString(uuidString), uuid -> new PlayerStore())
                            .put(material, new BigInteger(amount));
                }
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT uuid, material FROM player_recent ORDER BY uuid, position")) {
            while (results.next()) {
                Material material = Material.matchMaterial(results.getString("material"));
                if (material != null) {
                    stores.computeIfAbsent(UUID.fromString(results.getString("uuid")), uuid -> new PlayerStore())
                            .recent().add(material);
                }
            }
        }
    }

    private void loadShared(PlayerStore sharedStore) throws SQLException, GeneralSecurityException {
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT material, amount, signature FROM shared_items")) {
            while (results.next()) {
                String materialName = results.getString("material");
                String amount = results.getString("amount");
                String signature = results.getString("signature");
                if (!verifySignature(signature, "shared_item", materialName, amount)) {
                    plugin.getLogger().warning("Skipped tampered shared MineStack item row for " + materialName);
                    continue;
                }
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    sharedStore.put(material, new BigInteger(amount));
                }
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT material FROM shared_recent ORDER BY position")) {
            while (results.next()) {
                Material material = Material.matchMaterial(results.getString("material"));
                if (material != null) {
                    sharedStore.recent().add(material);
                }
            }
        }
    }

    private void savePlayerInternal(UUID uuid, PlayerStore store) throws SQLException, GeneralSecurityException {
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO player_settings(uuid, auto_store_mode, sort_mode, signature)
                    VALUES(?, ?, ?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET
                        auto_store_mode = excluded.auto_store_mode,
                        sort_mode = excluded.sort_mode,
                        signature = excluded.signature
                    """)) {
                String uuidString = uuid.toString();
                statement.setString(1, uuidString);
                statement.setString(2, store.autoStoreMode().name());
                statement.setString(3, store.sortMode().name());
                statement.setString(4, signature("settings", uuidString, store.autoStoreMode().name(), store.sortMode().name()));
                statement.executeUpdate();
            }
            try (PreparedStatement deleteItems = connection.prepareStatement("DELETE FROM player_items WHERE uuid = ?");
                 PreparedStatement deleteRecent = connection.prepareStatement("DELETE FROM player_recent WHERE uuid = ?")) {
                deleteItems.setString(1, uuid.toString());
                deleteItems.executeUpdate();
                deleteRecent.setString(1, uuid.toString());
                deleteRecent.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO player_items(uuid, material, amount, signature) VALUES(?, ?, ?, ?)
                    """)) {
                for (Map.Entry<Material, BigInteger> item : store.items().entrySet()) {
                    if (item.getValue().signum() <= 0) {
                        continue;
                    }
                    String material = item.getKey().name();
                    String amount = item.getValue().toString();
                    statement.setString(1, uuid.toString());
                    statement.setString(2, material);
                    statement.setString(3, amount);
                    statement.setString(4, signature("player_item", uuid.toString(), material, amount));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            saveRecent(uuid.toString(), store);
            connection.commit();
        } catch (SQLException | GeneralSecurityException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void saveSharedInternal(PlayerStore sharedStore) throws SQLException, GeneralSecurityException {
        connection.setAutoCommit(false);
        try {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM shared_items");
                statement.executeUpdate("DELETE FROM shared_recent");
            }
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO shared_items(material, amount, signature) VALUES(?, ?, ?)
                    """)) {
                for (Map.Entry<Material, BigInteger> item : sharedStore.items().entrySet()) {
                    if (item.getValue().signum() <= 0) {
                        continue;
                    }
                    String material = item.getKey().name();
                    String amount = item.getValue().toString();
                    statement.setString(1, material);
                    statement.setString(2, amount);
                    statement.setString(3, signature("shared_item", material, amount));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            saveSharedRecent(sharedStore);
            connection.commit();
        } catch (SQLException | GeneralSecurityException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void saveRecent(String uuid, PlayerStore store) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO player_recent(uuid, material, position) VALUES(?, ?, ?)")) {
            int position = 0;
            for (Material material : store.recent()) {
                statement.setString(1, uuid);
                statement.setString(2, material.name());
                statement.setInt(3, position++);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveSharedRecent(PlayerStore store) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO shared_recent(material, position) VALUES(?, ?)")) {
            int position = 0;
            for (Material material : store.recent()) {
                statement.setString(1, material.name());
                statement.setInt(2, position++);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private String signature(String... values) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(hmacKey());
        for (String value : values) {
            mac.update(value.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
        }
        return Base64.getEncoder().encodeToString(mac.doFinal());
    }

    private boolean verifySignature(String actual, String... values) throws GeneralSecurityException {
        return MessageDigest.isEqual(signature(values).getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKeySpec hmacKey() throws GeneralSecurityException {
        int[] parts = {77, 105, 110, 101, 83, 116, 97, 99, 107, 124, 115, 113, 108, 105, 116, 101, 124, 104, 109, 97, 99, 124, 50, 48, 50, 54};
        StringBuilder builder = new StringBuilder(parts.length);
        for (int part : parts) {
            builder.append((char) part);
        }
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(builder.toString().getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(digest, "HmacSHA256");
    }

    private void trimBackups() throws IOException {
        File[] backups = backupDirectory.listFiles((dir, name) -> name.startsWith("minestack-") && name.endsWith(".db"));
        if (backups == null || backups.length <= config.backupRetention()) {
            return;
        }
        Arrays.sort(backups, Comparator.comparing(File::getName).reversed());
        for (int i = config.backupRetention(); i < backups.length; i++) {
            Files.deleteIfExists(backups[i].toPath());
        }
    }
}
