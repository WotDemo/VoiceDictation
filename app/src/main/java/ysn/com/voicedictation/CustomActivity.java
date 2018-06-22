package ysn.com.voicedictation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.lazy.library.logging.Logcat;

import ysn.com.voicedictation.helper.XfHelper;
import ysn.com.voicedictation.util.ToastUtils;

/**
 * @Author yangsanning
 * @ClassName CustomActivity
 * @Description 一句话概括作用
 * @Date 2018/6/12
 * @History 2018/6/12 author: description:
 */
public class CustomActivity extends AppCompatActivity implements View.OnClickListener, XfHelper.OnXfListener {

    private EditText contentEditText;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(R.layout.activity_custom);
        findViewById(R.id.custom_activity_start_btn).setOnClickListener(this);
        contentEditText = findViewById(R.id.custom_activity_content_et);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XfHelper.get(this).destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_activity_start_btn:
                contentEditText.setText(null);
                XfHelper.get(this).setOnXfListener(this).start();
                break;
            default:
        }
    }

    @Override
    public void onVolumeChanged(int volume) {
        Logcat.d("当前正在说话，音量大小：" + volume);
    }

    @Override
    public void onResult(String result) {
        Logcat.d("result: " + result);
        contentEditText.setText(result);
    }

    @Override
    public void onError(int code, String msg) {
        Logcat.d("error: " + msg);
    }

    @Override
    public void onEndOfSpeech() {
        Logcat.d("结束说话");
        ToastUtils.showNormalToast("结束说话");
    }
}
