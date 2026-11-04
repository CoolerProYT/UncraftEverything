package net.p3pp3rf1y.sophisticatedbackpacks.crafting;

import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class SmithingBackpackUpgradeRecipe implements SmithingRecipe {
    private final SmithingTransformRecipe compose;
    public SmithingBackpackUpgradeRecipe(SmithingTransformRecipe compose){
        this.compose = compose;
    }

    public ItemStack result(){
        return compose.result.create();
    }

    public Optional<Ingredient> templateIngredient(){
        return compose.templateIngredient();
    }

    public Ingredient baseIngredient(){
        return compose.baseIngredient();
    }

    public Optional<Ingredient> additionIngredient(){
        return compose.additionIngredient();
    }

    public ItemStack assemble(SmithingRecipeInput input){
        return compose.assemble(input);
    }

    public boolean showNotification(){
        return true;
    }

    public String group(){
        return "";
    }

    public RecipeSerializer<? extends SmithingRecipe> getSerializer(){
        return SmithingTransformRecipe.SERIALIZER;
    }

    public PlacementInfo placementInfo(){
        return compose.placementInfo();
    }
}
