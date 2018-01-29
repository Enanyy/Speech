package com.cytx.speech;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


public class SpeechInterface {
		
		//通知哪个gameObject
		private static String gameObject = "SpeechManager";
	
	    private static Context unityContext;
	    private static UnityPlayerActivity unityActivity;
	    ///转文字语音
	    private static SpeechRecognizer speechRecognizer;
	    //文字转语音
	    private static SpeechSynthesizer speechSynthesizer;	    

	   // 用HashMap存储听写结果
		private static HashMap<String, String> resultMap = new LinkedHashMap<String, String>();
		
		private static byte[] audioData;
	    //init方法，用来传入上下文    
	    public static void init(Context paramContext){
	        unityContext = paramContext.getApplicationContext();
	        unityActivity = (UnityPlayerActivity) paramContext;
	        //注册讯飞科大
	        SpeechUtility.createUtility(unityContext, SpeechConstant.APPID +"=580dd8c8");
	        //创建语音听写
	        initRecognizer();
	        //创建语音合成
	        initSynthesizer();
	        
	        Log("初始化完成");
	    }
	    
	    //销毁引擎
	    public static void destroy()
	    {
	    	speechRecognizer.destroy();
	    	speechSynthesizer.destroy();
	    	SpeechUtility.getUtility().destroy();
	    }
	    
