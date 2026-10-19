package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.CrucibleStructure;
import moze_intel.projecte.gameObjs.block_entities.AlchemicalCrucibleBlockEntity;
import moze_intel.projecte.gameObjs.blocks.CruciblePart;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.customRecipes.athanor.OpusStage;
import moze_intel.projecte.gameObjs.customRecipes.athanor.RuneColor;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Draws the ritual of the Athanor: the catalyst floats in the centre and the ingredients float around it in a regular shape, above a glowing rune circle that covers the dais. During the ritual a beam of light runs
 * from the apex down to the catalyst. How everything moves, its colour, and which variant of the stone model is shown,
 * depends on the stage of the ritual.
 */
public class AlchemicalCrucibleRenderer implements BlockEntityRenderer<AlchemicalCrucibleBlockEntity, AlchemicalCrucibleRenderer.RenderState> {

	private static final int FULL_BRIGHT = 0xF000F0;
	private static final float CENTRE_Y = 1.5F;
	private static final Identifier CIRCLE = PECore.rl("textures/block/crucible/circle.png");
	/**
	 * The rune circle covers the three by three dais around the crucible.
	 */
	private static final float CIRCLE_RADIUS = 1.5F;

	private final BlockEntityRendererProvider.Context context;

	public AlchemicalCrucibleRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(@NotNull AlchemicalCrucibleBlockEntity core, @NotNull RenderState state, float partialTicks, @NotNull Vec3 cameraPosition,
			ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(core, state, partialTicks, cameraPosition, breakProgress);
		state.catalyst.clear();
		state.result.clear();
		for (ItemStackRenderState stone : state.stones) {
			stone.clear();
		}
		Level level = core.getLevel();
		if (core.isRemoved() || level == null) {
			return;
		}
		BlockPos pos = core.getBlockPos();
		int seed = (int) pos.asLong();
		double time = level.getGameTime() + partialTicks;
		OpusStage stage = core.getStage();
		state.stage = stage;
		state.rune = core.getRune();
		state.time = (float) time;
		state.fraction = core.getProgressFraction();
		state.radius = core.getOrbitRadius();
		state.glowing = stage.isActive() && !core.isStalled() || stage == OpusStage.COMPLETE;
		state.formed = core.isFormed();
		String variant = stage.stoneModel();
		int count = 0;
		for (int i = 0; i < AthanorRecipe.MAX_INPUTS; i++) {
			if (!core.getInput(i).isEmpty()) {
				count++;
			}
		}
		int index = 0;
		for (int i = 0; i < AthanorRecipe.MAX_INPUTS; i++) {
			ItemStack stone = core.getInput(i);
			if (!stone.isEmpty()) {
				//False Philosopher's Stones show the stage they are in, other ingredients look the same throughout
				if (variant != null && stone.is(PEItems.FALSE_PHILOSOPHERS_STONE)) {
					stone = stone.copy();
					stone.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(variant), List.of()));
				}
				context.itemModelResolver().updateForTopItem(state.stones[i], stone, ItemDisplayContext.GROUND, level, null, seed + i);
				state.angles[i] = (float) core.getInputAngle(index++, count, time);
			}
		}
		if (!core.getCatalystStack().isEmpty()) {
			context.itemModelResolver().updateForTopItem(state.catalyst, core.getCatalystStack(), ItemDisplayContext.GROUND, level, null, seed + 5);
		}
		if (!core.getResult().isEmpty()) {
			context.itemModelResolver().updateForTopItem(state.result, core.getResult(), ItemDisplayContext.GROUND, level, null, seed + 6);
		}
		//While the player holds a block of the Athanor, every block that is still missing floats where it belongs
		state.ghostCount = 0;
		Player player = Minecraft.getInstance().player;
		if (!core.isFormed() && player != null && isAthanorBlock(player.getMainHandItem())) {
			for (CrucibleStructure.Part part : CrucibleStructure.getParts()) {
				if (!part.matches(level.getBlockState(pos.offset(part.offset())))) {
					if (state.ghostCount == state.ghosts.size()) {
						state.ghosts.add(new ItemStackRenderState());
						state.ghostOffsets.add(BlockPos.ZERO);
					}
					context.itemModelResolver().updateForTopItem(state.ghosts.get(state.ghostCount), new ItemStack(part.block().getBlock()), ItemDisplayContext.GROUND,
							level, null, seed + 7 + state.ghostCount);
					state.ghostOffsets.set(state.ghostCount, part.offset());
					state.ghostCount++;
				}
			}
		}
	}

	private static boolean isAthanorBlock(ItemStack stack) {
		return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CruciblePart;
	}

	@Override
	public void submit(@NotNull RenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState camera) {
		for (int i = 0; i < state.ghostCount; i++) {
			BlockPos offset = state.ghostOffsets.get(i);
			poseStack.pushPose();
			poseStack.translate(offset.getX() + 0.5F, offset.getY() + 0.25F + Mth.sin(state.time / 15F + i) * 0.05F, offset.getZ() + 0.5F);
			poseStack.mulPose(Axis.YP.rotation(state.time / 30F));
			poseStack.scale(0.6F, 0.6F, 0.6F);
			state.ghosts.get(i).submit(poseStack, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		int light = state.glowing ? FULL_BRIGHT : state.lightCoords;
		float time = state.time;
		if (state.formed) {
			submitCircle(state, poseStack, collector);
			if (state.glowing) {
				//The beam of the apex, from just above the pedestal up to the crystal of the apex four blocks higher
				BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0F, time % 40F, 1, 3,
						runeColor(state.rune), state.stage == OpusStage.RUBEDO ? 0.04F + 0.06F * state.fraction : 0.04F, 0.08F);
			}
		}
		float bob = Mth.sin(time / 12F) * 0.05F;
		if (!state.catalyst.isEmpty()) {
			//The catalyst spins faster as the ritual goes on
			float spin = switch (state.stage) {
				case IDLE, COMPLETE -> time / 40F;
				case NIGREDO -> time / 60F;
				case CAUDA_PAVONIS, ALBEDO -> time / 30F;
				case CITRINITAS -> time / 15F;
				case RUBEDO -> time / Mth.lerp(state.fraction, 10F, 2F);
			};
			poseStack.pushPose();
			poseStack.translate(0.5F, CENTRE_Y + bob, 0.5F);
			float scale = state.stage == OpusStage.RUBEDO ? Mth.lerp(state.fraction, 0.6F, 0.3F) : 0.6F;
			poseStack.scale(scale, scale, scale);
			poseStack.mulPose(Axis.YP.rotation(spin));
			state.catalyst.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		for (int i = 0; i < AthanorRecipe.MAX_INPUTS; i++) {
			ItemStackRenderState stone = state.stones[i];
			if (stone.isEmpty()) {
				continue;
			}
			float angle = state.angles[i];
			float x = 0.5F + Mth.cos(angle) * state.radius;
			float z = 0.5F + Mth.sin(angle) * state.radius;
			float y = CENTRE_Y - 0.1F + Mth.sin(time / 10F + i * 2.1F) * 0.06F;
			if (state.stage == OpusStage.NIGREDO) {
				//The false nature of the stones is breaking apart
				x += Mth.sin(time * 3.1F + i * 7) * 0.012F;
				z += Mth.cos(time * 2.7F + i * 5) * 0.012F;
			} else if (state.stage == OpusStage.RUBEDO) {
				y = Mth.lerp(state.fraction, y, CENTRE_Y);
			}
			poseStack.pushPose();
			poseStack.translate(x, y, z);
			poseStack.scale(0.45F, 0.45F, 0.45F);
			poseStack.mulPose(Axis.YP.rotation(-angle));
			stone.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		if (!state.result.isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(0.5F, CENTRE_Y + 0.15F + bob, 0.5F);
			poseStack.scale(0.8F, 0.8F, 0.8F);
			poseStack.mulPose(Axis.YP.rotation(time / 20F));
			state.result.submit(poseStack, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	private static void submitCircle(RenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
		float spin = switch (state.stage) {
			case IDLE, COMPLETE -> state.time * 0.002F;
			case NIGREDO -> state.time * 0.004F;
			case CAUDA_PAVONIS, ALBEDO -> state.time * 0.008F;
			case CITRINITAS -> state.time * 0.015F;
			case RUBEDO -> state.time * (0.02F + 0.04F * state.fraction);
		};
		int color = (state.glowing ? 0xE0000000 : 0x90000000) | (runeColor(state.rune) & 0xFFFFFF);
		poseStack.pushPose();
		//Just above the top of the dais, which is two pixels high
		poseStack.translate(0.5F, 2.05F / 16F, 0.5F);
		poseStack.mulPose(Axis.YP.rotation(spin));
		collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(CIRCLE), (pose, buffer) -> {
			float r = CIRCLE_RADIUS;
			//Both sides, so the circle can also be seen from below the rim of the dais
			buffer.addVertex(pose, -r, 0, -r).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, -r, 0, r).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, r, 0, r).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, r, 0, -r).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
			buffer.addVertex(pose, r, 0, -r).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, r, 0, r).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, -r, 0, r).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, -1, 0);
			buffer.addVertex(pose, -r, 0, -r).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, -1, 0);
		});
		poseStack.popPose();
	}

	/**
	 * @return the colour of the circle and the beam, the same as the runes. A formed but idle Athanor glows red, as in the concept art.
	 */
	private static int runeColor(RuneColor rune) {
		return 0xFF000000 | (rune == RuneColor.OFF ? RuneColor.FORMED.getRgb() : rune.getRgb());
	}

	@NotNull
	@Override
	public AABB getRenderBoundingBox(@NotNull AlchemicalCrucibleBlockEntity core) {
		BlockPos pos = core.getBlockPos();
		return new AABB(pos.getX() - 2, pos.getY() - 1, pos.getZ() - 2, pos.getX() + 3, pos.getY() + 5, pos.getZ() + 3);
	}

	public static class RenderState extends BlockEntityRenderState {

		public final ItemStackRenderState catalyst = new ItemStackRenderState();
		public final ItemStackRenderState result = new ItemStackRenderState();
		public final ItemStackRenderState[] stones = {new ItemStackRenderState(), new ItemStackRenderState(), new ItemStackRenderState(),
				new ItemStackRenderState(), new ItemStackRenderState()};
		public final float[] angles = new float[AthanorRecipe.MAX_INPUTS];
		public OpusStage stage = OpusStage.IDLE;
		public RuneColor rune = RuneColor.FORMED;
		public final List<ItemStackRenderState> ghosts = new java.util.ArrayList<>();
		public final List<BlockPos> ghostOffsets = new java.util.ArrayList<>();
		public int ghostCount;
		public float time;
		public float fraction;
		public float radius;
		public boolean glowing;
		public boolean formed;
	}
}
