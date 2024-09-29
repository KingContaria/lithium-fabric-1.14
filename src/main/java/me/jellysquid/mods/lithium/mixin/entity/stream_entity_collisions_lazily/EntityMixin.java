package me.jellysquid.mods.lithium.mixin.entity.stream_entity_collisions_lazily;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /**
     * Redirect to try to collide with blocks first, so the entity stream doesn't have to be used when block collisions cancel the whole movement already.
     */
    @WrapOperation(
            method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Lnet/minecraft/block/ShapeContext;Lnet/minecraft/util/collection/ReusableStream;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;"
            )
    )
    private static Stream<VoxelShape> reorderStreams_BlockCollisionsFirst(Stream<? extends VoxelShape> entityShapes, Stream<? extends VoxelShape> blockShapes, Operation<Stream<VoxelShape>> original) {
        return original.call(blockShapes, entityShapes);
    }
}
