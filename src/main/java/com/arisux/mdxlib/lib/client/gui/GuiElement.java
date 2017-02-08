package com.arisux.mdxlib.lib.client.gui;

import javax.vecmath.Vector2d;

import com.arisux.mdxlib.lib.client.GUIElementTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiElement extends GuiButton implements IGuiElement
{
    protected IAction action;
    protected boolean trackInput;
    protected long lastRendered;
    
    public GuiElement(int id, int x, int y, int width, int height, String displayName)
    {
        super(id, x, y, width, height, displayName);
        this.trackInput = true;
    }

    @Override
    public void updateElement()
    {
        if (this.isRendered())
        {
            this.visible = true;
        }
        else
        {
            this.visible = false;
        }
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        this.lastRendered = System.currentTimeMillis();
    }
    
    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }
    
    @Override
    public long lastRenderTime()
    {
        return this.lastRendered;
    }
    
    @Override
    public boolean isRendered()
    {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime > 100 && this.lastRenderTime() > 100)
        {
            return currentTime - this.lastRenderTime() < 500;
        }
        
        return false;
    }
    
    @Override
    public boolean canTrackInput()
    {
        return this.isEnabled() && trackInput && visible;
    }
    
    @Override
    public boolean setTrackInput(boolean trackInput)
    {
        return this.trackInput = trackInput;
    }
    
    public void trackElement()
    {
        GUIElementTracker.instance.track(this);
    }
    
    public void stopTracking()
    {
        GUIElementTracker.instance.stopTracking(this);
    }
    
    public boolean isMouseInElement(Vector2d mousePosition)
    {
        int mouseX = (int) mousePosition.x;
        int mouseY = (int) mousePosition.y;
        
        return this.isEnabled() && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    @Override
    public IAction getAction()
    {
        return action;
    }

    @Override
    public IGuiElement setAction(IAction action)
    {
        this.action = action;
        return this;
    }
}