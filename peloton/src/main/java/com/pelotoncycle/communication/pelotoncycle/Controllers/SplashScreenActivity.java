package com.pelotoncycle.communication.pelotoncycle.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.pelotoncycle.communication.pelotoncycle.Controllers.util.SystemUiHider;
import com.pelotoncycle.communication.pelotoncycle.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SplashScreenActivity extends Activity implements Animation.AnimationListener {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;
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

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                final Intent startMainActivity = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(startMainActivity);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Take any action after completing the animation
        // check for fade in animation
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
