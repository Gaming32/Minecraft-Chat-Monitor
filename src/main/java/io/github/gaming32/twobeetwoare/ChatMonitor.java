package io.github.gaming32.twobeetwoare;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpClientSession;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.json.JSONTokener;

import io.github.gaming32.twobeetwoare.betacraft.BC;
import io.github.gaming32.twobeetwoare.gui.ChatGui;

public final class ChatMonitor {
    public static final String CHAT_LOGGER_KEY = "chat-logger";
    public static final String CHAT_GUI_KEY = "chat-gui";

    private static boolean hasGui;

    public static void main(String[] args) throws IOException {
        hasGui = args.length == 0 || !args[0].toLowerCase().endsWith("nogui");
        if (hasGui) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            UIManager.getLookAndFeelDefaults().put("Slider.paintValue", Boolean.FALSE); // GTK PLAF fix
        }

        Pair<GameProfile, String> profileData = getMsaAccessTokenFromBetacraft();
        if (profileData == null) {
            System.exit(1);
            return; // Unreachable
        }

        MinecraftProtocol protocol = new MinecraftProtocol(profileData.getLeft(), profileData.getRight());

        SessionService sessionService = new SessionService();
        sessionService.setProxy(Proxy.NO_PROXY);

        Session client = new TcpClientSession("viaproxy.raphimc.net", 25565, protocol, null);
        client.setFlag(MinecraftConstants.AUTOMATIC_KEEP_ALIVE_MANAGEMENT, Boolean.FALSE);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        client.setFlag(MinecraftConstants.PROFILE_KEY, profileData.getLeft());
        client.setFlag(MinecraftConstants.ACCESS_TOKEN_KEY, profileData.getRight());
        client.setFlag(CHAT_LOGGER_KEY, new LogToMultiplePlaces(client));
        client.setFlag(CHAT_GUI_KEY, null);

        client.addListener(new GameListener());
        client.connect(true);

        new KeepAliveTask(client).start();
        if (hasGui) {
            client.setFlag(CHAT_GUI_KEY, ChatGui.makeChatGui(client));
        }
    }

    public static boolean hasGui() {
        return hasGui;
    }

    private static Pair<GameProfile, String> getMsaAccessTokenFromBetacraft() {
        Path accountsJsonPath = Paths.get(BC.get(), "launcher", "accounts.json");
        JSONObject jsonRoot;
        try (Reader reader = new FileReader(accountsJsonPath.toFile())) {
            jsonRoot = new JSONObject(new JSONTokener(reader));
        } catch (Exception e) {
            final String message = "Can't read BetaCraft accounts.json";
            System.err.println(message);
            e.printStackTrace();
            if (hasGui) JOptionPane.showMessageDialog(null, message + ": " + e, ChatGui.TITLE, JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String currentAccount = jsonRoot.getString("current");
        for (Object accountObject : jsonRoot.getJSONArray("accounts")) {
            JSONObject account = (JSONObject)accountObject;
            if (account.getString("account_type").equals("MICROSOFT") && account.getString("local_uuid").equals(currentAccount)) {
                return Pair.of(new GameProfile(
                    currentAccount.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"),
                    account.getString("username")
                ), account.getString("access_token"));
            }
        }
        {
            final String message = "Unable to find valid Microsoft account in BetaCraft accounts.json";
            System.err.println(message);
            if (hasGui) JOptionPane.showMessageDialog(null, message, ChatGui.TITLE, JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
}
