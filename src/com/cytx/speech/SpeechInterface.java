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
		
		//֪ͨ�ĸ�gameObject
		private static String gameObject = "SpeechManager";
	
	    private static Context unityContext;
	    private static UnityPlayerActivity unityActivity;
	    ///ת��������
	    private static SpeechRecognizer speechRecognizer;
	    //����ת����
	    private static SpeechSynthesizer speechSynthesizer;	    

	   // ��HashMap�洢��д���
		private static HashMap<String, String> resultMap = new LinkedHashMap<String, String>();
		
		private static byte[] audioData;
	    //init��������������������    
	    public static void init(Context paramContext){
	        unityContext = paramContext.getApplicationContext();
	        unityActivity = (UnityPlayerActivity) paramContext;
	        //ע��Ѷ�ɿƴ�
	        SpeechUtility.createUtility(unityContext, SpeechConstant.APPID +"=580dd8c8");
	        //����������д
	        initRecognizer();
	        //���������ϳ�
	        initSynthesizer();
	        
	        Log("��ʼ�����");
	    }
	    
	    //��������
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
	    	 speechRecognizer.setParameter(SpeechConstant.SAMPLE_RATE, "16000"); //������ 8KHz or 16KHz
	    	 speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, unityActivity.getExternalCacheDir().getAbsolutePath()+"/audio/asr.wav"); //����ʶ����ɺ��ڱ��ر������Ƶ�ļ�
	    	 speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav"); //��Ƶ��ʽ
	    	 
	    }
	    
	    private static void initSynthesizer()
	    {
	    	speechSynthesizer = SpeechSynthesizer.createSynthesizer(unityContext, null);
	    	speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); //���÷�����
	    	speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");//��������
	    	speechSynthesizer.setParameter(SpeechConstant.VOLUME, "100");//������������Χ 0~100
	    	speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //�����ƶ�
	    	//���úϳ���Ƶ����λ�ã����Զ��屣��λ�ã��������ڡ�./sdcard/iflytek.pcm��
	    	//������ SD ����Ҫ�� AndroidManifest.xml ���д SD ��Ȩ��
	    	//��֧�ֱ���Ϊ pcm �� wav ��ʽ���������Ҫ����ϳ���Ƶ��ע�͸��д���
	    	speechSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH, unityActivity.getExternalCacheDir().getAbsolutePath()+"/audio/tts.wav");
	    	speechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav"); //��Ƶ��ʽ
	    }
	    
	    
	   //��д������
	    private static RecognizerListener recognizerListener = new RecognizerListener()
	    {
	    	//��д����ص��ӿ�(����Json��ʽ������û��ɲμ���¼13.1)��
	    	//һ������»�ͨ��onResults�ӿڶ�η��ؽ����������ʶ�������Ƕ�ν�����ۼӣ�
	    	//���ڽ���Json�Ĵ���ɲμ�Demo��JsonParser�ࣻ
	    	//isLast����trueʱ�Ự������
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
	    	//�Ự��������ص��ӿ�
	    	public void onError(SpeechError error) 
	    	{
	    		sendUnityMessage("OnError",error.toString());
	    	}
	    	//��ʼ¼��
	    	public void onBeginOfSpeech() {
	    		Log("��ʼ¼��");
	    		audioData = new byte[0];
	    		resultMap.clear();
	    	}
	    	//volume����ֵ0~30��data��Ƶ����
	    	public void onVolumeChanged(int volume, byte[] data)
	    	{
	    		audioData = combineBytes(audioData, data);
	    		sendUnityMessage("OnVolumeChanged",String.valueOf(volume) );
	    	}
	    	//����¼��
	    	public void onEndOfSpeech() 
	    	{
	    		Log("����¼��");
				sendUnityMessage("OnEndOfSpeech","");
	    	}
	    	//��չ�ýӿ�
	    	public void onEvent(int eventType, int arg1, int arg2, Bundle obj) 
	    	{
	    		
	    	}
	
		
	    };
	    
	    //�ϳɼ�����
	    private static SynthesizerListener synthesizerListener =  new SynthesizerListener(){
	    	//�Ự�����ص��ӿڣ�û�д���ʱ��errorΪnull
	    	public void onCompleted(SpeechError error) 
	    	{
	    		if(error == null){
	    			Log("���Ž���");
	    		}
	    		else{
	    			Log(error.toString());
	    		}
	    	}
	    	//������Ȼص�
	    	//percentΪ�������0~100��beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ��
	    	//�ı��н���λ�ã�infoΪ������Ϣ��
	    	public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}
	    	//��ʼ����
	    	public void onSpeakBegin() {Log("��ʼ���źϳ�����");}
	    	//��ͣ����
	    	public void onSpeakPaused() {Log("��ͣ���źϳ�����");}
	    	//���Ž��Ȼص�
	    	//percentΪ���Ž���0~100,beginPosΪ������Ƶ���ı��п�ʼλ�ã�endPos��ʾ������Ƶ����
	    	//���н���λ��.
	    	public void onSpeakProgress(int percent, int beginPos, int endPos) {}
	    	//�ָ����Żص��ӿ�
	    	public void onSpeakResumed() {}
	    	//�Ự�¼��ص��ӿ�
	    	public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
	    };
	    
	    
	    //��ʼ��д
	    public static void startListening()
	    {
	    	//speechRecognizer.cancel();
	    	
	    	speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "1");
	    	speechRecognizer.startListening(recognizerListener);
	    }
	    //ֹͣ��д
	    public static void stopListening()
	    {
	    	speechRecognizer.stopListening();
	    	Log("ֹͣ¼��");
	    }
	    
	    //��ȡ�����ļ�ʶ��
	    public static void startRecognize(String path)
	    {
	    	//speechRecognizer.cancel();
	    	
	    	speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-2"); //ʹ�ñ�����Ƶ�ļ�
	    	//���ö�ȡ��Ƶ��·��
	    	speechRecognizer.setParameter(SpeechConstant.ASR_SOURCE_PATH, path);
	    	
	    	speechRecognizer.startListening(recognizerListener);
	    }
	    
	    //ͨ��дbyte��д
	    public static void startRecognize(final byte[] bytes)
	    {
	    	new Thread(new Runnable() {

	            public void run() {
	            	
	            	//speechRecognizer.cancel();
	            	
	            	speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
	                // ���ö����ѡ���
	            	speechRecognizer.setParameter(SpeechConstant.ASR_NBEST, "3");
	            	speechRecognizer.setParameter(SpeechConstant.ASR_WBEST, "3");

	            	speechRecognizer.startListening(recognizerListener);
	            	
	            	speechRecognizer.writeAudio(bytes, 0, bytes.length);
	                
	            	speechRecognizer.stopListening();
	            }

	        }).start();
	    }
	    
	    //��ȡ��Ƶ����
	    public static byte[] getAudioData() {
			return audioData;
		}
	    // ��ʼ˵��
	    public static void startSpeaking(String message )
	    {
	    	speechSynthesizer.startSpeaking(message, synthesizerListener);
	    }
	    //ֹͣ˵��
	    public static void stopSpeaking()
	    {
	    	speechSynthesizer.stopSpeaking();
	    }
	    
	    //����Unity��SpeechManager�еķ���
	    
	    private static void sendUnityMessage(String func,String message)
	    {
	    	UnityPlayer.UnitySendMessage(gameObject, func, message);
	    }
	    
	    private static void Log(String message)
	    {
	    	 //sendUnityMessage("Log", message);
	    	 showMessage(message);
	    }
	    //����msg������һ��Toast��ʾ
	    public static void showMessage( final String msg){

	        unityActivity.runOnUiThread(new Runnable()
	        {    
	            public void run()    
	            {    
	                Toast.makeText(unityContext,msg, Toast.LENGTH_LONG).show();
	            }
	        });

	    }
	    
	    //��������ת���ֽ��
		private static void parseResult(RecognizerResult results) {
			
			if(results.equals(null))return;
			
			String text = JsonParser.parseIatResult(results.getResultString());

			String sn = null;
			// ��ȡjson����е�sn�ֶ�
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
