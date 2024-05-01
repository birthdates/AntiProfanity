package com.birthdates.antiprofanity;

import com.birthdates.antiprofanity.event.AsyncChatBlockedEvent;
import com.birthdates.antiprofanity.util.Internet;
import com.birthdates.antiprofanity.data.RequestPayload;
import com.birthdates.antiprofanity.data.ResponsePayload;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AntiProfanity extends JavaPlugin implements Listener {
    private final Gson GSON = new Gson();
    private ExecutorService executor;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int threads = getConfig().getInt("max-threads", -1);
        executor = threads > 0 ? Executors.newFixedThreadPool(threads) : Executors.newCachedThreadPool();
        Bukkit.getPluginManager().registerEvents(this, this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.CHAT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacket().<Boolean>getMeta("filtered").orElse(false)) {
                    return;
                }
                event.setCancelled(true);
                String message = removeWhitelistedWords(event.getPacket().getStrings().read(0));
                // ignore messages with less than 3 characters to avoid spamming the API (also it false flags)
                if (message.length() < 3) {
                    return;
                }
                isProfane(message).thenAccept((response) -> {
                    if (response.isProfanity() && AsyncChatBlockedEvent.callEvent(event.getPlayer(), message, response.score())) {
                        return;
                    }

                    event.getPacket().setMeta("filtered", true);
                    ProtocolLibrary.getProtocolManager().receiveClientPacket(event.getPlayer(), event.getPacket());
                });
            }
        });
    }

    @Override
    public void onDisable() {
        executor.shutdown();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlocked(AsyncChatBlockedEvent event) {
        // format score into #.##
        String score = String.format("%.2f", event.getScore());
        String msg = ChatColor.of("#6b130d") + "[ꜰɪʟᴛᴇʀᴇᴅ] " + ChatColor.of("#d4807b") +
                event.getPlayer().getName() + ChatColor.RESET + " tried to say \"" +
                ChatColor.of("#dbb979") + ChatColor.ITALIC + event.getMessage() + ChatColor.RESET +
                "\" which got flagged with a score of " + ChatColor.of("#d4807b") + score;
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(x -> x.hasPermission("antiprofanity.alerts"))
                .forEach(x -> x.sendMessage(msg));
    }

    public CompletableFuture<ResponsePayload> isProfane(String message) {
        CompletableFuture<ResponsePayload> future = new CompletableFuture<>();
        executor.execute(() -> {    
            String res = Internet.sendRequest("https://vector.profanity.dev", "POST",
                    Map.of("Content-Type", "application/json"), GSON.toJson(new RequestPayload(message)));
            ResponsePayload responsePayload = GSON.fromJson(res, ResponsePayload.class);
            future.complete(responsePayload);
        });
        return future;
    }

    private String removeWhitelistedWords(String msg) {
        String lower = msg.toLowerCase();
        for (String word : getConfig().getStringList("whitelisted-words")) {
            int index;
            while ((index = lower.indexOf(word)) >= 0) {
                int end = index + word.length();
                lower = lower.substring(0, index) + lower.substring(end);
                msg = msg.substring(0, index) + msg.substring(end);
            }
        }
        return msg;
    }
}
