package net.p3pp3rf1y.sophisticatedbackpacks.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

// NeoForge's wrapper is a CraftingRecipe, not a ShapedRecipe subclass.
public class BasicBackpackRecipe implements CraftingRecipe {
    private final ShapedRecipe compose;
    public BasicBackpackRecipe(ShapedRecipe compose){
        this.compose = compose;
    }

    public ShapedRecipe getCompose(){
        return compose;
    }

    public boolean matches(CraftingInput input, Level level){
        return compose.matches(input, level);
    }

    public ItemStack assemble(CraftingInput input){
        return compose.assemble(input);
    }

    public boolean showNotification(){
        return true;
    }

    public String group(){
        return "";
    }

    public RecipeSerializer<? extends CraftingRecipe> getSerializer(){
        return ShapedRecipe.SERIALIZER;
    }

    public PlacementInfo placementInfo(){
        return compose.placementInfo();
    }

    public CraftingBookCategory category(){
        return CraftingBookCategory.MISC;
    }
}
