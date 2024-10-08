package groundbreaking.gigachat.utils.logging;

import groundbreaking.gigachat.GigaChat;

import java.util.logging.Logger;

public final class BukkitLogger implements ILogger {

    private final Logger logger;

    public BukkitLogger(final GigaChat plugin) {
        logger = plugin.getServer().getLogger();
    }

    public void info(final String msg) {
        logger.info(msg);
    }

    public void warning(final String msg) {
        logger.warning(msg);
    }
}