package org.patchbukkit.datapack;

import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;
import io.papermc.paper.datapack.DatapackSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.FeatureFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitDatapackManager implements DatapackManager {
    private static final Datapack VANILLA = new Datapack() {
        @Override
        public @NotNull String getName() {
            return "vanilla";
        }

        @Override
        public @NotNull Component getTitle() {
            return Component.text("Vanilla");
        }

        @Override
        public @NotNull Component getDescription() {
            return Component.text("The default data pack");
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(boolean enabled) {}

        @Override
        public @NotNull Component computeDisplayName() {
            return Component.text("Vanilla");
        }

        @Override
        public @NotNull Compatibility getCompatibility() {
            return Compatibility.COMPATIBLE;
        }

        @Override
        public @NotNull Set<FeatureFlag> getRequiredFeatures() {
            return Collections.emptySet();
        }

        @Override
        public @NotNull DatapackSource getSource() {
            return DatapackSource.DEFAULT;
        }
    };

    @Override
    public void refreshPacks() {}

    @Override
    public @Nullable Datapack getPack(@NotNull String name) {
        if ("vanilla".equalsIgnoreCase(name)) return VANILLA;
        return null;
    }

    @Override
    public @NotNull Collection<Datapack> getPacks() {
        return Collections.singletonList(VANILLA);
    }

    @Override
    public @NotNull Collection<Datapack> getEnabledPacks() {
        return Collections.singletonList(VANILLA);
    }
}
