
package net.gabuchan.androidrecipe.view;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private Camera mCamera;
    private Camera.Size mPreviewSize;

    public CameraPreview(Context context) {
        super(context);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // 非推奨メソッドだけどAndroid 3.0未満のためにもセット
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        // 今回は縦固定なのでプレビューを90度回転する
        mCamera.setDisplayOrientation(90);
        // アスペクト比が4:3のプレビューサイズを取得する
        mPreviewSize = get4x3PreviewSize(camera);
        if (getHolder() != null) {
            try {
                mCamera.setPreviewDisplay(getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surfaceのサイズを出してみる
        Log.d(TAG, String.format("Before width=%d, height=%d", width, height));

        // プレビューサイズをログに出してみる
        // ちなみに、Galaxy NexusのデフォルトのPreviewSize width=640, height=480
        Log.d(TAG, String.format("PreviewSize width=%d, height=%d",
                mPreviewSize.width, mPreviewSize.height));

        // プレビューサイズのアスペクト比を計算して
        float ratio = mPreviewSize.width / (float) mPreviewSize.height;
        // Surfaceのサイズを補正
        height = (int) (width * ratio);
        // LayoutParamsをセットし直す
        LayoutParams params = getLayoutParams();
        // 補正後のSurfaseのサイズ
        Log.d(TAG, String.format("New width=%d, height=%d", width, height));
        params.width = width;
        params.height = height;
        // 補正後のLayoutParamsをセット
        setLayoutParams(params);
    }

    private Camera.Size get4x3PreviewSize(Camera camera) {
        List<Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size maxSize = null;

        // 16:9ではなくて4:3で最大のサイズを探す
        for (Camera.Size size : sizes) {
            Log.d(TAG, String.format("SupportedPreviewSize %d x %d", size.width, size.height));
            if (size.width / (float) size.height > 1.4) {
                // 少なくとも1.4以上は16:9なのでスキップ
                continue;
            }
            if (maxSize == null) {
                // 4:3で1つ目の場合はここ
                // アス比をチェックせずにループの前で初期値を入れると16:9が選ばれる場合があるので注意
                maxSize = size;
                continue;
            }
            if (maxSize.width < size.width) {
                // 大きければmaxSizeに入れる
                maxSize = size;
            }
        }
        return maxSize;
    }
}
