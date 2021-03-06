package com.holmusk.view.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.bowyer.app.fabtoolbar.FabToolbar;
import com.crashlytics.android.Crashlytics;
import com.dlazaro66.wheelindicatorview.WheelIndicatorItem;
import com.dlazaro66.wheelindicatorview.WheelIndicatorView;
import com.holmusk.view.components.scrollableviewpager.BaseFragment;
import com.holmusk.view.components.scrollableviewpager.RecyclerViewFragment;
import com.holmusk.view.components.scrollableviewpager.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import nutriwiki.holmusk.com.nutriwiki.R;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

public class SummaryActivity extends AppCompatActivity {
    private static final String ARG_LAST_SCROLL_Y = "arg.LastScrollY"; //store last scroll position when activity is stopped

    /* Bind views to objects */
    @Bind(R.id.wheel_indicator_view)
    WheelIndicatorView wheelView;
    @Bind(R.id.fab)
    FloatingActionButton actionButton;
    @Bind(R.id.fabtoolbar)
    FabToolbar fabToolbar;

    //Binds the action buttons
    @Bind(R.id.btn_logfood)
    ImageView btnLogFood;
    @Bind(R.id.btn_search)
    ImageView btnSearch;
    @Bind(R.id.btn_chart)
    ImageView btnChart;

    //Bind progress bars
    @Bind(R.id.progressBarCarb)
    ProgressBar progressBarCarb;
    @Bind(R.id.progressBarFat)
    ProgressBar progressBarFat;
    @Bind(R.id.progressBarProtein)
    ProgressBar progressBarProtein;

    //Bind scrollable viewpager views
    @Bind(R.id.header)
     View header;

    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @Bind(R.id.view_pager)
    ViewPager viewPager;

    @Bind(R.id.scrollable_layout)
     ScrollableLayout mScrollableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_summary);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Toolbar will now take on default Action Bar characteristics
        setSupportActionBar(toolbar);

        /* Set up scrollable view pager */
        mScrollableLayout.setDraggableView(tabs);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getResources(), getFragments());
        viewPager.setAdapter(adapter);
        tabs.setViewPager(viewPager);
        mScrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return adapter.canScrollVertically(viewPager.getCurrentItem(), direction);
            }
        });

        mScrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {
                //Hide FAB toolbar
                if (fabToolbar.isFabExpanded()) {
                    fabToolbar.slideOutFab();
                }

                //Perform header scroll
                final float tabsTranslationY;
                if (y < maxY) {
                    tabsTranslationY = .0F;
                } else {
                    tabsTranslationY = y - maxY;
                }
                tabs.setTranslationY(tabsTranslationY);
                header.setTranslationY(y / 2);
            }
        });

        //Maintain the current scroll position when resume activity
        if (savedInstanceState != null) {
            final int y = savedInstanceState.getInt(ARG_LAST_SCROLL_Y);
            mScrollableLayout.post(new Runnable() {
                @Override
                public void run() {
                    mScrollableLayout.scrollTo(0, y);
                }
            });
        }

        /*Init header view and animation*/
        initViews();

        /*Bind Floating action button to FABtoolbar */
        fabToolbar.setFab(actionButton);

    }

    private void initViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar));
        }

        //Progress bars
        progressBarCarb.setMax(100);
        progressBarCarb.setProgress(50);
        progressBarCarb.getProgressDrawable().setColorFilter(getResources().getColor(R.color.orange500), PorterDuff.Mode.SRC_IN);

        progressBarFat.setMax(100);
        progressBarFat.setProgress(76);
        progressBarFat.getProgressDrawable().setColorFilter(getResources().getColor(R.color.orange500), PorterDuff.Mode.SRC_IN);

        progressBarProtein.setMax(100);
        progressBarProtein.setProgress(80);
        progressBarProtein.getProgressDrawable().setColorFilter(getResources().getColor(R.color.orange500), PorterDuff.Mode.SRC_IN);


        //Wheel View
        WheelIndicatorItem breakFastIncatorItem = new WheelIndicatorItem(30f , getResources().getColor(R.color.green_bright));
        wheelView.addWheelIndicatorItem(breakFastIncatorItem);
        WheelIndicatorItem lunchIndicatorItem = new WheelIndicatorItem(25f, getResources().getColor(R.color.yellow_light));
        wheelView.addWheelIndicatorItem(lunchIndicatorItem);
        WheelIndicatorItem dinnerIncatorItem = new WheelIndicatorItem(25f, getResources().getColor(R.color.orange_background));
        wheelView.addWheelIndicatorItem(dinnerIncatorItem);
        wheelView.setFilledPercent(80);
        wheelView.notifyDataSetChanged();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wheelView.startItemsAnimation();
            }
        }, 300);
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        fabToolbar.expandFab();
    }

    @OnClick(R.id.btn_logfood)
    void onClickCall() {
        iconAnim(btnLogFood);
    }

    @OnClick(R.id.btn_search)
    void onClickEmail() {
        iconAnim(btnSearch);
    }

    @OnClick(R.id.btn_chart)
    void onClickForum() {
        iconAnim(btnChart);
    }

    private void iconAnim(final View icon) {
        Animator iconAnim = ObjectAnimator.ofPropertyValuesHolder(
                icon,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.8f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.8f, 1f));
        iconAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (icon.getId()==R.id.btn_search || icon.getId() == R.id.btn_logfood){
                    Intent searchFood = new Intent(SummaryActivity.this, SearchFoodActivity.class);
                    startActivity(searchFood);
                }

                if (icon.getId() == R.id.btn_chart){
                    Toast.makeText(getApplicationContext(),"Button chart selected",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        iconAnim.start();
    }

    private List<BaseFragment> getFragments() {

        final FragmentManager manager = getSupportFragmentManager();
        final List<BaseFragment> list = new ArrayList<>();


        RecyclerViewFragment today
                = (RecyclerViewFragment) manager.findFragmentByTag(RecyclerViewFragment.TAG);
        if (today == null) {
            today = RecyclerViewFragment.newInstance(getResources().getColor(R.color.white100));
        }



        Collections.addAll(list, today);

        return list;
    }



    /* Activity functions*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return  super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (fabToolbar.isFabExpanded()) {
            fabToolbar.slideOutFab();
        }
        else
            super.onBackPressed();
    }

}
