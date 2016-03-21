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

import com.gmail.tracebachi.DeltaExecutor.Enums.TaskStatus;
import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/19/16.
 */
public class AsyncTask
{
    private static final AtomicLong counter = new AtomicLong(0L);

    private final long id;
    private final Runnable runnable;
    private TaskStatus status = TaskStatus.WAITING;

    public AsyncTask(Runnable runnable)
    {
        this.id = counter.incrementAndGet();
        this.runnable = Preconditions.checkNotNull(runnable);
    }

    public long getId()
    {
        return id;
    }

    public Runnable getRunnable()
    {
        return runnable;
    }

    public synchronized TaskStatus getStatus()
    {
        return status;
    }

    public synchronized void setStatus(TaskStatus status)
    {
        this.status = Preconditions.checkNotNull(status);
    }

    public synchronized boolean switchToRunning()
    {
        if(status == TaskStatus.WAITING)
        {
            status = TaskStatus.RUNNING;
            return true;
        }

        return false;
    }

    public synchronized boolean switchToCancelled()
    {
        if(status == TaskStatus.WAITING)
        {
            status = TaskStatus.CANCELLED;
            return true;
        }

        return false;
    }
}
