package com.vkas.secondlock.ui.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vkas.secondlock.utils.LockWindow;

public class SlBroadcastReceiver extends BroadcastReceiver {
    private final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

            if (reason == null)
                return;

            // Home键
            if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                LockWindow.InstanceHelper.INSTANCE.getLockWindowHelper().closeThePasswordBox();
            }

            // 最近任务列表键
            if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                LockWindow.InstanceHelper.INSTANCE.getLockWindowHelper().closeThePasswordBox();
            }
        }
    }
}
