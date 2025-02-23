package com.github.groundbreakingmc.gigachat.utils.afk;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.github.groundbreakingmc.gigachat.GigaChat;
import org.bukkit.entity.Player;

public class EssentialsAfkChecker implements AfkChecker {

    private final IEssentials essentials;

    public EssentialsAfkChecker(final GigaChat plugin) {
        this.essentials = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
    }

    @Override
    public boolean isAfk(final Player target) {
        final User user = essentials.getUser(target);
        return user.isAfk();
    }
}
