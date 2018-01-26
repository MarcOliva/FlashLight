package freecom.flashlight.marc.oliva.flashlightfree;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private String cameraId;
    //camera 2 API
    CameraManager cameraManager;
    private boolean lightOn = false;
    private boolean hasFlash = false;

    //old version
    private Camera objCamera;
    Camera.Parameters parametresCamera;

    TextView stateText;
    FrameLayout flashButton;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                prepareLight();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else {
            prepararFlashLight();
        }
    }

    //old version
    void getObjCamera() {
        if (objCamera == null) {
            try {
                objCamera = Camera.open();
            } catch (RuntimeException e) {
                Log.e("ANTIGUO", e.toString());
            }
        }
    }

    //old version
    void encenderFlash() {
        if (!lightOn) {
            if (objCamera == null || parametresCamera == null) {
                return;
            }
            parametresCamera = objCamera.getParameters();
            if (parametresCamera == null) {
                return;
            }
            parametresCamera.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            objCamera.setParameters(parametresCamera);
            objCamera.startPreview();
            lightOn = true;
        }
    }

    //old version
    void apagarFlash() {
        if (lightOn) {
            if (objCamera == null || parametresCamera == null) {
                return;
            }
            parametresCamera = objCamera.getParameters();
            if (parametresCamera == null) {
                return;
            }
            parametresCamera.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            objCamera.setParameters(parametresCamera);
            objCamera.stopPreview();
            lightOn = false;
        }
    }

    //old version
    void prepararFlashLight() {
        flashButton = findViewById(R.id.flashlight_button);
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA);
        if (!hasFlash) {
            flashButton.setEnabled(false);
            return;
        }
        getObjCamera();
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lightOn == false) {
                    encenderFlash();
                } else {
                    apagarFlash();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void flashOn() {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true);
            }
            lightOn = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void flashOff() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
            }
            lightOn = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void prepareLight() {
        flashButton = findViewById(R.id.flashlight_button);
        stateText = findViewById(R.id.state_flashlight_text_view);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        sharedPreferences = getPreferences(this.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        lightOn = sharedPreferences.getBoolean("state", false);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (lightOn == false) {
            lightOnConfig();
        } else {
            lightOffConfig();
        }
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lightOn == false) {
                    lightOnConfig();
                } else {
                    lightOffConfig();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void lightOnConfig() {
        flashOn();
        flashButton.setForeground(getResources().getDrawable(R.drawable.ic_power_settings_new_green_24dp));
        stateText.setText(getString(R.string.flashlight_state_on));
        stateText.setTextColor(getResources().getColor(R.color.color_on));
        editor.putBoolean("state", true);
        editor.commit();
        lightOn = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void lightOffConfig() {
        flashOff();
        flashButton.setForeground(getResources().getDrawable(R.drawable.ic_power_settings_new_red_24dp));
        stateText.setText(getString(R.string.flashlight_state_off));
        stateText.setTextColor(getResources().getColor(R.color.color_off));
        editor.putBoolean("state", false);
        editor.commit();
        lightOn = false;
    }

    @Override
    protected void onStop() {
        editor.putBoolean("state", false);
        editor.commit();
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        boolean state = sharedPreferences.getBoolean("state", false);
        if (state == false) {
            editor.putBoolean("state", true);
            editor.commit();
        }
        if (state == true) {
            editor.putBoolean("state", false);
            editor.commit();
        }

        super.onDestroy();
    }
}
