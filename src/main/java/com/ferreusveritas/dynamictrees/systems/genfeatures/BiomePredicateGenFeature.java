package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BiomePredicate;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class BiomePredicateGenFeature extends GenFeature implements IPostGenFeature {

	public static final GenFeatureProperty<BiomePredicate> BIOME_PREDICATE = GenFeatureProperty.createProperty("biome_predicate", BiomePredicate.class);
	public static final GenFeatureProperty<ConfiguredGenFeature<?>> GEN_FEATURE = GenFeatureProperty.createProperty("gen_feature", ConfiguredGenFeature.class);
	public static final GenFeatureProperty<Boolean> ONLY_WORLD_GEN = GenFeatureProperty.createBooleanProperty("only_world_gen");

	public BiomePredicateGenFeature(ResourceLocation registryName) {
		super(registryName, BIOME_PREDICATE, GEN_FEATURE, ONLY_WORLD_GEN);
	}

	@Override
	protected ConfiguredGenFeature<?> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(BIOME_PREDICATE, i -> true).with(GEN_FEATURE, null).with(ONLY_WORLD_GEN, false);
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		ConfiguredGenFeature<?> configuredGenFeatureToPlace = configuredGenFeature.get(GEN_FEATURE);

		if (configuredGenFeature == null) // If the gen feature was not set, do nothing.
			return false;

		GenFeature genFeatureToPlace = configuredGenFeatureToPlace.getGenFeature();

		if (genFeatureToPlace instanceof IPostGenFeature && !(configuredGenFeature.get(ONLY_WORLD_GEN) && !worldGen) && configuredGenFeature.get(BIOME_PREDICATE).test(biome)) {
			return ((IPostGenFeature) genFeatureToPlace).postGeneration(configuredGenFeatureToPlace, world, rootPos, species, biome, radius, endPoints, safeBounds, initialDirtState, seasonValue, seasonFruitProductionFactor);
		}
		return false;
	}

}
