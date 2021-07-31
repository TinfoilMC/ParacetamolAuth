package br.nom.yt.re.paracetamol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ParacetamolAuth implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientLoginNetworking.registerGlobalReceiver(new Identifier("viaaas", "reauth"),
				(client, handler, buf, listenerAdder) -> {
					var username = buf.readString();
					var connectionHash = buf.readString();

					if (!username.equalsIgnoreCase(client.getSession().getUsername())) {
						return CompletableFuture.completedFuture(null);
					}

					var future = new CompletableFuture<PacketByteBuf>();
					client.execute(() -> {
						var oldScreen = client.currentScreen;

						client.setScreen(new ConfirmScreen(
								result -> {
									client.setScreen(oldScreen);
									NetworkUtils.EXECUTOR.execute(() -> {
										try {
											client.getSessionService().joinServer(client.getSession().getProfile(),
													client.getSession().getAccessToken(), connectionHash);
										} catch (Exception e) {
											e.printStackTrace();
											future.complete(null);
										}
										future.complete(PacketByteBufs.empty());
									});
								},
								new LiteralText("Allow VIAaaS to impersonate you?"),
								new LiteralText("This will allow VIAaaS instance to authenticate as you in a Minecraft server.")
						));
					});

					return future;
				});
	}
}
