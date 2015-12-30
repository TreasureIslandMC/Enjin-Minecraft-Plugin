package com.enjin.bukkit.stats;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class WriteStats {
    EnjinMinecraftPlugin plugin;

    public WriteStats(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean write(String file) {
        try {
            BufferedWriter outChannel = new BufferedWriter(new FileWriter(file));
            String jsonString = getStatsJSON();
            outChannel.write(jsonString);
            outChannel.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public String getStatsJSON() {
        JSONObject stats = plugin.getServerStats().getSerialized();
        JSONArray players = new JSONArray();

        for (Map.Entry<String, StatsPlayer> player : plugin.getPlayerStats().entrySet()) {
            players.add(player.getValue().getSerialized());
        }

        stats.put("players", players);
        return JSONValue.toJSONString(stats);
    }

    public String write() {
        return getStatsJSON();
    }
}
