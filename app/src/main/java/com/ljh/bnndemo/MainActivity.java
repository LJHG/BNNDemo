package com.ljh.bnndemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils utils = new Utils();

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        //tv.setText(testForJNI());
        //Student stu  = new Student();
        //stu.setValue("yoyochecknow");
        //tv.setText(stu.getValue());
        Net net = new Net();
//        net.initialize();
//        net.predict();
        tv.setText(net.testForJNI());

        //load model sort of things(solution from https://stackoverflow.com/questions/65273837/android-native-file-read-from-assets-folder-by-tflite-buildfromfile)
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = getResources().getAssets().openFd("quicknet.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer modelBuffer = null;
        try {
            modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //load model
        net.loadModel(modelBuffer);

        //fill in input
        float input[]  = new float[150528];
        for(int i=0;i<50176;i++)
        {
            input[i*3 + 0] = 1;
            input[i*3 + 1] = 2;
            input[i*3 + 2] = 3;
        }
        //predict
        long start=System.currentTimeMillis();
        float[] output = net.predict(input);
        long end = System.currentTimeMillis();
        System.out.println("Inference time: "+(end-start)+" ms");

        //present output
        System.out.println("print top 5");
        int[] indexs = utils.getTopNFromArray(output,1000,5);
        for(int i=0;i<5;i++){
            System.out.println(indexs[i]);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}