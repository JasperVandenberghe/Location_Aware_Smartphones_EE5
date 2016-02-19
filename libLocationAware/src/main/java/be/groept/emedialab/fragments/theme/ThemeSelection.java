package be.groept.emedialab.fragments.theme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import be.groept.emedialab.R;

public class ThemeSelection {

    private int color;

    public ThemeSelection(){}

    public LayoutInflater selectTheme(Bundle bundle, LayoutInflater inflater, Activity activity){
        Context contextThemeWrapper = new ContextThemeWrapper();
        if(bundle != null){
            if("HMT".equals(bundle.getString("Theme"))){
                contextThemeWrapper = new ContextThemeWrapper(activity, R.style.HappyManTheme);
                color = R.color.colorPrimaryHMT;
            } else if("AFT".equals(bundle.getString("Theme"))){
                contextThemeWrapper = new ContextThemeWrapper(activity, R.style.AnimalFarmTheme);
                color = R.color.colorPrimaryAFT;
            }
        } else{
            contextThemeWrapper = new ContextThemeWrapper(activity, R.style.AppTheme);
            color = Color.GRAY;
        }
        return inflater.cloneInContext(contextThemeWrapper);
    }

    public int getColor(){
        return color;
    }
}
