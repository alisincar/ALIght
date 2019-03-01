package com.mozared.alight;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private ImageButton btnSwitch;
    private Camera camera;
    private boolean isFlashOn,hasFlash;
    private Parameters params;
    private int CAMERA_IZIN = 0;
    private TextView seekDurum, baslat, durdur;
    private int SEEK_DURUM;
    private SeekBar seekBar;
    CountDownTimer cTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        // flash switch button
        btnSwitch = findViewById(R.id.btnSwitch);
        seekDurum = findViewById(R.id.seekDurum);
        baslat = findViewById(R.id.baslat);
        durdur = findViewById(R.id.durdur);

		/*
         * First check if device is supporting flashlight or not
		 */
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Hata");
            alert.setMessage("Üzgünüm cihazınız Flash Işık desteklemiyor");
            alert.setButton("Tamam", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
            return;
        }

        // get the camera
        getCamera();

        // displaying button image
        toggleButtonImage();

		/*
         * Switch button click event to toggle flash on/off
		 */
        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    // turn off flash
                    turnOffFlash();
                    cancelTimer();
                } else {
                    // turn on flash
                    turnOnFlash();
                }
            }
        });


        baslat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

        durdur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelTimer();
            }
        });
    }


    /*
     * Get the camera
     */
    private void getCamera() {
        if (camera == null) {

            if (CAMERA_IZIN == 0) {
                IzinKontroEt();
            } else {
                try {
                    camera = Camera.open();
                    params = camera.getParameters();
                } catch (RuntimeException e) {
                    Log.e("FailedtoOpenCamera: ", e.getMessage());
                }
            }
        }
    }

    /*
     * Turning On flash
     */
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound

            playSound(1);
            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
            // changing button/switch image
            toggleButtonImage();
        }
    }

    /*
     * Turning Off flash
     */
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound
            playSound(1);
            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
            // changing button/switch image
            toggleButtonImage();
        }
    }

    /*
     * Playing sound will play button toggle sound on flash on / off
     */
    private void playSound(int music) {

       /* if (isFlashOn) {
            mp = MediaPlayer.create(MainActivity.this, R.raw.button);
        } else {
            mp = MediaPlayer.create(MainActivity.this, R.raw.button);
        }*/
        MediaPlayer mp;
        if (music == 1) {
            mp = MediaPlayer.create(MainActivity.this, R.raw.button);
        } else {
            mp = MediaPlayer.create(MainActivity.this, R.raw.seekbar);
        }
        mp.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }

    /*
     * Toggle switch button images changing image states to on / off
     */
    private void toggleButtonImage() {
        if (isFlashOn) {
            btnSwitch.setImageResource(R.drawable.btn_switch_on);
        } else {
            btnSwitch.setImageResource(R.drawable.btn_switch_off);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // on pause turn off the flash
        // turnOffFlash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // on resume turn on the flash
        if (hasFlash)
            turnOnFlash();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // on starting the app get the camera params
        getCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // on stop release the camera
  /*      if (camera != null) {
            camera.release();
            camera = null;
        }*/
    }

    private void IzinKontroEt() {
        String[] izinler = {
                android.Manifest.permission.CAMERA
        };
        int izinKodu = 67;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //-- Eğer almak istediğimiz izinler daha önceden kullanıcı tarafından onaylanmış ise bu kısımda istediğimiz işlemleri yapabiliriz..
                //-- Mesela uygulama açılışında SD Kart üzerindeki herhangi bir dosyaya bu kısımda erişebiliriz.
                CAMERA_IZIN = 1;
            } else {
                //-- Almak istediğimiz izinler daha öncesinde kullanıcı tarafından onaylanmamış ise bu kod bloğu harekete geçecektir.
                //-- Burada requestPermissions() metodu ile kullanıcıdan ilgili Manifest izinlerini onaylamasını istiyoruz.
                requestPermissions(izinler, izinKodu);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 67: {
                //-- Kullanıcı izin isteğini iptal ederse if - else bloğunun içindeki kodlar çalışmayacaktır. Böyle bir durumda yapılacak işlemleri bu kısımda kodlayabilirsiniz.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //-- Eğer kullanıcı istemiş olduğunuz izni onaylarsa bu kod bloğu çalışacaktır.
                    CAMERA_IZIN = 1;
                    getCamera();
                    Toast.makeText(MainActivity.this, "Kameraya izin verdiniz uygulama ayarlarından istediğiniz zaman izni kaldırabilirsiniz.", Toast.LENGTH_LONG).show();
                } else {
                    //-- Kullanıcı istemiş olduğunuz izni reddederse bu kod bloğu çalışacaktır.
                    Toast.makeText(MainActivity.this, "Flash özelliğine erişebilmemiz için Kameraya izin vermelisiniz ", Toast.LENGTH_SHORT).show();
                }
            }
            //-- Farklı 'case' blokları ekleyerek diğer izin işlemlerinizin sonuçlarını da kontrol edebilirsiniz.. Biz burada sadece değerini 67 olarak tanımladığımız izin işlemini kontrol ettik.
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBarr, int progress, boolean b) {
        if (progress == 0) {
            seekBar.setProgress(1);
        } else {
            playSound(2);
            seekDurum.setText(progress + " dk");
            SEEK_DURUM = progress;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    //start timer function
    public void startTimer() {
        baslat.setVisibility(View.GONE);
        durdur.setVisibility(View.VISIBLE);
        turnOnFlash();
        seekBar.setEnabled(false);
        if(SEEK_DURUM==0){
            SEEK_DURUM=1;
        }
        int time = SEEK_DURUM * 60000;
        cTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished == 60000 || millisUntilFinished < 60000) {
                    seekDurum.setText(millisUntilFinished / 1000 + " sn kaldı");
                } else {
                    seekDurum.setText(millisUntilFinished / 60000 + " dk " + (millisUntilFinished % 60000) / 1000 + " sn kaldı");
                    seekBar.setProgress((int) (millisUntilFinished / 60000));
                }
            }

            public void onFinish() {
                baslat.setVisibility(View.VISIBLE);
                durdur.setVisibility(View.GONE);
                seekBar.setProgress(1);
                seekBar.setEnabled(true);
                seekDurum.setText("Tamamlandı");
                turnOffFlash();
            }
        };
        cTimer.start();
    }


    //cancel timer
    public void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
        seekBar.setEnabled(true);
        seekBar.setProgress(1);
        seekDurum.setText("1 dk");
        baslat.setVisibility(View.VISIBLE);
        durdur.setVisibility(View.GONE);
        turnOffFlash();
    }
}
