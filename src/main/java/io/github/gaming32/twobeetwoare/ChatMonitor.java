package io.github.gaming32.twobeetwoare;

import java.io.FileReader;
import java.io.Reader;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

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

public class ChatMonitor {
    public static void main(String[] args) {
        Pair<GameProfile, String> profileData = getMsaAccessTokenFromBetacraft();
        if (profileData == null) {
            System.exit(1);
            return; // Unreachable
        }

        MinecraftProtocol protocol = new MinecraftProtocol(profileData.getLeft(), profileData.getRight());

        SessionService sessionService = new SessionService();
        sessionService.setProxy(Proxy.NO_PROXY);

        Session client = new TcpClientSession("viaproxy.lenni0451.net", 25565, protocol, null);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        client.setFlag(MinecraftConstants.PROFILE_KEY, profileData.getLeft());
        client.setFlag(MinecraftConstants.ACCESS_TOKEN_KEY, profileData.getRight());

        client.addListener(new GameListener());
        client.connect();
    }

    private static Pair<GameProfile, String> getMsaAccessTokenFromBetacraft() {
        Path accountsJsonPath = Paths.get(BC.get(), "launcher", "accounts.json");
        JSONObject jsonRoot;
        try (Reader reader = new FileReader(accountsJsonPath.toFile())) {
            jsonRoot = new JSONObject(new JSONTokener(reader));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString(), "Can't read BetaCraft accounts.json", JOptionPane.ERROR_MESSAGE);
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
        return null;
    }
}
