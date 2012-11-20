package com.enjin.officialplugin.threaded;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class PeriodicVoteTask implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap<String, String>();
	int numoffailedtries = 0;
	boolean firstrun = true;
	
	public PeriodicVoteTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-votifier");
	}
	
	@Override
	public void run() {
		//Only run if we have votes to send.
		if(plugin.playervotes.size() > 0) {
			
			//Only run the ssl test on first run.
			if(firstrun && EnjinMinecraftPlugin.usingSSL) {
				if(!plugin.testHTTPSconnection()) {
					EnjinMinecraftPlugin.usingSSL = false;
					plugin.getLogger().warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
					EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
				}
			}
			
			boolean successful = false;
			StringBuilder builder = new StringBuilder();
			try {
				plugin.debug("Connecting to Enjin to send votes...");
				URL enjinurl = getUrl();
				HttpURLConnection con;
				// Mineshafter creates a socks proxy, so we can safely bypass it
		        // It does not reroute POST requests so we need to go around it
		        if (EnjinMinecraftPlugin.isMineshafterPresent()) {
		            con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
		        } else {
		            con = (HttpURLConnection) enjinurl.openConnection();
		        }
				con.setRequestMethod("POST");
				con.setReadTimeout(15000);
				con.setConnectTimeout(15000);
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setRequestProperty("User-Agent", "Mozilla/4.0");
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//StringBuilder builder = new StringBuilder();
				builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
				builder.append("&votifier=" + encode(getVotes()));
				builder.append("&accepts-packets=true");
				con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
				plugin.debug("Sending content: \n" + builder.toString());
				con.getOutputStream().write(builder.toString().getBytes());
				//System.out.println("Getting input stream...");
				InputStream in = con.getInputStream();
				//Let's just use the other method we already have so that there is
				//only one place to maintain.
				String success = plugin.getTask().handleInput(in);
				//System.out.println("Handling input stream...");
				if(success.equalsIgnoreCase("ok")) {
					successful = true;
					if(plugin.unabletocontactenjin) {
						plugin.unabletocontactenjin = false;
						Player[] players = plugin.getServer().getOnlinePlayers();
						for(Player player : players) {
							if(player.hasPermission("enjin.notify.connectionstatus")) {
								player.sendMessage(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Connection to Enjin re-established!");
								plugin.getLogger().info("Connection to Enjin re-established!");
							}
						}
					}
				}else if(success.equalsIgnoreCase("auth_error")) {
					plugin.authkeyinvalid = true;
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
					plugin.getLogger().warning("Auth key invalid. Please regenerate on the enjin control panel.");
					plugin.stopTask();
					Player[] players = plugin.getServer().getOnlinePlayers();
					for(Player player : players) {
						if(player.hasPermission("enjin.notify.invalidauthkey")) {
							player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
						}
					}
					successful = false;
				}else if(success.equalsIgnoreCase("bad_data")) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					plugin.getLogger().warning("Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					successful = false;
				}else if(success.equalsIgnoreCase("retry_later")) {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
					plugin.getLogger().info("Enjin said to wait, saving data for next sync.");
					successful = false;
				}else if(success.equalsIgnoreCase("connect_error")) {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
					plugin.getLogger().info("Enjin is having something going on, if you continue to see this error please report it to enjin.");
					successful = false;
				}else {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
					EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
					plugin.getLogger().info("Something happened on sync, if you continue to see this error please report it to enjin.");
					plugin.getLogger().info("Response code: " + success);
					successful = false;
				}
			} catch (SocketTimeoutException e) {
				//We don't need to spam the console every minute if the synch didn't complete correctly.
				if(numoffailedtries++ > 5) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
					Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
					numoffailedtries = 0;
				}
				plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
			} catch (Throwable t) {
				//We don't need to spam the console every minute if the synch didn't complete correctly.
				if(numoffailedtries++ > 30) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
					Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
					numoffailedtries = 0;
				}
				if(plugin.debug) {
					t.printStackTrace();
				}
				plugin.lasterror = new EnjinErrorReport(t, "Votifier sync. Information sent:\n" + builder.toString());
				EnjinMinecraftPlugin.enjinlogger.warning(plugin.lasterror.toString());
			}
			if(!successful) {
				plugin.debug("Vote sync unsuccessful.");
				
				Set<Entry<String, String>> voteset = removedplayervotes.entrySet();
				for(Entry<String, String> entry : voteset) {
					//If the plugin has put new votes in, let's not overwrite them.
					if(plugin.playervotes.containsKey(entry.getKey())) {
						//combine the lists.
						String lists = plugin.playervotes.get(entry.getKey()) + "," + entry.getValue();
						plugin.playervotes.put(entry.getKey(), lists);
					}else {
						plugin.playervotes.put(entry.getKey(), entry.getValue());
					}
					removedplayervotes.remove(entry.getKey());
				}
			}else {
				plugin.debug("Vote sync successful.");
				firstrun = false;
			}
		}
	}
	
	private String getVotes() {
		removedplayervotes.clear();
		StringBuilder votes = new StringBuilder();
		Set<Entry<String,String>> voteset = plugin.playervotes.entrySet();
		for(Entry<String, String> entry : voteset) {
			String player = entry.getKey();
			String lists = entry.getValue();
			if(votes.length() != 0) {
				votes.append(";");
			}
			votes.append(player + ":" + lists);
			removedplayervotes.put(player, lists);
			plugin.playervotes.remove(player);
		}
		return votes.toString();
	}
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
}
