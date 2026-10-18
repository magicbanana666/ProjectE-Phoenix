package moze_intel.projecte.client.rendering.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.function.Consumer;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.tools.PETrident;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public class TridentISTER implements SpecialModelRenderer<Identifier> {

	private static final Int2ObjectMap<Identifier> TRIDENT_TEXTURES = Util.make(new Int2ObjectArrayMap<>(2), map -> {
		map.put(EnumMatterType.DARK_MATTER.getMatterTier(), PECore.rl("textures/entity/dark_matter_trident.png"));
		map.put(EnumMatterType.RED_MATTER.getMatterTier(), PECore.rl("textures/entity/red_matter_trident.png"));
	});

	private final TridentModel model;

	private TridentISTER(TridentModel model) {
		this.model = model;
	}

	@Override
	public void submit(@Nullable Identifier texture, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
		Identifier location = texture != null ? texture : TridentModel.TEXTURE;
		//[VanillaCopy] TridentSpecialRenderer#submit but with our texture
		submitNodeCollector.order(0).submitModel(model, Unit.INSTANCE, poseStack, location, lightCoords, overlayCoords, outlineColor, null);
		if (hasFoil) {
			submitNodeCollector.order(1).submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, outlineColor, null);
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		model.root().getExtentsForGui(new PoseStack(), output);
	}

	@Override
	public Identifier extractArgument(ItemStack stack) {
		return getTexture(stack);
	}

	private static Identifier getTexture(ItemStack stack) {
		//Fall back to vanilla's trident texture
		return stack.getItem() instanceof PETrident trident ? getTexture(trident.getMatterTier()) : TridentModel.TEXTURE;
	}

	public static Identifier getTexture(int matterTier) {
		//Fall back to vanilla's trident texture
		return TRIDENT_TEXTURES.getOrDefault(matterTier, TridentModel.TEXTURE);
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<Identifier> {
		public static final MapCodec<TridentISTER.Unbaked> MAP_CODEC = MapCodec.unit(new TridentISTER.Unbaked());

		@Override
		public MapCodec<TridentISTER.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public TridentISTER bake(SpecialModelRenderer.BakingContext context) {
			return new TridentISTER(new TridentModel(context.entityModelSet().bakeLayer(ModelLayers.TRIDENT)));
		}
	}
}
