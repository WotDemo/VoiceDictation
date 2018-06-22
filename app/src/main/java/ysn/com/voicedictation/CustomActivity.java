package ysn.com.voicedictation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.lazy.library.logging.Logcat;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import ysn.com.voicedictation.util.JsonParser;
import ysn.com.voicedictation.util.ToastUtils;
import ysn.com.voicedictation.util.UShareEntry;

/**
 * @Author yangsanning
 * @ClassName CustomActivity
 * @Description 一句话概括作用
 * @Date 2018/6/12
 * @History 2018/6/12 author: description:
 */
public class CustomActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 缓存数据的名称
     */
    private static final String PRIVATE_SETTING = "com.iflytek.setting";

    /**
     * 引擎类型
     */
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    /**
     * 语音听写对象
     */
    private SpeechRecognizer mIat;

    /**
     * 用HashMap存储听写结果
     */
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    /**
     * 函数调用返回值
     */
    int ret = 0;

    private SharedPreferences mSharedPreferences;
    private EditText contentEditText;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(R.layout.activity_custom);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5b1e2ec3");
        initIat();
        findViewById(R.id.custom_activity_start_btn).setOnClickListener(this);
        contentEditText = findViewById(R.id.custom_activity_content_et);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIat) {
            //退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    /**
     * 创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
     */
    private void initIat() {
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        mSharedPreferences = getSharedPreferences(PRIVATE_SETTING, Activity.MODE_PRIVATE);
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
     * 参数设置
     */
    public void initParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //普通话
        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
        if ("en_us".equals(lag)) {
            // 设置语言 美式英文
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言 简体中文
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            //中文普通话 mandarin (默认)、粤语 cantonese 、四川话 lmz
            mIat.setParameter(SpeechConstant.ACCENT, "cantonese");
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 听写监听器
     */
    private RecognizerListener recognizerListener = new RecognizerListener() {

        //开始录音
        @Override
        public void onBeginOfSpeech() {
            ToastUtils.showNormalToast("开始说话");
            Logcat.d("开始说话");
        }

        //音量值0~30
        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {
            Logcat.d("当前正在说话，音量大小：" + volume);
        }

        //结束录音 ：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        @Override
        public void onEndOfSpeech() {
            ToastUtils.showNormalToast("结束说话");
            Logcat.d("结束说话");
        }

        //isLast等于true时会话结束。
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Logcat.d("results: " + results.getResultString());
            parseResult(results);
        }

        /**
         * 会话发生错误回调接口
         * 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
         * 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
         */

        @Override
        public void onError(SpeechError error) {
            ToastUtils.showNormalToast(error.getPlainDescription(true));
            Logcat.d("error: " + error.getPlainDescription(true));
        }

        //扩展用接口
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = bundle.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Logcat.d("session id =" + sid);
            }
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
            case R.id.custom_activity_start_btn:
                contentEditText.setText(null);
                mIatResults.clear();
                initParam();
                ret = mIat.startListening(recognizerListener);
                break;
            default:
        }
    }
}
