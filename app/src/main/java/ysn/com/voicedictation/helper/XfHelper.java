package ysn.com.voicedictation.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ysn.com.voicedictation.util.JsonParser;

/**
 * @Author yangsanning
 * @ClassName XfHelper
 * @Description 讯飞语音听写的辅助类
 * @Date 2018/6/22
 * @History 在下方书写更新历史记录
 * date: 2018/6/22
 * author: yangsanning
 * description: 直接调用start开始，可调用setAccent进行语种设置，调用destroy进行释放连接
 */
public class XfHelper implements InitListener, RecognizerListener {

    /**
     * 缓存数据的名称
     */
    private static final String PRIVATE_SETTING = "com.iflytek.setting";

    /**
     * 引擎类型
     */
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private static XfHelper instance;
    private SharedPreferences sharedPreferences;
    private OnXfListener onXfListener;
    private SpeechRecognizer speechRecognizer;

    /**
     * 用HashMap存储听写结果
     * accent: 语种
     */
    private HashMap<String, String> speechResults = new LinkedHashMap<>();
    private String accent = Accent.MANDARIN;

    private static XfHelper create(Context context) {
        instance = new XfHelper(context);
        return instance;
    }

    public static synchronized XfHelper get(Context context) {
        if (instance == null) {
            synchronized (XfHelper.class) {
                if (instance == null) {
                    instance = create(context);
                }
            }
        }
        return instance;
    }

    public void destroy() {
        //退出时释放连接
        if (null != speechRecognizer) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
        onXfListener = null;
    }

    private XfHelper(Context context) {
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=5b1e2ec3");
        initSpeechRecognizer(context);
    }

    /**
     * 初始化语音听写对象
     */
    private void initSpeechRecognizer(Context context) {
        speechRecognizer = SpeechRecognizer.createRecognizer(context, this);
        sharedPreferences = context.getSharedPreferences(PRIVATE_SETTING, Activity.MODE_PRIVATE);
    }

    /**
     * 参数设置
     */
    private void configureParam() {
        // 清空参数
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //普通话
        String lag = sharedPreferences.getString("iat_language_preference", "mandarin");
        if ("en_us".equals(lag)) {
            // 设置语言 美式英文
            speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言 简体中文
            speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            //中文普通话 mandarin (默认)、粤语 cantonese 、四川话 lmz
            speechRecognizer.setParameter(SpeechConstant.ACCENT, accent);
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        speechRecognizer.setParameter(SpeechConstant.VAD_BOS, sharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        speechRecognizer.setParameter(SpeechConstant.VAD_EOS, sharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT, sharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * @param code 错误码
     */
    @Override
    public void onInit(int code) {
        if (code != ErrorCode.SUCCESS) {
            onXfListener.onError(code, "初始化失败");
        }
    }

    /**
     * @param volume 音量值0~30
     */
    @Override
    public void onVolumeChanged(int volume, byte[] bytes) {
        if (onXfListener != null) {
            onXfListener.onVolumeChanged(volume);
        }
    }

    /**
     * 开始录音
     */
    @Override
    public void onBeginOfSpeech() {

    }

    /**
     * 结束说话: 检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
     */
    @Override
    public void onEndOfSpeech() {
        if (onXfListener != null) {
            onXfListener.onEndOfSpeech();
        }
    }

    /**
     * @param isLast isLast等于true时会话结束
     */
    @Override
    public void onResult(RecognizerResult results, boolean isLast) {
        onResult(results);
    }

    @Override
    public void onError(SpeechError speechError) {
        if (onXfListener != null) {
            onXfListener.onError(speechError.getErrorCode(), speechError.toString());
        }
    }

    /**
     * 扩展用接口
     */
    @Override
    public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
        // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
        // 若使用本地能力，会话id为null
        if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            String sid = bundle.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            Log.e("XfHelper", "session id =" + sid);
        }
    }

    private void onResult(RecognizerResult results) {
        try {
            // 读取json结果中的sn字段
            speechResults.put(new JSONObject(results.getResultString()).optString("sn"),
                    JsonParser.parseIatResult(results.getResultString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String result = getResult();
        Logcat.d(result);
        if (onXfListener != null) {
            onXfListener.onResult(result);
        }
    }

    public String getResult() {
        StringBuilder resultBuffer = new StringBuilder();
        for (String key : speechResults.keySet()) {
            resultBuffer.append(speechResults.get(key));
        }
        return resultBuffer.toString();
    }

    public void start() {
        speechResults.clear();
        configureParam();
        speechRecognizer.startListening(this);
    }

    public XfHelper setAccent(String accent) {
        this.accent = accent;
        return instance;
    }

    public XfHelper setOnXfListener(OnXfListener onXfListener) {
        this.onXfListener = onXfListener;
        return instance;
    }

    public interface OnXfListener extends EventListener {

        /**
         * 回调音量，可用于自定义UI
         *
         * @param volume 音量值0~30
         */
        void onVolumeChanged(int volume);

        /**
         * 听写字符串
         *
         * @param result 听写字符串
         */
        void onResult(String result);

        /**
         * 初始化和语音识别的错误信息
         *
         * @param code 错误code
         * @param msg  无措msg
         */
        void onError(int code, String msg);

        /**
         * 结束说话
         */
        void onEndOfSpeech();
    }

    public interface Accent {

        /**
         * 中文普通话 mandarin (默认)
         */
        String MANDARIN = "mandarin";

        /**
         * 粤语 cantonese
         */
        String CANTONESE = "cantonese";

        /**
         * 四川话 lmz
         */
        String LMZ = "lmz";
    }
}
