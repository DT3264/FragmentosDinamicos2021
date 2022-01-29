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

import java.io.IOException;

public class ServicioVinculado extends Service implements MediaController.MediaPlayerControl {
    private final IBinder mBinder = new MiBinder();
    String TAG = "ServicioVinculado";
    MediaPlayer mediaPlayer;
    Libro libro;
    int posLibro = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    void prepareMediaPlayer(MediaPlayer.OnPreparedListener onPreparedListener, Libro libro) {
        this.libro = libro;
        Uri uri = Uri.parse(libro.getUrl());
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            Intent stopIntent =
                    new Intent(ServicioVinculado.this, ServicioVinculado.class);
            mediaPlayer.setOnCompletionListener(
                    mediaPlayer ->
                            ServicioVinculado.this.stopService(stopIntent)
            );
            mediaPlayer.setDataSource(getBaseContext(), uri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        definePosLibro();
        createNotificationChannel();
        setAsForeground();
    }

    private void definePosLibro() {
        for (int i = 0; i < Libro.ejemplosLibros().size(); i++) {
            if (libro.getTitulo().equals(Libro.ejemplosLibros().elementAt(i).getTitulo())) {
                posLibro = i;
                break;
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "1000";
            String name = "Canal de audio";
            String description = "Notificación que muestra la canción actual";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setAsForeground() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }
        String CHANNEL_ID = "1000";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("flag_servicio", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2000,
                    notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(libro.getTitulo())
                .setContentText(libro.getAutor())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
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

    public class MiBinder extends Binder {
        public ServicioVinculado getService() {
            return ServicioVinculado.this;
        }
    }
}
