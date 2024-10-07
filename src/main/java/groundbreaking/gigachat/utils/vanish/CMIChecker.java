package groundbreaking.gigachat.utils.vanish;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import org.bukkit.entity.Player;

public final class CMIChecker implements IVanishChecker {

    private final CMI instance;

    public CMIChecker() {
        instance = CMI.getInstance();
    }

    @Override
    public boolean isVanished(final Player player) {
        final CMIUser user = instance.getPlayerManager().getUser(player);
        return user.isCMIVanished();
    }
}
