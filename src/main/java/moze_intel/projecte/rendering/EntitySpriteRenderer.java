package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Based on {@link net.minecraft.client.renderer.entity.DragonFireballRenderer}
 */
public class EntitySpriteRenderer<ENTITY extends Entity> extends EntityRenderer<ENTITY, EntityRenderState> {

	private final RenderType renderType;

	public EntitySpriteRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context);
		this.renderType = PERenderType.SPRITE_RENDERER.apply(texture);
	}

	@Override
	protected int getBlockLightLevel(@NotNull ENTITY entity, @NotNull BlockPos pos) {
		return 15;
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	public void submit(@NotNull EntityRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector,
			@NotNull CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.scale(0.5F, 0.5F, 0.5F);
		poseStack.rotate(camera.orientation);
		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, builder) -> {
			vertex(builder, pose, state.lightCoords, 0, 0, 0, 1);
			vertex(builder, pose, state.lightCoords, 1, 0, 1, 1);
			vertex(builder, pose, state.lightCoords, 1, 1, 1, 0);
			vertex(builder, pose, state.lightCoords, 0, 1, 0, 0);
		});
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int lightCoords, float x, int y, int u, int v) {
		consumer.addVertex(pose, x - 0.5F, y, 0)
				.setColor(-1)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(lightCoords)
				.setNormal(pose, 0, 1, 0);
	}
}
