package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

//Only used on the client
// [VanillaCopy] Adapted from ChestRenderer
public class ChestRenderer implements BlockEntityRenderer<EmcChestBlockEntity, ChestRenderer.RenderState> {

	private final ModelPart lid;
	private final ModelPart bottom;
	private final ModelPart lock;

	private final BlockRegistryObject<?, ?> type;
	private final Identifier texture;

	public ChestRenderer(BlockEntityRendererProvider.Context context, Identifier texture, BlockRegistryObject<?, ?> type) {
		this.texture = texture;
		this.type = type;
		ModelPart modelpart = context.bakeLayer(ModelLayers.CHEST);
		this.bottom = modelpart.getChild("bottom");
		this.lid = modelpart.getChild("lid");
		this.lock = modelpart.getChild("lock");
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(@NotNull EmcChestBlockEntity chest, @NotNull RenderState state, float partialTicks, @NotNull Vec3 cameraPosition,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(chest, state, partialTicks, cameraPosition, breakProgress);
		state.yRot = Float.NaN;
		if (chest.getLevel() != null && !chest.isRemoved()) {
			BlockState blockState = chest.getLevel().getBlockState(chest.getBlockPos());
			if (blockState.is(type.getBlock())) {
				state.yRot = -blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
			}
		}
		float lidAngle = 1.0F - chest.getOpenNess(partialTicks);
		lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
		state.lidRot = -(lidAngle * Mth.HALF_PI);
	}

	@Override
	public void submit(@NotNull RenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector,
			@NotNull CameraRenderState camera) {
		poseStack.pushPose();
		if (!Float.isNaN(state.yRot)) {
			poseStack.translate(0.5D, 0.5D, 0.5D);
			poseStack.rotate(Axis.YP.rotationDegrees(state.yRot));
			poseStack.translate(-0.5D, -0.5D, -0.5D);
		}
		RenderType renderType = RenderTypes.entityCutout(texture);
		lid.xRot = state.lidRot;
		lock.xRot = state.lidRot;
		submitNodeCollector.submitModelPart(lid, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
		submitNodeCollector.submitModelPart(lock, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
		submitNodeCollector.submitModelPart(bottom, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
		poseStack.popPose();
	}

	public static class RenderState extends BlockEntityRenderState {
		public float lidRot;
		public float yRot = Float.NaN;
	}
}
