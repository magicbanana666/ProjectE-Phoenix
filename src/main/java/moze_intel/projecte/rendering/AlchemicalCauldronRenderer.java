package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCauldronBlockEntity;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Draws what the block model of the Alchemical Cauldron can not: the chalk circles on the floor around it, the surface of its
 * liquid, and the items floating in it.
 */
public class AlchemicalCauldronRenderer implements BlockEntityRenderer<AlchemicalCauldronBlockEntity, AlchemicalCauldronRenderer.RenderState> {

	private static final int FULL_BRIGHT = 0xF000F0;
	private static final Identifier LIQUID = PECore.rl("textures/block/alchemical_cauldron/liquid.png");
	private static final Identifier[] CIRCLES = {
			PECore.rl("textures/block/alchemical_cauldron/circle_1.png"),
			PECore.rl("textures/block/alchemical_cauldron/circle_2.png"),
			PECore.rl("textures/block/alchemical_cauldron/circle_3.png")
	};
	/**
	 * The colours of the three chalks: salt, sulphur and mercury.
	 */
	private static final int[] CHALK = {0xF0F0EC, 0xECCE3C, 0xB8C0D0};
	private static final float SURFACE = 14F / 16F;

	private final BlockEntityRendererProvider.Context context;

	public AlchemicalCauldronRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(@NotNull AlchemicalCauldronBlockEntity cauldron, @NotNull RenderState state, float partialTicks, @NotNull Vec3 cameraPosition,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(cauldron, state, partialTicks, cameraPosition, breakProgress);
		for (ItemStackRenderState item : state.items) {
			item.clear();
		}
		Level level = cauldron.getLevel();
		if (cauldron.isRemoved() || level == null) {
			return;
		}
		state.time = level.getGameTime() + partialTicks;
		state.liquid = cauldron.getLiquid();
		state.brewing = cauldron.isBrewing();
		for (int circle = 0; circle < CauldronRecipe.MAX_CIRCLES; circle++) {
			state.circles[circle] = cauldron.hasCircle(circle + 1);
		}
		int seed = (int) cauldron.getBlockPos().asLong();
		state.itemCount = 0;
		for (ItemStack stack : cauldron.getContents()) {
			if (state.itemCount < state.items.length && !stack.isEmpty()) {
				context.itemModelResolver().updateForTopItem(state.items[state.itemCount], stack, ItemDisplayContext.GROUND, level, null, seed + state.itemCount);
				state.itemCount++;
			}
		}
	}

	@Override
	public void submit(@NotNull RenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
		for (int circle = 0; circle < CauldronRecipe.MAX_CIRCLES; circle++) {
			if (state.circles[circle]) {
				submitCircle(state, poseStack, collector, circle);
			}
		}
		if (state.liquid == CauldronLiquid.EMPTY) {
			return;
		}
		int color = 0xD0000000 | state.liquid.getColor();
		int light = state.liquid == CauldronLiquid.RED_WATER ? FULL_BRIGHT : state.lightCoords;
		poseStack.pushPose();
		poseStack.translate(0, SURFACE, 0);
		submitQuad(poseStack, collector, RenderTypes.entityTranslucent(LIQUID), 2F / 16F, 14F / 16F, color, light);
		poseStack.popPose();
		//The ingredients drift around in the liquid
		for (int i = 0; i < state.itemCount; i++) {
			float angle = state.time * (state.brewing ? 0.06F : 0.015F) + i * Mth.TWO_PI / state.itemCount;
			float radius = state.itemCount == 1 ? 0 : 0.22F;
			poseStack.pushPose();
			poseStack.translate(0.5F + Mth.cos(angle) * radius, SURFACE + 0.02F + Mth.sin(state.time / 9F + i) * 0.015F, 0.5F + Mth.sin(angle) * radius);
			poseStack.mulPose(Axis.YP.rotation(-angle));
			poseStack.mulPose(Axis.XP.rotationDegrees(90));
			poseStack.scale(0.35F, 0.35F, 0.35F);
			state.items[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	private static void submitCircle(RenderState state, PoseStack poseStack, SubmitNodeCollector collector, int circle) {
		//The inner and outer circle turn one way, the middle one the other
		float speed = (state.brewing ? 0.02F : 0.003F) * (circle % 2 == 0 ? 1 : -1);
		int alpha = state.brewing ? 0xF0 : 0xB0;
		float radius = circle + 1.5F;
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.01F + circle * 0.002F, 0.5F);
		poseStack.mulPose(Axis.YP.rotation(state.time * speed));
		poseStack.translate(-0.5F, 0, -0.5F);
		submitQuad(poseStack, collector, RenderTypes.entityTranslucentEmissive(CIRCLES[circle]), 0.5F - radius, 0.5F + radius, alpha << 24 | CHALK[circle], FULL_BRIGHT);
		poseStack.popPose();
	}

	/**
	 * A flat square, seen from above and below, from min to max on both horizontal axes.
	 */
	private static void submitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, float min, float max, int color, int light) {
		collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
			buffer.addVertex(pose, min, 0, min).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, min, 0, max).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, max, 0, max).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, max, 0, min).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, max, 0, min).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, max, 0, max).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, min, 0, max).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, min, 0, min).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, -1, 0);
		});
	}

	@NotNull
	@Override
	public AABB getRenderBoundingBox(@NotNull AlchemicalCauldronBlockEntity cauldron) {
		BlockPos pos = cauldron.getBlockPos();
		int reach = CauldronRecipe.MAX_CIRCLES + 1;
		return new AABB(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + 1 + reach, pos.getY() + 1, pos.getZ() + 1 + reach);
	}

	public static class RenderState extends BlockEntityRenderState {

		public final ItemStackRenderState[] items = new ItemStackRenderState[AlchemicalCauldronBlockEntity.MAX_STACKS];
		public final boolean[] circles = new boolean[CauldronRecipe.MAX_CIRCLES];
		public CauldronLiquid liquid = CauldronLiquid.EMPTY;
		public int itemCount;
		public float time;
		public boolean brewing;

		public RenderState() {
			for (int i = 0; i < items.length; i++) {
				items[i] = new ItemStackRenderState();
			}
		}
	}
}
