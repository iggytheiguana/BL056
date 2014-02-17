package biz.softtechnics.qodeme.ui.tutorial;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;

/**
 * Created by Alex on 11/18/13.
 */
public class TutorialActivity extends FragmentActivity {

    private TutorialFragmentAdapter adapter;
    private ViewPager pager;
    private PageIndicator indicator;
    private ImageButton buttonBack;
    private ImageButton buttonForward;
    private ImageButton buttonFinish;
    private ImageButton buttonClose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_tutorial);
        initViews();
        setListners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnalyticsHelper.onStartActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AnalyticsHelper.onStopActivity(this);
    }

    private void initViews() {

        pager = (ViewPager) findViewById(R.id.introduction_pager);
        adapter = new TutorialFragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        indicator = (CirclePageIndicator) findViewById(R.id.introduction_indicator);
        indicator.setViewPager(pager);

        buttonBack = (ImageButton) findViewById(R.id.introduction_button_back);
        buttonBack.setVisibility(Button.INVISIBLE);
        buttonForward = (ImageButton) findViewById(R.id.introduction_button_forward);
        buttonFinish = (ImageButton) findViewById(R.id.introduction_button_finish);
        buttonClose = (ImageButton)findViewById(R.id.introduction_button_close);
    }

    private void setListners() {
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageSelected(int position) {
                buttonBack.setVisibility(position == 0 ? Button.INVISIBLE : Button.VISIBLE);

                if (position == (adapter.getCount() - 1)) {
                    buttonForward.setVisibility(Button.INVISIBLE);
                    buttonFinish.setVisibility(Button.VISIBLE);
                } else {
                    buttonForward.setVisibility(Button.VISIBLE);
                    buttonFinish.setVisibility(Button.INVISIBLE);
                }
            }
            @Override public void onPageScrolled(int arg0, float arg1, int arg2) {    }
            @Override public void onPageScrollStateChanged(int arg0) {    }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() - 1);
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            }
        });

        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

}
