package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.canarymod.Canary;
import net.canarymod.api.OfflinePlayer;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.user.Group;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.RemovePlayerGroupEvent;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet11RemovePlayerGroup {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String instring = PacketUtilities.readString(in);
			EnjinMinecraftPlugin.debug("Read string: " + instring);
			String[] msg = instring.split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				if("*".equals(world)) {
					world = null;
				}
				EnjinMinecraftPlugin.debug("Removing player " + playername + " from group " + groupname + " in world " + world + " world");
				
				Player target = Canary.getServer().matchPlayer(playername);
		        Group group = Canary.usersAndGroups().getGroup(groupname);
		        if(group == null) {
		            return;
		        }
		        if(target == null) {
		            OfflinePlayer oplayer = Canary.getServer().getOfflinePlayer(playername);
		            oplayer.removeGroup(group);
		        }else {
			        target.removeGroup(group);
		        }
				
		        Canary.hooks().callHook(new RemovePlayerGroupEvent(playername, groupname, world));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}