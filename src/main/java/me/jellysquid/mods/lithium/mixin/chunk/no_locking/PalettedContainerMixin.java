package me.jellysquid.mods.lithium.mixin.chunk.no_locking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.locks.ReentrantLock;

/**
 * The implementation of {@link PalettedContainer} performs a strange check to catch concurrent modification and throws
 * an exception if it occurs. In practice, this never occurs and seems to be a left-over that was added when off-thread
 * chunk generation was being developed and tested. However, a poorly behaved mod could violate the thread-safety
 * contract and cause issues which would not be caught with this patch.
 * <p>
 * This locking (according to some individuals) can impact performance significantly, though my own testing shows it to
 * have only a small impact (about 5% of the world generation time) in Java 11 on an AMD Piledriver-based system running
 * Linux 5.4. As the locking code is platform (and even implementation) specific, it's hard to make an absolute statement
 * about it. My experience has been that the Java locks tend to perform worse on Windows, which is what most players use.
 * <p>
 * This is a rather, well, dumb patch, to remove the locking mechanic. It would likely be wiser to implement a faster
 * check against some counter after modification has occurred to see if something was updated beneath our feet, though
 * that would not be guaranteed to catch the issue.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "()Ljava/util/concurrent/locks/ReentrantLock;"
            )
    )
    private ReentrantLock noLock(Operation<ReentrantLock> original) {
        return null;
    }

    /**
     * @reason Do not check the container's lock
     * @author JellySquid
     */
    @Overwrite
    public void lock() {

    }

    /**
     * @reason Do not check the container's lock
     * @author JellySquid
     */
    @Overwrite
    public void unlock() {

    }
}
