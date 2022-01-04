package io.github.gaming32.chatmonitor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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

import io.github.gaming32.chatmonitor.betacraft.BetaCraftFolder;
import io.github.gaming32.chatmonitor.gui.ChatGui;

public final class ChatMonitor {
    public static final Scanner STDIN = new Scanner(System.in);

    private static boolean hasGui;

    public static void main(String[] args) throws IOException {
        List<String> argsL = new ArrayList<>(Arrays.asList(args));
        hasGui = discoverHasGui(argsL);
        String serverAddress = consumeArgument(argsL);
        String serverVersion = consumeArgument(argsL);

        if (hasGui) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            UIManager.getLookAndFeelDefaults().put("Slider.paintValue", Boolean.FALSE); // GTK PLAF fix
        }

        if (serverAddress == null) {
            if (hasGui) {
                serverAddress = JOptionPane.showInputDialog("Enter server address");
            } else {
                System.out.println("Enter server address: ");
                serverAddress = STDIN.nextLine();
            }
        }
        if (serverVersion == null) {
            if (hasGui) {
                serverVersion = JOptionPane.showInputDialog("Enter server version (ViaProxy form)");
            } else {
                System.out.println("Enter server version (ViaProxy form): ");
                serverVersion = STDIN.nextLine();
            }
        }
        if (!ChatMonitorConstants.VERSION_NUMBERS.contains(serverVersion)) {
            StringBuilder messageBuilder = new StringBuilder("Invalid version: ")
                .append(serverVersion)
                .append("\nPlease choose from the following versions: ");
            ChatMonitorConstants.VERSION_NUMBERS.forEach((ver) -> messageBuilder.append(ver).append(", "));
            final String message = Utils.lineBreaks(messageBuilder.substring(0, messageBuilder.length() - 2), 100);
            System.err.println(message);
            if (hasGui) {
                JOptionPane.showMessageDialog(null, message, ChatGui.TITLE, JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
            return;
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
        client.setFlag(ChatMonitorConstants.CHAT_SERVER_ADDRESS_KEY, serverAddress);
        client.setFlag(ChatMonitorConstants.CHAT_SERVER_VERSION_KEY, serverVersion);
        client.setFlag(ChatMonitorConstants.CHAT_LOGGER_KEY, new LogToMultiplePlaces(client));
        client.setFlag(ChatMonitorConstants.CHAT_GUI_KEY, null);

        client.addListener(new GameListener());
        client.connect(true);

        new KeepAliveTask(client).start();
        if (hasGui) {
            client.setFlag(ChatMonitorConstants.CHAT_GUI_KEY, ChatGui.makeChatGui(client));
        }
    }

    private static boolean discoverHasGui(List<String> args) {
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).endsWith("nogui")) {
                args.remove(i);
                return false;
            }
        }
        return true;
    }

    private static String consumeArgument(List<String> args) {
        return args.size() > 0 ? args.remove(0) : null;
    }

    public static boolean hasGui() {
        return hasGui;
    }

    private static Pair<GameProfile, String> getMsaAccessTokenFromBetacraft() {
        String bcFolder = BetaCraftFolder.get();
        if (bcFolder == null) {
            final String message = "Your OS is not supported";
            System.err.println(message);
            if (hasGui) {
                JOptionPane.showMessageDialog(null, message, ChatGui.TITLE, JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        Path accountsJsonPath = Paths.get(bcFolder, "launcher", "accounts.json");
        if (!Files.isRegularFile(accountsJsonPath)) {
            final String message = "BetaCraft does not appear to be installed. Please install it.";
            System.err.println(message);
            if (hasGui) {
                JOptionPane.showMessageDialog(null, message, ChatGui.TITLE, JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }

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
            final String message = "Unable to find valid Microsoft account in BetaCraft accounts.json.\nPlease sign into BetaCraft with a Microsoft account.";
            System.err.println(message);
            if (hasGui) JOptionPane.showMessageDialog(null, message, ChatGui.TITLE, JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
}
