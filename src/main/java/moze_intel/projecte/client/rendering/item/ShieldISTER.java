package moze_intel.projecte.client.rendering.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Consumer;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.items.tools.PEShield;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public class ShieldISTER implements SpecialModelRenderer<ShieldISTER.ShieldData> {

	private static final Identifier DM_SHIELD = PECore.rl("entity/dark_matter_shield");
	private static final Identifier RM_SHIELD = PECore.rl("entity/red_matter_shield");

	private final SpriteGetter sprites;
	private final ShieldModel model;

	private ShieldISTER(SpriteGetter sprites, ShieldModel model) {
		this.sprites = sprites;
		this.model = model;
	}

	@Override
	public void submit(@Nullable ShieldData data, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords,
			boolean hasFoil, int outlineColor) {
		if (data == null) {
			return;
		}
		SpriteId spriteId = new SpriteId(Sheets.SHIELD_SHEET, data.texture());
		BannerPatternLayers patterns = data.patterns();
		DyeColor baseColor = data.baseColor();
		boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
		//[VanillaCopy] ShieldSpecialRenderer#submit but with our texture
		if (hasFoil && !hasPatterns) {
			submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.entitySolidGlint(spriteId.atlasLocation()), lightCoords, overlayCoords, -1,
					sprites.get(spriteId), outlineColor);
		} else {
			submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, spriteId, sprites, outlineColor);
		}
		if (hasPatterns) {
			BannerRenderer.submitPatterns(sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, model, Unit.INSTANCE, false,
					Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns);
			if (hasFoil) {
				submitNodeCollector.order(patterns.layers().size() + 2)
						.submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.patternedShieldGlint(), lightCoords, overlayCoords, -1, sprites.get(spriteId), 0);
			}
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		model.root().getExtentsForGui(new PoseStack(), output);
	}

	@Override
	public ShieldData extractArgument(ItemStack stack) {
		BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		DyeColor baseColor = stack.get(DataComponents.BASE_COLOR);
		Identifier texture = stack.getItem() instanceof PEShield shield && shield.getMatterTier() > 0 ? RM_SHIELD : DM_SHIELD;
		return new ShieldData(patterns, baseColor, texture);
	}

	public record ShieldData(BannerPatternLayers patterns, @Nullable DyeColor baseColor, Identifier texture) {
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<ShieldData> {
		public static final MapCodec<ShieldISTER.Unbaked> MAP_CODEC = MapCodec.unit(new ShieldISTER.Unbaked());

		@Override
		public MapCodec<ShieldISTER.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public ShieldISTER bake(SpecialModelRenderer.BakingContext context) {
			return new ShieldISTER(context.sprites(), new ShieldModel(context.entityModelSet().bakeLayer(ModelLayers.SHIELD)));
		}
	}
}
