package com.birthdates.antiprofanity.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncChatBlockedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String message;
    private final double score;
    private boolean cancelled;

    public AsyncChatBlockedEvent(Player player, String message, double score) {
        super(true);
        this.player = player;
        this.score = score;
        this.message = message;
    }

    public static boolean callEvent(Player player, String message, double score) {
        AsyncChatBlockedEvent event = new AsyncChatBlockedEvent(player, message, score);
        event.setCancelled(false);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public double getScore() {
        return score;
    }

    public String getMessage() {
        return message;
    }

    public Player getPlayer() {
        return player;
    }

}
