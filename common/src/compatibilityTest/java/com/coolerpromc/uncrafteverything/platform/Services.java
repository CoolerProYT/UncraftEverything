package com.coolerpromc.uncrafteverything.platform;

import com.coolerpromc.uncrafteverything.platform.services.*;
import com.coolerpromc.uncrafteverything.platform.util.ingredient.ComponentIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

// Test-only platform services. These classes are never packaged in the mod.
public final class Services {
    public static final IPlatformHelper PLATFORM = new IPlatformHelper(){
        public String getPlatformName(){
            return "Compatibility regression";
        }
        public boolean isModLoaded(String id){
            return false;
        }
        public boolean isDevelopmentEnvironment(){
            return true;
        }
        public boolean isClient(){
            return true;
        }
        public java.nio.file.Path configPath(){
            return java.nio.file.Path.of("config");
        }
    };
    public static final INetworkHelper NETWORK = null;
    public static final IIngredientHelper INGREDIENT = new IIngredientHelper(){
        public Ingredient componentIngredient(ComponentIngredient ingredient){
            throw new UnsupportedOperationException();
        }
        public List<Pair<Item, DataComponentPatch>> getItemsFromIngredient(Ingredient ingredient, ItemStack input){
            return ingredient.items().map(holder -> Pair.of(holder.value(), DataComponentPatch.EMPTY)).toList();
        }
        public boolean isVanillaIngredientRecipe(Recipe<?> recipe){
            return true;
        }
    };
}
