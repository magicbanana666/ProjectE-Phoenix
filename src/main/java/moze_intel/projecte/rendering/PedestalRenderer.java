package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import moze_intel.projecte.gameObjs.block_entities.DMPedestalBlockEntity;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class PedestalRenderer implements BlockEntityRenderer<DMPedestalBlockEntity, PedestalRenderer.RenderState> {

	private final BlockEntityRendererProvider.Context context;

	public PedestalRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(@NotNull DMPedestalBlockEntity pedestal, @NotNull RenderState state, float partialTicks, @NotNull Vec3 cameraPosition,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTicks, cameraPosition, breakProgress);
		state.item.clear();
		state.effectBounds = null;
		state.bobY = 0.0F;
		state.angle = 0.0F;
		Level level = pedestal.getLevel();
		if (!pedestal.isRemoved() && level != null) {
			BlockPos pos = pedestal.getBlockPos();
			if (Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
				state.effectBounds = pedestal.getEffectBounds().move(-pos.getX(), -pos.getY(), -pos.getZ());
			}
			ItemStack stack = pedestal.getInventory().getStackInSlot(0);
			if (!stack.isEmpty()) {
				long gameTime = level.getGameTime();
				state.bobY = Mth.sin((gameTime + partialTicks) / 10.0F) * 0.1F + 0.1F;
				state.angle = (gameTime + partialTicks) / SharedConstants.TICKS_PER_SECOND;
				this.context.itemModelResolver().updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, level, null, (int) pos.asLong());
			}
		}
	}

	@Override
	public void submit(@NotNull RenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector,
			@NotNull CameraRenderState camera) {
		AABB effectBounds = state.effectBounds;
		if (effectBounds != null) {
			submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, builder) -> renderLineBox(pose, builder, effectBounds));
		}
		if (!state.item.isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(0.5D, 0.7D + state.bobY, 0.5D);
			poseStack.scale(0.75F, 0.75F, 0.75F);
			poseStack.rotate(Axis.YP.rotation(state.angle));
			state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	// [VanillaCopy] Vanilla's LevelRenderer#renderLineBox was removed, so draw the box's edges manually
	private static void renderLineBox(PoseStack.Pose pose, VertexConsumer builder, AABB bounds) {
		float minX = (float) bounds.minX;
		float minY = (float) bounds.minY;
		float minZ = (float) bounds.minZ;
		float maxX = (float) bounds.maxX;
		float maxY = (float) bounds.maxY;
		float maxZ = (float) bounds.maxZ;
		line(pose, builder, minX, minY, minZ, maxX, minY, minZ);
		line(pose, builder, minX, maxY, minZ, maxX, maxY, minZ);
		line(pose, builder, minX, minY, maxZ, maxX, minY, maxZ);
		line(pose, builder, minX, maxY, maxZ, maxX, maxY, maxZ);
		line(pose, builder, minX, minY, minZ, minX, minY, maxZ);
		line(pose, builder, maxX, minY, minZ, maxX, minY, maxZ);
		line(pose, builder, minX, maxY, minZ, minX, maxY, maxZ);
		line(pose, builder, maxX, maxY, minZ, maxX, maxY, maxZ);
		line(pose, builder, minX, minY, minZ, minX, maxY, minZ);
		line(pose, builder, maxX, minY, minZ, maxX, maxY, minZ);
		line(pose, builder, minX, minY, maxZ, minX, maxY, maxZ);
		line(pose, builder, maxX, minY, maxZ, maxX, maxY, maxZ);
	}

	private static void line(PoseStack.Pose pose, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2) {
		Vector3f normal = new Vector3f(x2 - x1, y2 - y1, z2 - z1).normalize();
		builder.addVertex(pose, x1, y1, z1).setColor(255, 0, 255, 255).setNormal(pose, normal).setLineWidth(1.0F);
		builder.addVertex(pose, x2, y2, z2).setColor(255, 0, 255, 255).setNormal(pose, normal).setLineWidth(1.0F);
	}

	@NotNull
	@Override
	public AABB getRenderBoundingBox(@NotNull DMPedestalBlockEntity pedestal) {
		if (Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
			return pedestal.getEffectBounds();
		}
		return BlockEntityRenderer.super.getRenderBoundingBox(pedestal);
	}

	public static class RenderState extends BlockEntityRenderState {
		public final ItemStackRenderState item = new ItemStackRenderState();
		@Nullable
		public AABB effectBounds;
		public float bobY;
		public float angle;
	}
}
