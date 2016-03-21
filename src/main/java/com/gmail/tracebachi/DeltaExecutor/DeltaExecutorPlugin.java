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
            debugEnabled
        );
    }

    @Override
    public void onDisable()
    {
        DeltaExecutor.instance().shutdown();
    }
}
