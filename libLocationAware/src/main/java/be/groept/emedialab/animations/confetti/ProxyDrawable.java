/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.groept.emedialab.animations.confetti;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class ProxyDrawable extends Drawable {

    private Drawable proxy;

    public ProxyDrawable(Drawable target){
        proxy = target;
    }

    public Drawable getProxy(){
        return proxy;
    }

    public void setProxy(Drawable proxy){
        if(proxy != this)
            this.proxy = proxy;
    }

    @Override
    public void draw(Canvas canvas){
        if(proxy != null)
            proxy.draw(canvas);
    }

    @Override
    public int getIntrinsicWidth(){
        return proxy != null ? proxy.getIntrinsicWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight(){
        return proxy != null ? proxy.getIntrinsicHeight() : -1;
    }

    @Override
    public int getOpacity(){
        return proxy != null ? proxy.getOpacity() : PixelFormat.TRANSPARENT;
    }

    @Override
    public void setFilterBitmap(boolean filter){
        if(proxy != null)
            proxy.setFilterBitmap(filter);
    }

    @Override
    public void setDither(boolean dither){
        if(proxy != null)
            proxy.setDither(dither);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter){
        if(proxy != null)
            proxy.setColorFilter(colorFilter);
    }

    @Override
    public void setAlpha(int alpha){
        if(proxy != null)
            proxy.setAlpha(alpha);
    }
}