	    private static void initRecognizer()
	    {
	    	 speechRecognizer =  SpeechRecognizer.createRecognizer(unityContext, null);
	    	 speechRecognizer.setParameter(SpeechConstant.DOMAIN, "iat");
	    	 speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
	    	 speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin ");
	    	 speechRecognizer.setParameter(SpeechConstant.SAMPLE_RATE, "16000"); //采样率 8KHz or 16KHz
	    	 speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, unityActivity.getExternalCacheDir().getAbsolutePath()+"/audio/asr.wav"); //语音识别完成后在本地保存的音频文件
	    	 speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav"); //音频格式
	    	 
	    }
	    
	    private static void initSynthesizer()
	    {
	    	speechSynthesizer = SpeechSynthesizer.createSynthesizer(unityContext, null);
	    	speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); //设置发音人
	    	speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");//设置语速
	    	speechSynthesizer.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围 0~100
	    	speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
	    	//设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
	    	//保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
	    	//仅支持保存为 pcm 和 wav 格式，如果不需要保存合成音频，注释该行代码
	    	speechSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH, unityActivity.getExternalCacheDir().getAbsolutePath()+"/audio/tts.wav");
	    	speechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav"); //音频格式
	    }
	    
	    
	   //听写监听器
	    private static RecognizerListener recognizerListener = new RecognizerListener()
	    {
	    	//听写结果回调接口(返回Json格式结果，用户可参见附录13.1)；
	    	//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
	    	//关于解析Json的代码可参见Demo中JsonParser类；
	    	//isLast等于true时会话结束。
	    	public void onResult(RecognizerResult results, boolean isLast) 
	    	{
	    		parseResult(results);
	    		if(isLast)
	    		{
	    			StringBuffer resultBuffer = new StringBuffer();
	    			for (String key : resultMap.keySet()) {
	    				resultBuffer.append(resultMap.get(key));
	    			}
	    			String resultString= resultBuffer.toString();
	    			sendUnityMessage("OnResult", resultString);
	    			showMessage(resultString);
	    		}
	    	}
	    	//会话发生错误回调接口
	    	public void onError(SpeechError error) 
	    	{
	    		sendUnityMessage("OnError",error.toString());
	    	}
	    	//开始录音
	    	public void onBeginOfSpeech() {
	    		Log("开始录音");
	    		audioData = new byte[0];
	    		resultMap.clear();
	    	}
	    	//volume音量值0~30，data音频数据
	    	public void onVolumeChanged(int volume, byte[] data)
	    	{
	    		audioData = combineBytes(audioData, data);
	    		sendUnityMessage("OnVolumeChanged",String.valueOf(volume) );
	    	}
	    	//结束录音
	    	public void onEndOfSpeech() 
	    	{
	    		Log("结束录音");
				sendUnityMessage("OnEndOfSpeech","");
	    	}
	    	//扩展用接口
	    	public void onEvent(int eventType, int arg1, int arg2, Bundle obj) 
	    	{
	    		
	    	}
	
		
	    };
	    
	    //合成监听器
	    private static SynthesizerListener synthesizerListener =  new SynthesizerListener(){
	    	//会话结束回调接口，没有错误时，error为null
	    	public void onCompleted(SpeechError error) 
	    	{
	    		if(error == null){
	    			Log("播放结束");
	    		}
	    		else{
	    			Log(error.toString());
	    		}
	    	}
	    	//缓冲进度回调
	    	//percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在
	    	//文本中结束位置，info为附加信息。
	    	public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}
	    	//开始播放
	    	public void onSpeakBegin() {Log("开始播放合成语音");}
	    	//暂停播放
	    	public void onSpeakPaused() {Log("暂停播放合成语音");}
	    	//播放进度回调
	    	//percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文
	    	//本中结束位置.
	    	public void onSpeakProgress(int percent, int beginPos, int endPos) {}
	    	//恢复播放回调接口
	    	public void onSpeakResumed() {}
	    	//会话事件回调接口
	    	public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
	    };
	    
	    
	    //开始听写
	    public static void startListening()
	    {
	    	//speechRecognizer.cancel();
	    	
	    	speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "1");
	    	speechRecognizer.startListening(recognizerListener);
	    }
	    //停止听写
	    public static void stopListening()
	    {
	    	speechRecognizer.stopListening();
	    	Log("停止录音");
	    }
	    
	    //读取本地文件识别
	    public static void startRecognize(String path)
	    {
	    	//speechRecognizer.cancel();
	    	
	    	speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-2"); //使用本地音频文件
	    	//设置读取音频的路径
	    	speechRecognizer.setParameter(SpeechConstant.ASR_SOURCE_PATH, path);
	    	
	    	speechRecognizer.startListening(recognizerListener);
	    }
	    
	    //通过写byte听写
	    public static void startRecognize(final byte[] bytes)
	    {
	    	new Thread(new Runnable() {

	            public void run() {
	            	
	            	//speechRecognizer.cancel();
	            	
	            	speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
	                // 设置多个候选结果
	            	speechRecognizer.setParameter(SpeechConstant.ASR_NBEST, "3");
	            	speechRecognizer.setParameter(SpeechConstant.ASR_WBEST, "3");

	            	speechRecognizer.startListening(recognizerListener);
	            	
	            	speechRecognizer.writeAudio(bytes, 0, bytes.length);
	                
	            	speechRecognizer.stopListening();
	            }

	        }).start();
	    }
	    
	    //获取音频数据
	    public static byte[] getAudioData() {
			return audioData;
		}
	    // 开始说话
	    public static void startSpeaking(String message )
	    {
	    	speechSynthesizer.startSpeaking(message, synthesizerListener);
	    }
	    //停止说话
	    public static void stopSpeaking()
	    {
	    	speechSynthesizer.stopSpeaking();
	    }
	    
	    //调用Unity的SpeechManager中的方法
	    
	    private static void sendUnityMessage(String func,String message)
	    {
	    	UnityPlayer.UnitySendMessage(gameObject, func, message);
	    }
	    
	    private static void Log(String message)
	    {
	    	 //sendUnityMessage("Log", message);
	    	 showMessage(message);
	    }
	    //传入msg，弹出一个Toast提示
	    public static void showMessage( final String msg){

	        unityActivity.runOnUiThread(new Runnable()
	        {    
	            public void run()    
	            {    
	                Toast.makeText(unityContext,msg, Toast.LENGTH_LONG).show();
	            }
	        });

	    }
	    
	    //解析语音转文字结果
		private static void parseResult(RecognizerResult results) {
			
			if(results.equals(null))return;
			
			String text = JsonParser.parseIatResult(results.getResultString());

			String sn = null;
			// 读取json结果中的sn字段
			try {
				JSONObject resultJson = new JSONObject(results.getResultString());
				sn = resultJson.optString("sn");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			resultMap.put(sn, text);
			
		}
		
		public static byte[] combineBytes(byte[] data1, byte[] data2) {  
		    byte[] data3 = new byte[data1.length + data2.length];  
		    System.arraycopy(data1, 0, data3, 0, data1.length);  
		    System.arraycopy(data2, 0, data3, data1.length, data2.length);  
		    return data3;  
		}
}
