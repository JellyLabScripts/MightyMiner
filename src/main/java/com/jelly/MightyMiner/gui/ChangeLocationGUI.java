package com.jelly.MightyMiner.gui;

import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

public class ChangeLocationGUI extends GuiScreen {

    private Callable<Rectangle> drawFunction;
    private BiFunction<Integer, Integer, Void> saveFunction;
    private static Rectangle startLocation;
    private int xOffset;
    private int yOffset;

    private boolean isDragging = false;

    public static void open(Callable<Rectangle> drawFunction, BiFunction<Integer, Integer, Void> saveFunction) {
        ChangeLocationGUI gui = new ChangeLocationGUI();
        gui.drawFunction = drawFunction;
        gui.saveFunction = saveFunction;
        try {
            startLocation = drawFunction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Minecraft.getMinecraft().displayGuiScreen(gui);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawRect(0, 0, width, height, 0x80000000);
        onMouseMove();

        try {
            Rectangle point = startLocation;
            drawFunction.call();
            if (isDragging || (mouseX >= point.x && mouseX <= point.x + point.width && mouseY >= point.y && mouseY <= point.y + point.height)) {
                drawRect(point.x, point.y, point.x + point.width, point.y + point.height, new Color(44, 44, 44, 200).getRGB());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        Rectangle point = startLocation;
        if (mouseX >= point.x && mouseX <= point.x + point.width && mouseY >= point.y && mouseY <= point.y + point.height) {
            isDragging = true;
            xOffset = mouseX - point.x;
            yOffset = mouseY - point.y;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        isDragging = false;
        MightyMiner.config.markDirty();
        MightyMiner.config.writeData();
    }

    private void onMouseMove() {
        if (!isDragging) return;

        int x = Mouse.getEventX() * width / mc.displayWidth - xOffset;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1 - yOffset;

        startLocation.x = x;
        startLocation.y = y;

        if (startLocation.x < 0) startLocation.x = 0;
        if (startLocation.y < 0) startLocation.y = 0;
        if (startLocation.x + startLocation.width > width) startLocation.x = width - startLocation.width;
        if (startLocation.y + startLocation.height > height) startLocation.y = height - startLocation.height;

        saveFunction.apply(startLocation.x, startLocation.y);
    }
}
