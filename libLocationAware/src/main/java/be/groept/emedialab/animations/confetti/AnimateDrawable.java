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
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

public class AnimateDrawable extends ProxyDrawable {

    private Animation animation;
    private Transformation transformation = new Transformation();

    public AnimateDrawable(Drawable target){
        super(target);
    }

    public AnimateDrawable(Drawable target, Animation animation){
        super(target);
        this.animation = animation;
    }

    public Animation getAnimation(){
        return animation;
    }

    public void setAnimation(Animation animation){
        this.animation = animation;
    }

    public boolean hasStarted(){
        return animation != null && animation.hasStarted();
    }

    public boolean hasEnded(){
        return animation == null || animation.hasEnded();
    }

    @Override
    public void draw(Canvas canvas){
        Drawable drawable = getProxy();
        if(drawable != null){
            int saveCount = canvas.save();
            Animation anim = animation;
            if(anim != null){
                anim.getTransformation(AnimationUtils.currentAnimationTimeMillis(), transformation);
                canvas.concat(transformation.getMatrix());
            }
            drawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

}
