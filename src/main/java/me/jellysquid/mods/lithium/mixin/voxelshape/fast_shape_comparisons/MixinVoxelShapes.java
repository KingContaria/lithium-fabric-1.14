package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_comparisons;

import me.jellysquid.mods.lithium.common.shapes.VoxelShapeEmpty;
import me.jellysquid.mods.lithium.common.shapes.VoxelShapeSimpleCube;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.*;

/**
 * Re-initializes the basic VoxelShapes with our own optimized types and redirects new shape construction.
 */
@Mixin(VoxelShapes.class)
public abstract class MixinVoxelShapes {
    @Mutable
    @Shadow
    @Final
    public static VoxelShape UNBOUNDED;

    @Mutable
    @Shadow
    @Final
    private static VoxelShape FULL_CUBE;

    @Mutable
    @Shadow
    @Final
    private static VoxelShape EMPTY;

    private static final VoxelSet FULL_CUBE_VOXELS;

    static {
        FULL_CUBE_VOXELS = new BitSetVoxelSet(1, 1, 1);
        FULL_CUBE_VOXELS.set(0, 0, 0, true, true);

        UNBOUNDED = new VoxelShapeSimpleCube(FULL_CUBE_VOXELS, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        FULL_CUBE = new VoxelShapeSimpleCube(FULL_CUBE_VOXELS, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        EMPTY = new VoxelShapeEmpty(new BitSetVoxelSet(0, 0, 0));
    }

    /**
     * Responsible for determining whether or not a shape occludes light. This implementation early-exits in some
     * common situations to avoid unnecessary computation.
     *
     * @reason Avoid the expensive shape combination
     * @author JellySquid
     */
    @Overwrite
    public static boolean method_20713(VoxelShape a, VoxelShape b) {
        // At least one shape is a full cube and will match
        if (a == VoxelShapes.fullCube() || b == VoxelShapes.fullCube()) {
            return true;
        }

        boolean ae = a == VoxelShapes.empty() || a.isEmpty();
        boolean be = b == VoxelShapes.empty() || b.isEmpty();

        // If both shapes are empty, they can never overlap
        if (ae && be) {
            return false;
        }

        // Test each shape individually if they're non-empty and fail fast
        if (!ae && VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), a, BooleanBiFunction.ONLY_FIRST)) {
            return false;
        }

        return be || !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), b, BooleanBiFunction.ONLY_FIRST);
    }

    /**
     * @reason Use our optimized shape types
     * @author JellySquid
     */
    @Overwrite
    public static VoxelShape cuboid(Box box) {
        return new VoxelShapeSimpleCube(FULL_CUBE_VOXELS, box.x1, box.y1, box.z1, box.x2, box.y2, box.z2);
    }
}
