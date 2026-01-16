package top.einsluca.autogg;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import top.einsluca.autogg.listener.ChatListener;
import top.einsluca.autogg.server.ServerRegistry;

import java.util.Timer;
import java.util.TimerTask;

@AddonMain
public class AutoGGAddon extends LabyAddon<AutoGGConfiguration> {

    private final ServerRegistry serverRegistry = new ServerRegistry(this);

    private boolean alreadySent = false;

    @Override
    protected void enable() {
        this.registerSettingCategory();

        this.registerListener(new ChatListener(this));
    }

    public void sendGG() {
        new Timer("sendGG").schedule(new TimerTask() {
            @Override
            public void run() {
                if (!alreadySent) {
                    Laby.references().chatExecutor().chat(configuration().message.getOrDefault("GG"), false);
                    alreadySent = true;
                }
            }
        }, 1000L * (int) Math.round(configuration().delay.getOrDefault(1.0)));


        new Timer("resetSend").schedule(new TimerTask() {
            @Override
            public void run() {
                alreadySent = false;
            }
        }, 15000L);
    }

    public ServerRegistry getServerRegistry() {
        return serverRegistry;
    }

    @Override
    protected Class<? extends AutoGGConfiguration> configurationClass() {
        return AutoGGConfiguration.class;
    }
}
