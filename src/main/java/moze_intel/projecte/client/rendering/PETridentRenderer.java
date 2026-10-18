package moze_intel.projecte.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moze_intel.projecte.client.rendering.item.TridentISTER;
import moze_intel.projecte.gameObjs.entity.PETridentEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

public class PETridentRenderer extends ThrownTridentRenderer {

	private final TridentModel model;

	public PETridentRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new TridentModel(context.bakeLayer(ModelLayers.TRIDENT));
	}

	@Override
	public PETridentRenderState createRenderState() {
		return new PETridentRenderState();
	}

	@Override
	public void extractRenderState(ThrownTrident entity, ThrownTridentRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		if (state instanceof PETridentRenderState peState) {
			peState.texture = entity instanceof PETridentEntity peTrident ? TridentISTER.getTexture(peTrident.getMatterTier())
					: ThrownTridentRenderer.TRIDENT_LOCATION;
		}
	}

	@Override
	public void submit(ThrownTridentRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		// [VanillaCopy] Vanilla's ThrownTridentRenderer#submit but with our per-entity texture
		Identifier texture = state instanceof PETridentRenderState peState ? peState.texture : ThrownTridentRenderer.TRIDENT_LOCATION;
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
		poseStack.rotateDegrees(Axis.ZP, state.xRot + 90.0F);
		if (state.isFoil) {
			submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, RenderTypes.entitySolidGlint(texture), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		} else {
			submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		}
		poseStack.popPose();
		this.submitNameDisplay(state, poseStack, submitNodeCollector, camera);
	}

	public static class PETridentRenderState extends ThrownTridentRenderState {
		public Identifier texture = ThrownTridentRenderer.TRIDENT_LOCATION;
	}
}
