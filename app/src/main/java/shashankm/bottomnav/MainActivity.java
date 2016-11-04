package shashankm.bottomnav;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import static android.view.ViewTreeObserver.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static int PREVIOUS_HEIGHT = -1;
    private static boolean isOpen = false;
    private LinearLayout signUp;
    private View textTrack;
    private View button;
    private View activityRootView;
    private OnGlobalLayoutListener onGlobalLayoutListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View textView = findViewById(R.id.sign_up);
        signUp = (LinearLayout) findViewById(R.id.sign_up_layout);
        textTrack = findViewById(R.id.text_track);
        button = findViewById(R.id.button);
        PercentView percentView = (PercentView) findViewById(R.id.percent_view);
        percentView.setPercentage(80f);

        textView.setOnClickListener(this);

        keyBoardChangeListener();
    }

    private void keyBoardChangeListener() {
        activityRootView = findViewById(R.id.activity_main);
        ViewTreeObserver viewTreeObserver = activityRootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                onGlobalLayoutListener = this;
                //r will be populated with the coordinates of your view that area still visible.
                activityRootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (PREVIOUS_HEIGHT == -1) {
                    PREVIOUS_HEIGHT = heightDiff;
                }

                if (heightDiff > (PREVIOUS_HEIGHT + 100)) {
                    PREVIOUS_HEIGHT = heightDiff;
                    textTrack.setVisibility(View.GONE);
                    button.setVisibility(View.GONE);
                    Log.d("NavBarController", "Keyboard is shown");
                } else if (heightDiff < PREVIOUS_HEIGHT) {
                    PREVIOUS_HEIGHT = heightDiff;
                    textTrack.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    Log.d("NavBarController", "Keyboard is hidden");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT < 16) {
            activityRootView.getViewTreeObserver()
                    .removeGlobalOnLayoutListener(onGlobalLayoutListener);
        } else {
            activityRootView.getViewTreeObserver()
                    .removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    @Override
    public void onClick(View view) {
        if (!isOpen) {
            isOpen = true;
            button.animate()
                    .translationY(-200f)
                    .setDuration(300)
                    .start();
            textTrack.animate()
                    .translationY(-200f)
                    .setDuration(300)
                    .start();
            expand(signUp);
        } else {
            isOpen = false;
            button.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .start();
            textTrack.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .start();
            collapse(signUp);
        }
    }

    public static void expand(final View v) {
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
