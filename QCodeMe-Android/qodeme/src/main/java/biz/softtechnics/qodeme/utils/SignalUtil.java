package biz.softtechnics.qodeme.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/15/13
 * Time: 2:19 PM
 */
public final class SignalUtil {

    public static void signal(Context context){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp", "Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                defaultVibrate(context);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp","Normal mode");
                defaultSound(context);
                break;
        }
    }

    public static void vibrate(Context context, long[] pattern){
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);
    }

    public static void defaultVibrate(Context context){
        vibrate(context, new long[]{ 0,300,200,300,200 });
    }

    public static void sound(Context context, Uri ringtoneUri){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, ringtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void defaultSound(Context context){
        sound(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

}
