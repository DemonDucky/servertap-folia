package io.servertap.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class to abstract scheduler differences between Paper and Folia.
 * This class automatically detects if the server is running Folia and uses the appropriate scheduler.
 */
public class SchedulerUtil {
    private static final boolean IS_FOLIA;
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "ServerTap-Async");
        thread.setDaemon(true);
        return thread;
    });

    static {
        boolean isFolia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            // Not Folia, use standard Bukkit scheduler
        }
        IS_FOLIA = isFolia;
    }

    /**
     * Check if the server is running Folia
     * @return true if Folia, false if Paper/Bukkit
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Run a task on the global region scheduler (Folia) or main thread (Paper).
     * Use this for tasks that don't need to be tied to a specific world/region.
     */
    public static BukkitTask runTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object globalRegionScheduler = Bukkit.class
                        .getMethod("getGlobalRegionScheduler")
                        .invoke(Bukkit.getServer());
                // Folia uses Consumer<ScheduledTask> instead of Runnable
                Consumer<Object> taskWrapper = (scheduledTask) -> task.run();
                Object scheduledTask = globalRegionScheduler.getClass()
                        .getMethod("run", Plugin.class, Consumer.class)
                        .invoke(globalRegionScheduler, plugin, taskWrapper);
                // Wrap Folia's ScheduledTask in a BukkitTask wrapper
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule task on Folia", e);
            }
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task asynchronously.
     * Note: Folia doesn't have async scheduler, so we use an executor service.
     */
    public static BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            // Folia doesn't have async scheduler, use executor service
            CompletableFuture<BukkitTask> future = new CompletableFuture<>();
            ASYNC_EXECUTOR.execute(() -> {
                try {
                    task.run();
                    // Return a dummy task for compatibility
                    future.complete(new DummyBukkitTask());
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to run async task", e);
            }
        } else {
            return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Run a task timer on the global region scheduler (Folia) or main thread (Paper).
     * Use this for repeating tasks that don't need to be tied to a specific world/region.
     */
    public static BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            try {
                Object globalRegionScheduler = Bukkit.class
                        .getMethod("getGlobalRegionScheduler")
                        .invoke(Bukkit.getServer());
                // Folia uses Consumer<ScheduledTask> instead of Runnable
                Consumer<Object> taskWrapper = (scheduledTask) -> task.run();
                Object scheduledTask = globalRegionScheduler.getClass()
                        .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)
                        .invoke(globalRegionScheduler, plugin, taskWrapper, delay, period);
                // Wrap Folia's ScheduledTask in a BukkitTask wrapper
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule task timer on Folia", e);
            }
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }

    /**
     * Run a task later on the global region scheduler (Folia) or main thread (Paper).
     */
    public static BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (IS_FOLIA) {
            try {
                Object globalRegionScheduler = Bukkit.class
                        .getMethod("getGlobalRegionScheduler")
                        .invoke(Bukkit.getServer());
                // Folia uses Consumer<ScheduledTask> instead of Runnable
                Consumer<Object> taskWrapper = (scheduledTask) -> task.run();
                Object scheduledTask = globalRegionScheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, long.class)
                        .invoke(globalRegionScheduler, plugin, taskWrapper, delay);
                // Wrap Folia's ScheduledTask in a BukkitTask wrapper
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule delayed task on Folia", e);
            }
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    /**
     * Run a task on a specific region (Folia) or main thread (Paper).
     * Use this for tasks that need to access world-specific data.
     */
    public static BukkitTask runTaskAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object regionScheduler = Bukkit.class
                        .getMethod("getRegionScheduler")
                        .invoke(Bukkit.getServer());
                // Folia uses Consumer<ScheduledTask> instead of Runnable
                Consumer<Object> taskWrapper = (scheduledTask) -> task.run();
                Object scheduledTask = regionScheduler.getClass()
                        .getMethod("run", Plugin.class, Location.class, Consumer.class)
                        .invoke(regionScheduler, plugin, location, taskWrapper);
                // Wrap Folia's ScheduledTask in a BukkitTask wrapper
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule task at location on Folia", e);
            }
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task on an entity's scheduler (Folia) or main thread (Paper).
     * Use this for tasks that need to access entity-specific data.
     */
    public static BukkitTask runTaskOnEntity(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object entityScheduler = entity.getClass()
                        .getMethod("getScheduler")
                        .invoke(entity);
                // Folia uses Consumer<ScheduledTask> instead of Runnable
                Consumer<Object> taskWrapper = (scheduledTask) -> task.run();
                Object scheduledTask = entityScheduler.getClass()
                        .getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                        .invoke(entityScheduler, plugin, taskWrapper, null);
                // Wrap Folia's ScheduledTask in a BukkitTask wrapper
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                throw new RuntimeException("Failed to schedule task on entity on Folia", e);
            }
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Call a method synchronously and return the result.
     * For Folia, this uses the global region scheduler.
     */
    public static <T> CompletableFuture<T> callSyncMethod(Plugin plugin, Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (IS_FOLIA) {
            runTask(plugin, () -> {
                try {
                    future.complete(supplier.get());
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    future.complete(supplier.get());
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        }
        return future;
    }

    /**
     * Wrapper class to wrap Folia's ScheduledTask as a BukkitTask
     */
    private static class FoliaTaskWrapper implements BukkitTask {
        private final Object scheduledTask;
        private final Plugin owner;

        public FoliaTaskWrapper(Object scheduledTask) {
            this.scheduledTask = scheduledTask;
            // Try to get the owning plugin from the ScheduledTask
            Plugin ownerPlugin = null;
            try {
                ownerPlugin = (Plugin) scheduledTask.getClass()
                        .getMethod("getOwningPlugin")
                        .invoke(scheduledTask);
            } catch (Exception e) {
                // If we can't get the owner, it's okay, we'll return null
            }
            this.owner = ownerPlugin;
        }

        @Override
        public int getTaskId() {
            // Folia's ScheduledTask doesn't have a task ID, return -1
            return -1;
        }

        @Override
        public Plugin getOwner() {
            return owner;
        }

        @Override
        public boolean isSync() {
            // All Folia tasks are "sync" in their respective regions
            return true;
        }

        @Override
        public boolean isCancelled() {
            try {
                return (boolean) scheduledTask.getClass()
                        .getMethod("isCancelled")
                        .invoke(scheduledTask);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void cancel() {
            try {
                scheduledTask.getClass()
                        .getMethod("cancel")
                        .invoke(scheduledTask);
            } catch (Exception e) {
                // Silently fail if cancel doesn't work
            }
        }
    }

    /**
     * Dummy BukkitTask implementation for async tasks on Folia
     */
    private static class DummyBukkitTask implements BukkitTask {
        @Override
        public int getTaskId() {
            return -1;
        }

        @Override
        public Plugin getOwner() {
            return null;
        }

        @Override
        public boolean isSync() {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void cancel() {
            // No-op
        }
    }
}

