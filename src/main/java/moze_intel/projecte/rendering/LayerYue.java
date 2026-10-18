package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.UUID;
import moze_intel.projecte.PECore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

public class LayerYue extends RenderLayer<AvatarRenderState, PlayerModel> {

	private static final UUID SIN_UUID = UUID.fromString("5f86012c-ca4b-451a-989c-8fab167af647");
	private static final UUID CLAR_UUID = UUID.fromString("e5c59746-9cf7-4940-a849-d09e1f1efc13");
	private static final Identifier HEART_LOC = PECore.rl("textures/models/heartcircle.png");
	private static final Identifier YUE_LOC = PECore.rl("textures/models/yuecircle.png");

	public LayerYue(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int lightCoords, @NotNull AvatarRenderState state,
			float yRot, float xRot) {
		Minecraft minecraft = Minecraft.getInstance();
		if (state.isInvisible || minecraft.level == null) {
			return;
		}
		//EntityRenderState does not expose the UUID, so recover the entity from its id to check it
		if (!(minecraft.level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
			return;
		}
		UUID uuid = player.getUUID();
		if (FMLEnvironment.isProduction() && !SIN_UUID.equals(uuid) && !CLAR_UUID.equals(uuid)) {
			return;
		}
		poseStack.pushPose();
		getParentModel().body.translateAndRotate(poseStack);
		double yShift = -0.498;
		if (state.isCrouching) {
			//Only modify where it renders if the player's pose is crouching
			poseStack.rotate(Axis.XN.rotation(0.5F));
			yShift = -0.44;
		}
		poseStack.rotate(Axis.ZP.rotation(Mth.PI));
		poseStack.scale(3, 3, 3);
		poseStack.translate(-0.5, yShift, -0.5);
		Identifier texture = CLAR_UUID.equals(uuid) ? HEART_LOC : YUE_LOC;
		submitNodeCollector.submitCustomGeometry(poseStack, PERenderType.YEU_RENDERER.apply(texture), (pose, builder) -> {
			vertex(builder, pose, lightCoords, 0, 0, 0, 0, 0);
			vertex(builder, pose, lightCoords, 0, 0, 1, 0, 1);
			vertex(builder, pose, lightCoords, 1, 0, 1, 1, 1);
			vertex(builder, pose, lightCoords, 1, 0, 0, 1, 0);
		});
		poseStack.popPose();
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int lightCoords, float x, float y, float z, int u, int v) {
		consumer.addVertex(pose, x, y, z)
				.setColor(0, 255, 0, 255)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightCoords)
				.setNormal(pose, 0, 1, 0);
	}
}
