package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.items.PhilosophersStone.PhilosophersStoneMode;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.Constants;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class TransmutationRenderingOverlay implements GuiLayer {

	private final Minecraft mc = Minecraft.getInstance();
	@Nullable
	private Block transmutationResult;
	private long lastGameTime;

	public TransmutationRenderingOverlay() {
		NeoForge.EVENT_BUS.addListener(this::onBlockOutline);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		if (!mc.gui.hud.isHidden() && transmutationResult != null) {
			if (transmutationResult instanceof LiquidBlock liquidBlock) {
				FluidState fluidState = liquidBlock.fluid.defaultFluidState();
				FluidModel fluidModel = mc.getModelManager().getFluidStateModelSet().get(fluidState);
				int color = fluidModel.fluidTintSource() != null ? fluidModel.fluidTintSource().color(fluidState) : -1;
				TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 1, 1, 16, 16, color);
			} else {
				//Just render it normally instead of with the given model as some block's don't render properly then as an item
				// for example glass panes
				graphics.item(new ItemStack(transmutationResult), 1, 1);
			}
			long gameTime = mc.level == null ? 0 : mc.level.getGameTime();
			if (lastGameTime != gameTime) {
				//If the game time changed, so we aren't actually still hovering a block set our
				// result to null. We do this after rendering it just in case there is a single
				// frame where this may actually be valid based on the order the events are fired
				transmutationResult = null;
				lastGameTime = gameTime;
			}
		}
	}

	private void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
		Camera activeRenderInfo = event.getCamera();
		if (!(activeRenderInfo.entity() instanceof Player player)) {
			return;
		}
		Level level = player.level();
		lastGameTime = level.getGameTime();
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			stack = player.getOffhandItem();
		}
		if (stack.isEmpty() || !stack.is(PEItems.PHILOSOPHERS_STONE)) {
			transmutationResult = null;
			return;
		}
		boolean isSneaking = player.isSecondaryUseActive();
		PhilosophersStone philoStone = (PhilosophersStone) stack.getItem();
		//Note: We use the philo stone's ray trace instead of the event's ray trace as we want to make sure that we
		// can properly take fluid into account/ignore it when needed
		BlockHitResult rtr = philoStone.getHitBlock(level, player, isSneaking);
		if (rtr.getType() == HitResult.Type.BLOCK) {
			int charge = philoStone.getCharge(stack);
			PhilosophersStoneMode mode = philoStone.getMode(stack);
			Object2ReferenceMap<BlockPos, BlockState> changes = PhilosophersStone.getChanges(level, rtr.getBlockPos(), rtr.getDirection(), player.getDirection(),
					isSneaking, mode, charge);
			if (changes.isEmpty()) {
				transmutationResult = null;
			} else {
				transmutationResult = changes.values().iterator().next().getBlock();
				Vec3 viewPosition = activeRenderInfo.position();
				float alpha = ProjectEConfig.client.pulsatingOverlay.get() ? getPulseProportion() * 0.60F : 0.35F;
				int alphaColor = (int) (alpha * 255);
				CollisionContext selectionContext = event.getCollisionContext();
				//Extract all the data we need so that we don't capture the level in the renderer
				List<OutlineShape> shapes = new ArrayList<>();
				for (BlockPos pos : changes.keySet()) {
					BlockState state = level.getBlockState(pos);
					if (!state.isAir()) {
						VoxelShape shape = state.getShape(level, pos, selectionContext);
						if (!shape.isEmpty()) {
							shapes.add(new OutlineShape(pos, shape));
						}
					}
				}
				event.addCustomRenderer((outlineState, collector, pose, levelRenderState) -> {
					//Note: Since 26.2 this is only invoked once per frame, and the geometry is submitted instead of being
					// written straight into a buffer
					for (OutlineShape outlineShape : shapes) {
						BlockPos pos = outlineShape.pos();
						pose.pushPose();
						//Shift by view position here so that we don't have floating point issues at large values
						pose.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);
						collector.submitCustomGeometry(pose, PERenderType.TRANSMUTATION_OVERLAY, (posed, builder) ->
								outlineShape.shape().forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
									for (Direction value : Constants.DIRECTIONS) {
										renderFace(posed, builder, value, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, alphaColor);
									}
								}));
						pose.popPose();
					}
					//Don't suppress the vanilla outline of the block we are looking at
					return false;
				});
			}
		} else {
			transmutationResult = null;
		}
	}

	private static void renderFace(PoseStack.Pose pose, VertexConsumer builder, Direction direction,
			float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int alpha) {
		switch (direction) {
			case DOWN -> quad(pose, builder, alpha,
					minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ);
			case UP -> quad(pose, builder, alpha,
					minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
			case NORTH -> quad(pose, builder, alpha,
					minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ);
			case SOUTH -> quad(pose, builder, alpha,
					minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
			case WEST -> quad(pose, builder, alpha,
					minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
			case EAST -> quad(pose, builder, alpha,
					maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ);
		}
	}

	private static void quad(PoseStack.Pose pose, VertexConsumer builder, int alpha,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float x3, float y3, float z3, float x4, float y4, float z4) {
		builder.addVertex(pose, x1, y1, z1).setColor(255, 255, 255, alpha);
		builder.addVertex(pose, x2, y2, z2).setColor(255, 255, 255, alpha);
		builder.addVertex(pose, x3, y3, z3).setColor(255, 255, 255, alpha);
		builder.addVertex(pose, x4, y4, z4).setColor(255, 255, 255, alpha);
	}

	private float getPulseProportion() {
		return (float) (0.5F * Math.sin(System.currentTimeMillis() / 350.0) + 0.5F);
	}

	/**
	 * The shape of a block position that needs an outline rendered for it.
	 */
	private record OutlineShape(BlockPos pos, VoxelShape shape) {
	}
}
