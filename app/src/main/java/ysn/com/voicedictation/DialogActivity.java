package ysn.com.voicedictation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lazy.library.logging.Logcat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ysn.com.voicedictation.util.JsonParser;
import ysn.com.voicedictation.util.ToastUtils;

/**
 * @Author yangsanning
 * @ClassName DialogActivity
 * @Description 一句话概括作用
 * @Date 2018/6/12
 * @History 2018/6/12 author: description:
 */
public class DialogActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 用HashMap存储听写结果
     */
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    /**
     * 语音听写 Dialog
     */
    private RecognizerDialog mIatDialog;

    private EditText contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(R.layout.activity_dialog);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5b1e2ec3");
        initIat();
        findViewById(R.id.dialog_activity_start_btn).setOnClickListener(this);
        contentEditText = findViewById(R.id.dialog_activity_content_et);
    }

    /**
     * 初始化语音识别中的对象
     */
    private void initIat() {
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);
    }

    /**
     * 初始化监听器
     */
    private InitListener mInitListener = code -> {
        Logcat.d("SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            ToastUtils.showNormalToast("初始化失败，错误码：" + code);
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Logcat.d(results.getResultString());
            parseResult(results);
        }

        /**
         * 识别回调错误.
         */
        @Override
        public void onError(SpeechError error) {
            ToastUtils.showNormalToast(error.getPlainDescription(true));
        }
    };

    private void parseResult(RecognizerResult results) {
        String result = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, result);

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String content = resultBuffer.toString();
        Logcat.d(content);
        contentEditText.setText(content);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_activity_start_btn:
                contentEditText.setText(null);
                mIatResults.clear();
                // 显示听写对话框
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                //隐藏讯飞输入法提供字样
                TextView textLink = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
                textLink.setText("");
                textLink.setOnClickListener(null);
                ToastUtils.showNormalToast("开始录音");
                break;
            default:
        }
    }
}
