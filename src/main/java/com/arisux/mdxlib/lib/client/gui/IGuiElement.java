package com.arisux.mdxlib.lib.client.gui;

import javax.vecmath.Vector2d;

import com.arisux.mdxlib.lib.client.render.Draw;
import com.arisux.mdxlib.lib.client.render.OpenGL;
import com.arisux.mdxlib.lib.client.render.Screen;

public interface IGuiElement
{
    public boolean isEnabled();
    
    public boolean isRendered();
    
    public long lastRenderTime();
    
    public void updateElement();

    public void mousePressed(Vector2d mousePosition);

    public void mouseReleased(Vector2d mousePosition);

    public void mouseDragged(Vector2d mousePosition);

    public IAction getAction();

    public IGuiElement setAction(IAction action);

    public default void drawTooltip()
    {
        Vector2d mousePosition = Screen.scaledMousePosition();

        if (this.isMouseInElement(mousePosition) && this.getTooltip() != null && !getTooltip().equalsIgnoreCase(""))
        {
            Draw.drawToolTip((int) mousePosition.x + 10, (int) mousePosition.y, this.getTooltip());
        }
        OpenGL.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    public String getTooltip();
    
    public void setTooltip(String tooltip);
    
    public void trackElement();
    
    public void stopTracking();
    
    public boolean canTrackInput();
    
    public boolean setTrackInput(boolean trackInput);
    
    public boolean isMouseInElement(Vector2d mousePosition);
}
