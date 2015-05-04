package com.pelotoncycle.communication.pelotoncycle.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.pelotoncycle.communication.pelotoncycle.R;

/**
 * A splash screen when the app is launched
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class SplashScreenActivity extends Activity implements Animation.AnimationListener {


    private Animation animFadeIn, animFadeOut;
    private ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        splashImage = (ImageView) findViewById(R.id.splash_imageview);
        // load the animation
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        // set animation listener
        animFadeIn.setAnimationListener(this);
        splashImage.setVisibility(View.VISIBLE);
        splashImage.startAnimation(animFadeIn);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                final Intent startMainActivity = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(startMainActivity);

                // close this activity
                finish();
            }
        }, 2000);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        if (animation == animFadeIn) {
            splashImage.startAnimation(animFadeOut);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
