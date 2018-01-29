// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   VoiceRecorder.java

package com.cytx.voice;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class VoiceRecorder
{
	private static UnityPlayerActivity unityActivity;

	private static MediaRecorder mRecorder;
	private static MediaPlayer mPlayer;
	private static boolean mStartRecording = false;
	private static long mStartRecordTime;
	private static int mRecordSeconds;
	
	private static OnInfoListener mRecordInfoListener = new OnInfoListener() {

		public void onInfo(MediaRecorder mr, int what, int extra)
		{
			if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
			{
				stopRecord();
				
				UnityPlayer.UnitySendMessage("VoiceManager", "OnRecordComplete", "");
			}
		}

	};
	
	private static OnCompletionListener mPlayerCompletionListener = new OnCompletionListener() {
		
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			stopPlay();
			UnityPlayer.UnitySendMessage("VoiceManager", "OnPlayComplete", "");
		}
	};
	
	private static  OnErrorListener mPlayerErrorListener = new OnErrorListener() {
		
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			stopPlay();
			return false;
		}
	};
	
	private static android.media.MediaRecorder.OnErrorListener mRecordErrorListener = new android.media.MediaRecorder.OnErrorListener() {
		
		public void onError(MediaRecorder mr, int what, int extra) {
			// TODO Auto-generated method stub
			stopRecord();
		}
	};

	public VoiceRecorder()
	{
	}

	public static void init(UnityPlayerActivity activity)
	{
		unityActivity = activity;
	}

	public static int getRecordSeconds()
	{
		return mRecordSeconds;
	}

	public static boolean isRecording()
	{
		return mStartRecording;
	}

	public static boolean isPlaying()
	{
		return (mPlayer!=null && mPlayer.isPlaying());
	}

	public static void startRecord(String path,int maxDuration)
	{
		try
		{
			if (mRecorder == null){
				mRecorder = new MediaRecorder();
			}
			
			mRecorder.reset();
			
			
			File sourceFile = new File(path);
			if (sourceFile.exists())
			{
				sourceFile.delete();
			}
			mRecorder.setAudioSource(1);
			mRecorder.setOutputFormat(3);
			mRecorder.setAudioEncoder(1);
			mRecorder.setOutputFile(sourceFile.getAbsolutePath());
			mRecorder.setMaxDuration(maxDuration);
			mRecorder.setAudioEncodingBitRate(4000);
			mRecorder.setAudioSamplingRate(8000);
			mRecorder.setOnInfoListener(mRecordInfoListener);
			mRecorder.setOnErrorListener(mRecordErrorListener);
		
			mRecorder.prepare();
			mRecorder.start();
			mStartRecording = true;
			mStartRecordTime = System.currentTimeMillis();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			stopRecord();
		}
	}

	public static void stopRecord()
	{
		try {
			if (mRecorder != null && mStartRecording)
			{
				mRecorder.stop();
				mRecorder.reset();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		mStartRecording = false;
		mRecordSeconds = (int)(System.currentTimeMillis() - mStartRecordTime) / 1000;
	}
	
	public static int getRecordRunTime()
	{
		if(mStartRecording)
		{
			return (int)(System.currentTimeMillis() - mStartRecordTime) / 1000;
		}
		return 0;
	}

	public static void startPlay(String path,float volume)
	{
		if (mPlayer == null)
		{	
			mPlayer = new MediaPlayer();
		}
			
		try
		{
		
			if(mPlayer.isPlaying())
			{
				mPlayer.stop();
				mPlayer.reset();
			}
			
			mPlayer.setDataSource(path);
			mPlayer.setVolume(volume, volume);
			mPlayer.setLooping(false);
				
			
			mPlayer.setOnCompletionListener(mPlayerCompletionListener);
			mPlayer.setOnErrorListener(mPlayerErrorListener);
			
			mPlayer.setOnPreparedListener(new OnPreparedListener() {
				
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mPlayer.start();
					
					int miliSeconds = mPlayer.getDuration();
					
					UnityPlayer.UnitySendMessage("VoiceManager", "OnBeginPlay", String.valueOf(miliSeconds));
				}
			});
			
			mPlayer.prepare();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			stopPlay();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
			stopPlay();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
			stopPlay();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			stopPlay();
		}
	}

	public static void stopPlay()
	{
		try {
			
			if (mPlayer != null)
			{
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}		
	}
}
