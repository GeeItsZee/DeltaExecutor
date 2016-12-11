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

import com.gmail.tracebachi.DeltaExecutor.Enums.CancelResult;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/19/16.
 */
public class DeltaExecutor
{
    private static DeltaExecutor instance;

    private volatile boolean acceptingNewTasks;
    private volatile boolean debugEnabled;
    private final Logger logger;
    private final int niceShutdownPasses;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ConcurrentHashMap<Long, AsyncTask> taskMap;

    /**
     * @return Singleton reference to DeltaExecutor
     */
    public static DeltaExecutor instance()
    {
        if(instance == null)
        {
            throw new IllegalStateException("DeltaExecutor has not been initialized");
        }

        return instance;
    }

    /**
     * @return True if debug is enabled or false
     */
    public boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    /**
     * @param enable Boolean which controls if debug should be enabled or not
     */
    public void setDebugEnabled(boolean enable)
    {
        this.debugEnabled = enable;
    }

    /**
     * This method returns true if new tasks are being accepted to run
     * asynchronously. If they cannot be run async, they will be run
     * sync immediately.
     *
     * @return True or false.
     */
    public boolean isAcceptingNewTasks()
    {
        return acceptingNewTasks;
    }

    /**
     * If the executor is accepting new tasks (see {@link #isAcceptingNewTasks()},
     * the runnable passed to this method will be added to a queue to run
     * asynchronously. If the executor is not accepting new tasks, the
     * runnable passed will run synchronously.
     *
     * @param toExecute Runnable to execute
     * @return Assigned task ID
     */
    public long execute(Runnable toExecute)
    {
        AsyncTask task = new AsyncTask(toExecute);
        Long id = task.getId();

        taskMap.put(task.getId(), task);
        debug("Queued task #" + id);

        if(acceptingNewTasks)
        {
            threadPoolExecutor.execute(() -> runTask(id));
        }
        else
        {
            runTask(id);
        }

        return id;
    }

    /**
     * Attempts to cancel the task with the passed task ID
     *
     * @param taskId Assigned task ID
     * @return {@link CancelResult#CANCELLED} is the task was successfully
     * cancelled, {@link CancelResult#RUNNING} if the task is currently
     * being executed, or {@link CancelResult#NOT_FOUND} if the task ID is
     * unknown (either the task has completed or never existed)
     */
    public CancelResult cancel(long taskId)
    {
        AsyncTask task = taskMap.get(taskId);

        if(task != null)
        {
            if(task.switchToCancelled())
            {
                debug("Task #" + taskId + " has been cancelled.");
                return CancelResult.CANCELLED;
            }
            else
            {
                debug("Task #" + taskId + " could not be cancelled.");
                return CancelResult.RUNNING;
            }
        }

        debug("Task #" + taskId + " was not found during an attempt to cancel it.");
        return CancelResult.NOT_FOUND;
    }

    /**
     * This method attempts to shut down the executor nicely by waiting for all tasks.
     * If the executor fails to shutdown nicely, it will be shutdown forcibly.
     * In which case, worker threads will be interrupted and queued tasks will
     * be run synchronously.
     */
    public void shutdown()
    {
        if(!acceptingNewTasks) { return; }

        info("Shutting down DeltaExecutor ...");

        acceptingNewTasks = false;
        threadPoolExecutor.shutdown();

        try
        {
            if(!shutdownNicely())
            {
                shutdownForcibly();
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
            shutdownForcibly();
        }

        info("All tasks executed from this point on will be run synchronously.");
        info("Running tasks synchronously means your server may run slower if other plugins " +
            "relying on DeltaExecutor use it assuming it is running tasks not synchronously.");
    }

    static void initialize(Logger logger, int coreThreadCount, int maxThreadCount,
                           int idleThreadTimeout, int niceShutdownPasses, boolean debugEnabled)
    {
        if(instance != null)
        {
            throw new IllegalStateException("An instance of DeltaExecutor already exists.");
        }

        instance = new DeltaExecutor(
            logger,
            coreThreadCount,
            maxThreadCount,
            idleThreadTimeout,
            niceShutdownPasses,
            debugEnabled);
    }

    /**
     * Private constructor for DeltaExecutor
     */
    private DeltaExecutor(Logger logger, int coreThreadCount, int maxThreadCount,
                          int idleThreadTimeout, int niceShutdownPasses, boolean debugEnabled)
    {
        this.logger = logger;
        this.niceShutdownPasses = niceShutdownPasses;
        this.debugEnabled = debugEnabled;
        this.taskMap = new ConcurrentHashMap<>();

        this.threadPoolExecutor = new ThreadPoolExecutor(
            coreThreadCount,
            maxThreadCount,
            idleThreadTimeout,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("delta-executor-pool-%d").build());

        this.acceptingNewTasks = true;
    }

    private void runTask(long taskId)
    {
        AsyncTask asyncTask = taskMap.get(taskId);

        if(asyncTask == null)
        {
            severe("Task #" + taskId + " not found. Was it added it using DeltaExecutor#execute()?");
            return;
        }

        try
        {
            if(asyncTask.switchToRunning())
            {
                debug("Running task #" + taskId);
                asyncTask.getRunnable().run();
                debug("Completed task #" + taskId);
            }
        }
        catch(Exception ex)
        {
            severe("Exception in task #" + taskId);
            ex.printStackTrace();
        }
        finally
        {
            debug("Cleaning up task #" + taskId);
            taskMap.remove(taskId);
        }
    }

    private boolean shutdownNicely() throws InterruptedException
    {
        boolean terminated = false;

        for(int i = 1; i <= niceShutdownPasses && !terminated; ++i)
        {
            info("(Pass " + i + " of " + niceShutdownPasses + ") " +
                "Waiting 30 seconds for all tasks to complete ...");

            terminated = threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS);

            if(terminated)
            {
                info("DeltaExecutor has been shutdown nicely.");
            }
            else
            {
                info("Tasks Remaining: " + taskMap.size());
            }
        }

        return terminated;
    }

    private void shutdownForcibly()
    {
        info("Failed to shutdown DeltaExecutor nicely. Shutting down forcibly ...");

        for(Runnable runnable : threadPoolExecutor.shutdownNow())
        {
            try
            {
                runnable.run();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }

        info("DeltaExecutor has been shutting down forcibly.");
    }

    private void info(String input)
    {
        logger.info(input);
    }

    private void severe(String input)
    {
        logger.severe(input);
    }

    private void debug(String input)
    {
        if(debugEnabled)
        {
            logger.info("[Debug] " + input);
        }
    }
}
