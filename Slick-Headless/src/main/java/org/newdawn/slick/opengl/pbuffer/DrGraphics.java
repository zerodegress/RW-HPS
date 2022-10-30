/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package org.newdawn.slick.opengl.pbuffer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

public class DrGraphics extends Graphics {
    /** The image we're we're sort of rendering to */
    private Image image;
    /** The ID of the FBO in use */
    private int FBO;
    /** True if this context is valid */
    private boolean valid = true;

    public DrGraphics(Image image) throws SlickException {
        super(image.getTexture().getTextureWidth(), image.getTexture().getTextureHeight());

        Log.debug("Creating Dr "+image.getWidth()+"x"+image.getHeight());

        init();
    }

    /**
     * Initialise the FBO that will be used to render to
     *
     * @throws SlickException
     */
    private void init() throws SlickException {
    }

    /**
     * Bind to the FBO created
     */
    private void bind() {
    }

    /**
     * Unbind from the FBO created
     */
    private void unbind() {
    }

    /**
     * @see org.newdawn.slick.Graphics#disable()
     */
    protected void disable() {
    }

    /**
     * @see org.newdawn.slick.Graphics#enable()
     */
    protected void enable() {
    }

    /**
     * Initialise the GL context
     */
    protected void initGL() {
    }

    /**
     * Enter the orthographic mode
     */
    protected void enterOrtho() {
    }

    /**
     * @see org.newdawn.slick.Graphics#destroy()
     */
    public void destroy() {
    }

    /**
     * @see org.newdawn.slick.Graphics#flush()
     */
    public void flush() {
    }
}
