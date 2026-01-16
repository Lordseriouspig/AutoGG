package top.einsluca.autogg.server;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import top.einsluca.autogg.AutoGGAddon;

public class ServerRegistry {

    private static final String DEFAULT_REMOTE_URL = "https://raw.githubusercontent.com/EinsLucaaa/AutoGG/refs/heads/master/core/src/main/resources/assets/auto-gg/servers.json";

    private static final String BUNDLED_RESOURCE_PATH = "/assets/auto-gg/servers.json";

    private final List<ServerConfiguration> servers = new ArrayList<>();

    public ServerRegistry(AutoGGAddon addon) {
        String remoteUrl = DEFAULT_REMOTE_URL;

        Optional<List<ServerEntry>> remote = Optional.empty();
        try {
            remote = loadFromUrl(remoteUrl);
        } catch (Exception e) {
            addon.logger().warn("[ServerRegistry] Failed to load remote servers.json from {}: {}", remoteUrl, e.getMessage(), e);
        }

        if (remote.isPresent()) {
            fillFromEntries(remote.get());
            addon.logger().info("[ServerRegistry] Loaded server configurations from remote URL: {}", remoteUrl);
            return;
        }

        try {
            Optional<List<ServerEntry>> bundled = loadFromResource();
            if (bundled.isPresent()) {
                fillFromEntries(bundled.get());
                addon.logger().info("[ServerRegistry] Loaded server configurations from bundled resource");
                return;
            } else {
                addon.logger().warn("[ServerRegistry] Bundled servers.json not found or empty");
            }
        } catch (Exception e) {
            addon.logger().warn("[ServerRegistry] Failed to load bundled servers.json: {}", e.getMessage(), e);
        }

        addon.logger().info("[ServerRegistry] No server configurations available; registry is empty");
    }

    private void fillFromEntries(List<ServerEntry> entries) {
        for (ServerEntry e : entries) {
            List<String> hosts = nonNullList(e.hosts);
            List<String> ids = nonNullList(e.identifiers);
            List<String> seps = nonNullList(e.separators);
            this.servers.add(new ServerConfigurationImpl(hosts, ids, seps));
        }
    }

    private List<String> nonNullList(List<String> list) {
        return list == null ? List.of() : list;
    }

    private Optional<List<ServerEntry>> loadFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("HTTP response code: " + code);
        }

        try (InputStream in = conn.getInputStream()) {
            String json = readStream(in);
            return Optional.ofNullable(parseServersJson(json));
        }
    }

    private Optional<List<ServerEntry>> loadFromResource() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(BUNDLED_RESOURCE_PATH)) {
            if (in == null) return Optional.empty();
            String json = readStream(in);
            return Optional.ofNullable(parseServersJson(json));
        }
    }

    private String readStream(InputStream in) throws IOException {
        Objects.requireNonNull(in);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    private List<ServerEntry> parseServersJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        Gson gson = new Gson();
        ServersWrapper wrapper = gson.fromJson(json, ServersWrapper.class);
        return wrapper == null || wrapper.servers == null ? List.of() : wrapper.servers;
    }

    private static class ServersWrapper {
        @SerializedName("servers")
        List<ServerEntry> servers;
    }

    private static class ServerEntry {
        @SerializedName("hosts")
        List<String> hosts;

        @SerializedName("identifiers")
        List<String> identifiers;

        @SerializedName("separators")
        List<String> separators;
    }

    public List<ServerConfiguration> getServers() {
        return this.servers;
    }

}
