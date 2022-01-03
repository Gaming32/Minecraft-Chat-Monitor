package io.github.gaming32.chatmonitor;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundKeepAlivePacket;
import com.github.steveice10.packetlib.Session;

public class KeepAliveTask extends Thread {
    private Session session;

    public KeepAliveTask(Session session) {
        super("KeepAliveTask");
        setDaemon(true);
        this.session = session;
    }

    @Override
    public void run() {
        int pingId = 0;
        while (true) {
            session.send(new ServerboundKeepAlivePacket(pingId++));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
