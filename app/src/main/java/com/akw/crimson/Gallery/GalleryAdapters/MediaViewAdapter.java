package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.akw.crimson.Backend.TouchImageView;

import java.util.ArrayList;

public class MediaViewAdapter extends PagerAdapter {
    private final Context mContext;
    private ArrayList<String> mediaUri=new ArrayList<>();

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    public MediaViewAdapter(Context applicationContext, ArrayList<String> mediaUri) {
        mContext=applicationContext;
        this.mediaUri=mediaUri;
    }

    @Override
    public int getCount() {
        return mediaUri.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

//        ImageView imageView = new ImageView(mContext);
//        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        imageView.setImageURI(Uri.parse(mediaUri.get(position)));
//
//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                ImageView view = (ImageView) v;
//                view.setScaleType(ImageView.ScaleType.MATRIX);
//                float scale;
//
//                dumpEvent(event);
//                // Handle touch events here...
//
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN: // first finger down only
//                        savedMatrix.set(matrix);
//                        start.set(event.getX(), event.getY());
//                        Log.d("MEDIA VIEW::::::", "mode=DRAG"); // write to LogCat
//                        mode = DRAG;
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_POINTER_UP:
//
//                        mode = NONE;
//                        Log.d("MEDIA VIEW::::::", "mode=NONE");
//                        break;
//
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        oldDist = spacing(event);
//                        Log.d("MEDIA VIEW::::::", "oldDist=" + oldDist);
//                        if (oldDist > 5f) {
//                            savedMatrix.set(matrix);
//                            midPoint(mid, event);
//                            mode = ZOOM;
//                            Log.d("MEDIA VIEW::::::", "mode=ZOOM");
//                        }
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//
//                        if (mode == DRAG) {
//                            matrix.set(savedMatrix);
//                            matrix.postTranslate(event.getX() - start.x, event.getY()
//                                    - start.y); /*
//                             * create the transformation in the matrix
//                             * of points
//                             */
//                        } else if (mode == ZOOM) {
//                            // pinch zooming
//                            float newDist = spacing(event);
//                            Log.d("MEDIA VIEW::::::", "newDist=" + newDist);
//                            if (newDist > 5f) {
//                                matrix.set(savedMatrix);
//                                scale = newDist / oldDist;
//                                /*
//                                 * setting the scaling of the matrix...if scale > 1 means
//                                 * zoom in...if scale < 1 means zoom out
//                                 */
//                                matrix.postScale(scale, scale, mid.x, mid.y);
//                            }
//                        }
//                        break;
//                }
//
//                view.setImageMatrix(matrix); // display the transformation on screen
//
//                return true;
//
//            }
//        });
//        container.addView(imageView, 0);
//        return imageView;


        TouchImageView imageView = new TouchImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(Uri.parse(mediaUri.get(position)));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }


    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Event", sb.toString());
    }

}