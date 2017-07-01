package com.android.facemask.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.android.facemask.R;

import java.util.logging.Logger;

/**
 * Created by shashank on 26/6/17.
 */
public class GoggleGraphics extends GraphicOverlay.Graphic {
    private static final String TAG = "GoggleGraphics";
    private volatile PointF mLeftPosition;

    private volatile PointF mRightPosition;

    private Bitmap mGoggleBitmap = null;
    private float prevAngle = 0;
    private PointF prevLeftPos = new PointF();

    public GoggleGraphics(Bitmap bitmap, GraphicOverlay overlay) {
        super(overlay);
        mGoggleBitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null) || (mGoggleBitmap == null)) {
            return;
        }

        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        // Use the inter-eye distance to set the size of the eyes.
        float distance = (float) Math.sqrt(
                Math.pow(rightPosition.x - leftPosition.x, 2) +
                        Math.pow(rightPosition.y - leftPosition.y, 2));
        Log.d(TAG, "distance " + distance);
        int newWidth = (int)(distance * 2.5);
        Bitmap bitmap = null;
        int newHeight = (int)((mGoggleBitmap.getHeight() * (newWidth)) / (float) mGoggleBitmap.getWidth());
        bitmap = Bitmap.createScaledBitmap(mGoggleBitmap, newWidth, newHeight, true);
        Log.d(TAG, "Rendered Image Bitmap Size " + newWidth + " , " + newHeight);

        int left = bitmap.getWidth() / 3;

        float angle = getAngle(leftPosition, rightPosition);
        double angleInRadian = Math.atan2(leftPosition.y - rightPosition.y, rightPosition.x - leftPosition.x);

        Log.d(TAG, "angle -> " + String.valueOf(angle));

        canvas.drawBitmap(bitmap, leftPosition.x - left, leftPosition.y - bitmap.getHeight() / 2, null);


        if (Math.abs(angle - prevAngle) > 2.0f) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-angle, leftPosition.x, leftPosition.y);
            canvas.setMatrix(matrix);
            left = (int) (left / Math.cos(angleInRadian));
            canvas.drawBitmap(bitmap, leftPosition.x - left, leftPosition.y - bitmap.getHeight() / 2, null);
            prevAngle = angle;
            prevLeftPos = leftPosition;
        } else {
            Matrix matrix = new Matrix();
            matrix.postRotate(-prevAngle, prevLeftPos.x, prevLeftPos.y);
            canvas.setMatrix(matrix);
            left = (int) (left / Math.cos(Math.toRadians(prevAngle)));
            canvas.drawBitmap(bitmap, prevLeftPos.x - left, prevLeftPos.y - bitmap.getHeight() / 2, null);
        }

    }

    void updateEyes(PointF leftPosition, PointF rightPosition) {
        mLeftPosition = leftPosition;

        mRightPosition = rightPosition;

        postInvalidate();
    }

    private float getAngle(PointF leftPosition, PointF rightPosition ) {
        float angle = (float) Math.toDegrees(Math.atan2(leftPosition.y - rightPosition.y, rightPosition.x - leftPosition.x));

        if(angle < 0){
            angle += 360;
        }
        return angle;
    }

}
