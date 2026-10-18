package moze_intel.projecte.gameObjs.items.tools;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.IMatterType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;

public class PEShield extends ShieldItem {

    private final IMatterType matterType;

    public PEShield(IMatterType matterType, Properties props) {
        super(props.rarity(Rarity.RARE)
              .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        );
        this.matterType = matterType;
    }

    public int getMatterTier() {
        return matterType.getMatterTier();
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, T entity, @NotNull Consumer<ItemStack> onBroken) {
        return 0;
    }
}
