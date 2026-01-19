package br.nom.yt.re.paracetamol;

import com.mojang.authlib.exceptions.AuthenticationException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ParacetamolAuth implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(Identifier.of("oam", "join"),
                (client, handler, buf, listenerAdder) -> {
                    var connectionHash = buf.readString();

                    var future = new CompletableFuture<PacketByteBuf>();
                    client.execute(() -> {
                        var oldScreen = client.currentScreen;

                        client.setScreen(new ConfirmScreen(
                                result -> {
                                    client.setScreen(oldScreen);

                                    if (result) {
                                        try {
                                            joinServer(client, connectionHash);
                                            future.complete(new PacketByteBuf(generateSuccessResponse()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            future.complete(null);
                                        }
                                    } else {
                                        future.complete(null);
                                    }
                                },
                                Text.translatable("viaaas.impersonate.title"),
                                Text.translatable("viaaas.impersonate.content")
                        ));
                    });

                    return future;
                });
    }

    private void joinServer(MinecraftClient client, String connectionHash) throws AuthenticationException {
        client.getApiServices().sessionService().joinServer(
                client.getSession().getUuidOrNull(),
                client.getSession().getAccessToken(),
                connectionHash);
    }


    private ByteBuf generateSuccessResponse() {
        return Unpooled.wrappedBuffer(new byte[]{1});
    }
}
