package com.github.groundbreakingmc.gigachat.utils;

import com.github.groundbreakingmc.gigachat.GigaChat;

public final class ServerInfo {

    public int getSubVersion(final GigaChat plugin) {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]);
        } catch (NumberFormatException ex) {
            plugin.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public boolean isPaperOrFork() {
        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
