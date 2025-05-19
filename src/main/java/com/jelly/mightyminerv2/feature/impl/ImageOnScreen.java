package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


public class ImageOnScreen extends AbstractFeature {

    public static ImageOnScreen instance;


    private ResourceLocation imageTexture;


    private int imageWidth;
    private int imageHeight;

    /**
     * Indicates whether the feature is marked as enabled by config or default logic.
     * IMPORTANT: This is independent of whether it is currently running.
     *
     * @return true if the feature is considered enabled
     */
    @Override
    public boolean isEnabled() {
        return MightyMinerConfig.showTheWoman;
    }

    public static ImageOnScreen getInstance() {
        if (instance == null) {
            instance = new ImageOnScreen();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Image Projector";
    }

    /**
     * Starts the feature. Should be overridden by subclasses
     * to initialize or enable feature-specific logic.
     * NOTE: This does NOT automatically set 'enabled' to true.
     */
    @Override
    public void start() {
        if(MightyMinerConfig.showTheWoman){
            return;
        }
        MightyMinerConfig.showTheWoman = true;

    }

    /**
     * Stops the feature and resets internal state.
     * This also disables the feature by setting 'enabled' to false.
     */
    @Override
    public void stop() {
        MightyMinerConfig.showTheWoman = false;
        this.resetStatesAfterStop();
    }

    /**
     * Checks whether the internal timer is currently running
     * and has not yet completed its duration.
     *
     * @return true if the timer is scheduled and still in progress
     */
    @Override
    protected boolean isTimerRunning() {
        return super.isTimerRunning();
    }

    /**
     * Checks whether the internal timer is scheduled and has completed.
     *
     * @return true if the timer is scheduled and has elapsed
     */
    @Override
    protected boolean hasTimerEnded() {
        return super.hasTimerEnded();
    }

    @SubscribeEvent
    protected void onTick(TickEvent.ClientTickEvent event) {


    }

    @SubscribeEvent
    protected void onRender(RenderWorldLastEvent event) {
        super.onRender(event);
    }

    @SubscribeEvent
    protected void onTablistUpdate(UpdateTablistEvent event) {
        super.onTablistUpdate(event);
    }

    @SubscribeEvent
    protected void onOverlayRender(RenderGameOverlayEvent event) {

        if(this.mc.theWorld == null || !this.isEnabled() || event.type != RenderGameOverlayEvent.ElementType.TEXT){

            return;
        }


        // Create the texture if needed (only in render thread)
        if (!textureLoaded && bufferedImage != null) {
            createTexture();

        }

        // Don't render if texture isn't ready
        if (imageTexture == null) {

            return;
        }

        // Save the current state
        GlStateManager.pushMatrix();

        // Set up the rendering state
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F - (MightyMinerConfig.mommyTrans/100));

        // Scale the image if needed
        // Scale factor for the image
        final float scale = 0.2f;
        GlStateManager.scale(scale * getMommyScale(), scale * getMommyScale(), 1.0F);

        // Bind the texture
        this.mc.getTextureManager().bindTexture(imageTexture);

        // Calculate scaled position
        float scaledX = MightyMinerConfig.mommyXCoord * event.resolution.getScaledWidth()/getMommyScale() / scale /100 ;
        float scaledY = MightyMinerConfig.mommyYCoord * event.resolution.getScaledHeight()/getMommyScale() / scale /100 ;

        // Draw the image
        Gui.drawModalRectWithCustomSizedTexture(
                (int)scaledX,
                (int)scaledY,
                0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight
        );

        // Restore the state
        GlStateManager.popMatrix();
    }
    private float getMommyScale(){
        return MightyMinerConfig.mommyScale * MightyMinerConfig.mommyScale /9000;
    }
    private boolean textureLoaded = false;
    private boolean loadingAttempted = false;
    private BufferedImage bufferedImage = null;

    private void loadImage() {
        if (loadingAttempted) return;
        loadingAttempted = true;

        try {
            // We can load the image data outside the OpenGL context
            ResourceLocation imageResourceLocation = new ResourceLocation("mightyminerv2", "textures/egirl.jpeg");
            System.out.println("Attempting to load image from: " + imageResourceLocation);

            // Load the image into memory (this doesn't require OpenGL)
            InputStream imageStream = mc.getResourceManager().getResource(imageResourceLocation).getInputStream();
            if (imageStream == null) {
                System.out.println("Image stream is null!");
                return;
            }

            // Store the BufferedImage for later texture creation
            bufferedImage = ImageIO.read(imageStream);
            if (bufferedImage == null) {
                System.out.println("Failed to read image from stream!");
                return;
            }

            imageWidth = bufferedImage.getWidth();
            imageHeight = bufferedImage.getHeight();

            System.out.println("Image data loaded successfully! Dimensions: " + imageWidth + "x" + imageHeight);
            // We will create the texture later in the render thread

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load image. Error details: " + e.getMessage());
            System.out.println("Make sure the image exists at: assets/mightyminerv2/textures/egirl.jpeg");

            // Try alternative loading method as fallback
            tryAlternativeLoading();
        }
    }

    private void tryAlternativeLoading() {
        try {
            // Try loading using class loader as a fallback
            InputStream resourceAsStream = getClass().getResourceAsStream("/assets/mightyminerv2/textures/egirl.jpeg");
            if (resourceAsStream == null) {
                System.out.println("Alternative loading failed: Resource not found in classpath");
                return;
            }

            // Store the BufferedImage for later texture creation
            bufferedImage = ImageIO.read(resourceAsStream);
            imageWidth = bufferedImage.getWidth();
            imageHeight = bufferedImage.getHeight();

            System.out.println("Image data loaded using alternative method!");
            // We will create the texture later in the render thread

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Alternative loading also failed: " + e.getMessage());
        }
    }

    // This will be called only in the render thread where OpenGL context exists
    private void createTexture() {
        if (textureLoaded || bufferedImage == null) return;

        try {
            // Create a dynamic texture (This must happen in the render thread)
            DynamicTexture texture = new DynamicTexture(bufferedImage);

            // Register the texture
            imageTexture = mc.getTextureManager().getDynamicTextureLocation("mightyminerv2_image", texture);

            textureLoaded = true;
            System.out.println("Texture created successfully in render thread!");

            // Free up memory
            bufferedImage = null;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to create texture: " + e.getMessage());
        }
    }

    @SubscribeEvent
    protected void onWorldLoad(WorldEvent.Load event) {
        loadImage();
    }

    @SubscribeEvent
    protected void onWorldUnload(WorldEvent.Unload event) {
        super.onWorldUnload(event);
    }

    @SubscribeEvent
    protected void onBlockChange(BlockChangeEvent event) {
        super.onBlockChange(event);
    }

    @SubscribeEvent
    protected void onBlockDestroy(BlockDestroyEvent event) {
        super.onBlockDestroy(event);
    }



}
