/*
 * This file is part of DeltaExecutor.
 *
 * DeltaExecutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaExecutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaExecutor.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaExecutor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/20/16.
 */
public class DeltaExecutorPlugin extends JavaPlugin
{
    @Override
    public void onLoad()
    {
        saveDefaultConfig();
        reloadConfig();

        int coreThreadCount = getConfig().getInt("CoreThreadCount");
        int maxThreadCount = getConfig().getInt("MaxThreadCount");
        int idleThreadTimeout = getConfig().getInt("IdleThreadTimeout");
        int niceShutdownPasses = getConfig().getInt("NiceShutdownPasses");
        boolean debugEnabled = getConfig().getBoolean("Debug");

        DeltaExecutor.initialize(
            getLogger(),
            coreThreadCount,
            maxThreadCount,
            idleThreadTimeout,
            niceShutdownPasses,
            debugEnabled);
    }

    @Override
    public void onDisable()
    {
        DeltaExecutor.instance().shutdown();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!sender.hasPermission("DeltaExecutor.Debug"))
        {
            String message = ChatColor.translateAlternateColorCodes(
                '&',
                "&8[&c!&8] &cFailure &8[&c!&8]&7 " +
                    "You do not have the &fDeltaExecutor.Debug&7 permission.");

            sender.sendMessage(message);
            return true;
        }

        if(args.length == 0)
        {
            String message = ChatColor.translateAlternateColorCodes(
                '&',
                "&8[&c!&8] &cFailure &8[&c!&8]&7 /deltaexecutordebug <on|off>");

            sender.sendMessage(message);
            return true;
        }

        DeltaExecutor instance = DeltaExecutor.instance();

        if(args[0].equalsIgnoreCase("on"))
        {
            String message = ChatColor.translateAlternateColorCodes(
                '&',
                "&8[&9!&8] &9Info &8[&9!&8]&7 DeltaExecutorDebug: &fON");

            instance.setDebugEnabled(true);
            sender.sendMessage(message);
        }
        else if(args[0].equalsIgnoreCase("off"))
        {
            String message = ChatColor.translateAlternateColorCodes(
                '&',
                "&8[&9!&8] &9Info &8[&9!&8]&7 DeltaExecutorDebug: &fOFF");

            instance.setDebugEnabled(false);
            sender.sendMessage(message);
        }
        else
        {
            String message = ChatColor.translateAlternateColorCodes(
                '&',
                "&8[&c!&8] &cFailure &8[&c!&8]&7 /deltaexecutordebug <on|off>");

            sender.sendMessage(message);
        }

        return true;
    }
}
