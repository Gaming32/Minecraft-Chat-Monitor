package io.github.gaming32.twobeetwoare;

import javax.swing.JOptionPane;

import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundCustomPayloadPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundKeepAlivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundCustomPayloadPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;

import io.github.gaming32.twobeetwoare.gui.ChatGui;

public final class GameListener extends SessionAdapter {
    private static String OAM_CHANNEL = "openauthmod:join";

    private static enum ClientState {
        AWAITING_CONNECT,
        AWAITING_AUTH_REQUEST,
        IN_GAME_AFAIK
    }

    private ClientState state = ClientState.AWAITING_CONNECT;

    @Override
    public void packetReceived(Session session, Packet p) {
        LogToMultiplePlaces logger = session.getFlag(ChatMonitor.CHAT_LOGGER_KEY);
        ClientState oldState = state;
        switch (state) {
            case AWAITING_CONNECT:
                if (p instanceof ClientboundKeepAlivePacket) {
                    final String COMMAND = "/viaproxy 2b2r.org b1.7-b1.7.3";
                    session.send(new ServerboundChatPacket(COMMAND));
                    logger.println(COMMAND);
                    state = ClientState.AWAITING_AUTH_REQUEST;
                }
                break;
            case AWAITING_AUTH_REQUEST:
                if (p instanceof ClientboundCustomPayloadPacket) {
                    ClientboundCustomPayloadPacket packet = (ClientboundCustomPayloadPacket)p;
                    if (packet.getChannel().equals(OAM_CHANNEL)) {
                        SessionService sessionService = session.getFlag(MinecraftConstants.SESSION_SERVICE_KEY);
                        try {
                            sessionService.joinServer(
                                session.getFlag(MinecraftConstants.PROFILE_KEY),
                                session.getFlag(MinecraftConstants.ACCESS_TOKEN_KEY),
                                new String(packet.getData(), 1, packet.getData().length - 1)
                            );
                            session.send(new ServerboundCustomPayloadPacket(OAM_CHANNEL, new byte[] {1}));
                            state = ClientState.IN_GAME_AFAIK;
                        } catch (Exception e) {
                            session.send(new ServerboundCustomPayloadPacket(OAM_CHANNEL, new byte[] {0}));
                            session.disconnect("Failed to authenticate", e);
                        }
                    }
                }
                break;
            case IN_GAME_AFAIK:
                if (p instanceof ClientboundChatPacket) {
                    ClientboundChatPacket packet = (ClientboundChatPacket)p;
                    logger.println(MessageFormatter.formatMessage(packet.getMessage()));
                }
        }
        if (state != oldState) {
            logger.println("!Switched to the " + state + " state");
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        LogToMultiplePlaces logger = event.getSession().getFlag(ChatMonitor.CHAT_LOGGER_KEY);
        logger.println("!Disconnected for: " + event.getReason());
        if (event.getCause() != null) {
            event.getCause().printStackTrace();
            if (ChatMonitor.hasGui()) {
                JOptionPane.showMessageDialog(
                    null,
                    "Unexpectedly disconnected from server!\n" + event.getCause(),
                    ChatGui.TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            if (ChatMonitor.hasGui() && !event.getReason().equals(ChatGui.GUI_CLOSED_REASON)) {
                JOptionPane.showMessageDialog(
                    null,
                    "You have been disconnected (see the main window for why)",
                    ChatGui.TITLE,
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
        System.exit(event.getCause() == null ? 0 : 1);
    }
}
