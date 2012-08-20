package jp.soha.tool;


import android.app.Activity;
import android.os.Bundle;

public class MyCameraActivity extends Activity {
    private CameraView cameraView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        cameraView = new CameraView(this);
        setContentView(cameraView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }
}