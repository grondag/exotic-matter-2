package grondag.xm2.placement;

import java.util.function.BooleanSupplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.fermion.world.IBlockRegion;
import grondag.fermion.world.SingleBlockRegion;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.simulator.jobs.JobManager;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SingleBlockPlacementSpec extends SingleStackPlacementSpec
{
    public SingleBlockPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate()
    {
        if(!this.player.world.isHeightValidAndBlockLoaded(this.pPos.inPos)) return false;

        if(this.isExcavation)
        {
            return !this.player.world.isAir(this.pPos.inPos);
        }
        else
        {
            if(player.world.getBlockState(pPos.inPos).getMaterial().isReplaceable())
            {
                this.outputStack = PlacementHandler.cubicPlacementStack(this);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
    {
        // NOOP - selection mode not meaningful for a single-block region
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
    {
        switch(previewMode)
        {
        case EXCAVATE:
            
            Box box = new Box(this.pPos.inPos);

            // draw edges without depth to show extent of region
            GlStateManager.disableDepthTest();
            GlStateManager.lineWidth(2.0F);
            bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
            // TODO: confirm this is right method - names changed
            WorldRenderer.buildBoxOutline(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 1f);
            tessellator.draw();
            
            // draw sides with depth to better show what parts are unobstructed
            GlStateManager.enableDepthTest();
            
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            // TODO: confirm this is right method - names changed
            WorldRenderer.buildBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue, 0.4f);
            tessellator.draw();
            
            break;
        case PLACE:
            this.drawPlacementPreview(tessellator, bufferBuilder);
            break;
            
        case SELECT:
        case OBSTRUCTED:
        default:
            break;
        
        }
    }

    @Override
    public BooleanSupplier worldTask(ServerPlayerEntity player)
    {
        if(this.isExcavation)
        {
            return new BooleanSupplier() {

                @Override
                public boolean getAsBoolean() {

                    World world = player.world;

                    BlockPos pos = SingleBlockPlacementSpec.this.pPos.inPos;
                    if(pos == null) return false;

                    // is the position inside the world?
                    if(!world.isHeightValidAndBlockLoaded(pos)) return false;

                    BlockState blockState = world.getBlockState(pos);

                    // is the block at the position affected
                    // by this excavation?
                    if(SingleBlockPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(
                            blockState, 
                            world, 
                            pos, 
                            SingleBlockPlacementSpec.this.placedStack(),
                            SingleBlockPlacementSpec.this.isVirtual))
                    {
                        Job job = new Job(RequestPriority.MEDIUM, player);
                        job.setDimension(world.dimension);
                        job.addTask(new ExcavationTask(pos));
                        IDomain domain = DomainManager.instance().getActiveDomain(player);
                        if(domain != null)
                        {
                            domain.getCapability(JobManager.class).addJob(job);
                        }
                    }
                    return false;
                }

            };
        }
        else
        {
            // Placement world task places virtual blocks in the currently active build
            return new BooleanSupplier() {

                @Override
                public boolean getAsBoolean() {

                    Build build = BuildManager.getActiveBuildForPlayer(player);
                    if(build == null || !build.isOpen())
                    {                        
                        String chatMessage = I18n.translate("placement.message.no_build");
                        player.sendMessage(new TranslatableText(chatMessage));
                        return false;
                    }
                    
                    World world = player.world;

                    BlockPos pos = SingleBlockPlacementSpec.this.pPos.inPos;
                    if(pos == null) return false;

                    // is the position inside the world?
                    if(!world.isHeightValidAndBlockLoaded(pos)) return false;

                    BlockState blockState = world.getBlockState(pos);

                    // is the block at the position affected
                    // by this excavation?
                    if(SingleBlockPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(
                            blockState, 
                            world, 
                            pos, 
                            SingleBlockPlacementSpec.this.placedStack(),
                            SingleBlockPlacementSpec.this.isVirtual))
                    {
                        PlacementHandler.placeVirtualBlock(world, SingleBlockPlacementSpec.this.outputStack, player, pos, build);
                    }
                    return false;
                }
            };
        }
    }

    @Override
    public IBlockRegion region()
    {
        return new SingleBlockRegion(this.pPos.inPos);
    }
}