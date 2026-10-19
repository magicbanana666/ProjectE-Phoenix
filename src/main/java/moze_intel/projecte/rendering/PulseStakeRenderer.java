package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moze_intel.projecte.gameObjs.block_entities.PulseStakeBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Floats the focus of a Pulse Stake above it, turning faster while the array is formed.
 */
public class PulseStakeRenderer implements BlockEntityRenderer<PulseStakeBlockEntity, PulseStakeRenderer.RenderState> {

	private final BlockEntityRendererProvider.Context context;

	public PulseStakeRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(@NotNull PulseStakeBlockEntity stake, @NotNull RenderState state, float partialTicks, @NotNull Vec3 cameraPosition,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(stake, state, partialTicks, cameraPosition, breakProgress);
		state.focus.clear();
		Level level = stake.getLevel();
		if (level == null || stake.getFocus().isEmpty()) {
			return;
		}
		state.time = level.getGameTime() + partialTicks;
		state.formed = stake.isFormed();
		context.itemModelResolver().updateForTopItem(state.focus, stake.getFocus(), ItemDisplayContext.GROUND, level, null, (int) stake.getBlockPos().asLong());
	}

	@Override
	public void submit(@NotNull RenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
		if (state.focus.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.15F + Mth.sin(state.time / 10F) * 0.05F, 0.5F);
		poseStack.mulPose(Axis.YP.rotation(state.time / (state.formed ? 8F : 30F)));
		poseStack.scale(0.6F, 0.6F, 0.6F);
		state.focus.submit(poseStack, collector, state.formed ? 0xF000F0 : state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	public static class RenderState extends BlockEntityRenderState {

		public final ItemStackRenderState focus = new ItemStackRenderState();
		public float time;
		public boolean formed;
	}
}
