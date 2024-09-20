package com.manway.lockit.Handler;

import android.os.CountDownTimer;

import java.util.Calendar;

public abstract class InfiniteTimer extends CountDownTimer {



    public abstract void timerAction(long currentTimeMills);

    public InfiniteTimer() {
        super(10000, 1000);
    }

    public InfiniteTimer(long millseconds) {
        super(10000, millseconds);
    }

    @Override
    public void onTick(long millisUntilFinished) {

        timerAction(Calendar.getInstance().getTime().getTime());
    }

    @Override
    public void onFinish() {
        start();
    }

}
