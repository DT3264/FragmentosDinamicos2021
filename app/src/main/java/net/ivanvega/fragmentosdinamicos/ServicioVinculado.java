package net.ivanvega.fragmentosdinamicos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.MediaController;

import androidx.annotation.Nullable;

import java.io.IOException;

public class ServicioVinculado extends Service implements MediaController.MediaPlayerControl {
    String TAG = "ServicioVinculado";
    MediaPlayer mediaPlayer;

    Libro libro;
    int posLibro=0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
    }

    private final IBinder mBinder = new MiBinder();

    public class MiBinder extends Binder {
        public ServicioVinculado getService() {
            return ServicioVinculado.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    void prepareMediaPlayer(MediaPlayer.OnPreparedListener onPreparedListener, Libro libro) {
        this.libro = libro;
        Uri uri = Uri.parse(libro.getUrl());
        Log.d(TAG, "Preparando: " + uri.toString());
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
//                    ServicioVinculado.this.
                    Log.d(TAG, "Deteniendo serviucio");
                    ServicioVinculado.this.stopService(new Intent(ServicioVinculado.this, ServicioVinculado.class));
                }
            });
            mediaPlayer.setDataSource(getBaseContext(), uri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createNotificationChannel();
        setAsForeground();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        String CHANNEL_ID = "1000";
        String name = "Canal de audio";
        String description = "Notificación que muestra la canción actual";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
        NotificationManager notificationManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setAsForeground() {
        for(int i=0; i<Libro.ejemplosLibros().size(); i++){
            if(libro.getTitulo().equals(Libro.ejemplosLibros().elementAt(i).getTitulo())){
                Log.d("Servicio", "Pos: " + i);
                posLibro = i;
            }
        }

        Log.d("Servicio", "Pos: " + posLibro);
        String CHANNEL_ID = "1000";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("flag_servicio", true);
        notificationIntent.putExtra("pos", Integer.toString(posLibro));
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 2000, notificationIntent, PendingIntent.FLAG_MUTABLE);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(libro.getTitulo())
                    .setContentText(libro.getAutor())
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(pendingIntent)
                    .setTicker("Se inicio el servicio")
                    .build();
        }
        // Notification ID cannot be 0.
        startForeground(2000, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Apagando serviucio");
        mediaPlayer.stop();
        mediaPlayer.release();
        stopForeground(true);
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
