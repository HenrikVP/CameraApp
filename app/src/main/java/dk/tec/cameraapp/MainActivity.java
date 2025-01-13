package dk.tec.cameraapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageButton captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.view_preview);
        captureButton = findViewById(R.id.btn_captureButton);
        captureButton.setOnClickListener(view -> takePhoto());


        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            // Permission already granted
            startCamera();
           // openCamera();
        }
    }

    private void openCamera() {
        // Your logic to open the camera
        Toast.makeText(this, "Camera is ready to use", Toast.LENGTH_SHORT).show();
    }


    private void takePhoto() {
        if (imageCapture == null) return;

//        // Create time-stamped output file to hold the image
//        File photoFile = new File(getExternalFilesDir(null),
//                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
//                        .format(System.currentTimeMillis()) + ".jpg");

        File photoFile = createImageFile();

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take a picture
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String msg = "Photo capture succeeded: " + photoFile.getAbsolutePath();
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                Log.d("CameraXApp", msg);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraXApp", "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //String url = Environment.DIRECTORY_PICTURES;
        //File storageDir = getExternalFilesDir("Pictures");
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);


        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // ImageCapture
                imageCapture = new ImageCapture.Builder().build();

                // Select back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraXApp", "Use case binding failed", e);
            }

        }, ContextCompat.getMainExecutor(this));
    }
}