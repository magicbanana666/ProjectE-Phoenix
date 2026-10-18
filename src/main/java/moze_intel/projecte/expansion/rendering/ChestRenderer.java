package moze_intel.projecte.expansion.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moze_intel.projecte.PECore;
import moze_intel.projecte.expansion.block.BlockAdvancedAlchemicalChest;
import moze_intel.projecte.expansion.block.BlockCondenserMK3;
import moze_intel.projecte.expansion.util.IChestLike;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// lovingly "lifted" from ProjectE
// https://github.com/sinkillerj/ProjectE/blob/98aee771bdb09beecf51b5608938d93de6f1afb6/src/main/java/moze_intel/projecte/rendering/ChestRenderer.java
//26.1: rendering a block entity goes through createRenderState/extractRenderState/submit, MultiBufferSource is gone
public class ChestRenderer<BE extends BlockEntity & IChestLike> implements BlockEntityRenderer<BE, ChestRenderer.RenderState> {
	private final ModelPart lid;
	private final ModelPart bottom;
	private final ModelPart lock;

	private final BlockEntityType<BE> type;

	public ChestRenderer(BlockEntityRendererProvider.Context context, BlockEntityType<BE> type) {
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
	public void extractRenderState(@NotNull BE chest, @NotNull RenderState state, float partialTick, @NotNull Vec3 cameraPosition,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(chest, state, partialTick, cameraPosition, breakProgress);
		//Note: texture == null means "we have nothing to draw", which is also the case for the default NaN rotation
		state.texture = null;
		state.yRot = Float.NaN;
		if (chest.getType().equals(this.type) && chest.getLevel() != null && !chest.isRemoved()) {
			BlockState blockState = chest.getLevel().getBlockState(chest.getBlockPos());
			state.texture = getTexture(blockState.getBlock());
			if (state.texture != null) {
				state.yRot = -blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
			}
		}
		float lidAngle = 1.0F - chest.getOpenNess(partialTick);
		lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
		state.lidRot = -(lidAngle * Mth.HALF_PI);
	}

	@Override
	public void submit(@NotNull RenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector,
			@NotNull CameraRenderState camera) {
		if (state.texture == null || Float.isNaN(state.yRot)) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		poseStack.rotate(Axis.YP.rotationDegrees(state.yRot));
		poseStack.translate(-0.5D, -0.5D, -0.5D);
		RenderType renderType = RenderTypes.entityCutout(state.texture);
		lid.xRot = state.lidRot;
		lock.xRot = state.lidRot;
		submitNodeCollector.submitModelPart(lid, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
		submitNodeCollector.submitModelPart(lock, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
		submitNodeCollector.submitModelPart(bottom, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
		poseStack.popPose();
	}

	private @Nullable Identifier getTexture(Block block) {
		return switch (block) {
			case BlockAdvancedAlchemicalChest chest -> PECore.rl(String.format("textures/block/advanced_alchemical_chest/%s.png", chest.getColor().getName()));
			case BlockCondenserMK3 ignored -> PECore.rl("textures/block/condenser_mk1.png");
			default -> null;
		};
	}

	public static class RenderState extends BlockEntityRenderState {
		public float lidRot;
		public float yRot = Float.NaN;
		public @Nullable Identifier texture;
	}
}
