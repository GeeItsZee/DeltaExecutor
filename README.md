# DeltaExecutor
DeltaExecutor is an async task executor for Spigot servers.

## Why use DeltaExecutor?
- Uses a thread pool (which means threads are reused instead of recreated for every task)
- Supports a configurable nice shutdown that allows tasks to complete
- Supports task debugging
- Provides more information on a task status

## How do I use DeltaExecutor?

### Getting the DeltaExecutor instance
```java
DeltaExecutor exec = DeltaExecutor.instance()
```

### Scheduling a runnable
```java
Runnable r = ...
long taskId = DeltaExecutor.instance().execute(r);
```

### Cancelling a runnable
```java
CancelResult result = DeltaExecutor.instance().cancel(taskId);
switch(result) {
  case CANCELLED:
    // Handle the task being successfully cancelled
    break;
  case RUNNING:
    // Handle the task failing to be cancelled due to it being currently run
    break;
  case NOT_FOUND:
    // Handle the task not being found (completed task or not existent task)
    break;
}
```

### Shutting Down
```java
// It is recommended to call this onDisable() of all plugins that use DeltaExecutor
// It is safe to use shutdown() after using shutdown()
// The executor falls back to synchronous execution after shutdown
DeltaExecutor.instance().shutdown();
```

## Planned Features
- Task Metadata
- Callable support

## Not Planned Features
- Repeated async task support


# Licence ([GPLv3](http://www.gnu.org/licenses/gpl-3.0.en.html))
```
DeltaExecutor - Async task executor for Bukkit/Spigot servers.
Copyright (C) 2015  Trace Bachi (tracebachi@gmail.com)

DeltaExecutor is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DeltaExecutor is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DeltaExecutor.  If not, see <http://www.gnu.org/licenses/>.
```
