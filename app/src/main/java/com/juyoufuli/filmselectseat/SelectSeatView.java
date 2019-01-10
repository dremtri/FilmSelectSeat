package com.juyoufuli.filmselectseat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author : dongfang
 * @Created Time : 2018-11-19  15:25
 * @Description:
 */
public class SelectSeatView extends View {
    private Context context;
    private Paint screenPaint;
    private Paint paintSeat;
    private TextPaint textPaint;

    /**
     * 座位的二维数组
     */
    private int[][] seatList;
    /**
     * 所有的矩形集合
     */
    private List<SelectRectBean> mRectList;
    /**
     * 选中的矩形
     */
    private List<SelectRectBean> selectList;
    /**
     * 当前点击的点
     */
    private Point currentPoint;

    private LayoutInflater inflater;
    private ChildSelectListener childSelectListener;
    private ScaleGestureDetector scaleGestureDetector;
    /**
     * 默认的标签间距
     */
    private int margiHorizontal = 10;
    /**
     * 座位宽度
     */
    private int seatWidth = 80;
    /**
     * 默认的行间距
     */
    private int margiVertical = 20;
    /**
     * 排号距离左边距离
     */
    private int margiLeft = seatWidth / 2;
    /**
     * 画布的矩阵
     */
    // 画布当前的 Matrix， 用于获取当前画布的一些状态信息，例如缩放大小，平移距离等
    private Matrix mCanvasMatrix = new Matrix();
    /**
     * 移动的距离
     */
    private float translateX, translateY;
    /**
     * 缩放的比例
     */
    private float scale;
    /**
     * --- 限制缩放比例 ---
     */
    private static final float MAX_SCALE = 3.0f;
    private static final float MIN_SCALE = 0.5f;
    private int measuredWidth;
    private int measuredHeight;
    private GestureDetector gestureDetector;
    /**
     * 排
     */
    private int row;
    /**
     * 列
     */
    private int column;
    /**
     * 空位置
     */
    public static int emptySeat = 0;
    /**
     * 默认座位
     */
    public static int normalSeat = 1;
    /**
     * 已卖座位
     */
    public static int sellSeat = 2;
    /**
     * 选中座位
     */
    public static int selectSeat = 3;
    /**
     * 屏幕高度
     */
    private static int screenHeight = 50;
    /**
     * 座位距离屏幕的距离
     */
    private static int marginTopScreen = 150;


    public void setChildSelectListener(ChildSelectListener childSelectListener) {
        this.childSelectListener = childSelectListener;
    }

    private int getRandom() {
        int endNum = 10;
        Random random = new Random();
        int result = random.nextInt(endNum);
        if (result == 0) {
            return 1;
        } else {
            return result;
        }
    }

    public SelectSeatView(@NonNull Context context) {
        this(context, null);
    }

    public SelectSeatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SelectSeatView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initData();
        init(context);
    }

    private void initData() {
        selectList = new ArrayList<>();
        mRectList = new ArrayList<>();
        currentPoint = new Point();
        seatList = new int[6][];
        //外层数组
        for (int i = 0; i < 6; i++) {
            row = 6;
//            int index = getRandom();
            int[] indes = new int[10];
            column = 10;
            for (int x = 0; x < 10; x++) {
                if (i == 0) {
                    if (x < 2 || x > 8) {
                        indes[x] = 0;
                    } else if (x == 4) {
                        indes[x] = 2;
                    } else {
                        indes[x] = 1;
                    }
                } else {
                    indes[x] = 1;
                }
            }
            seatList[i] = indes;
        }

    }

    private void init(Context context) {
        //初始化手势包括缩放和平移
        initGesture(context);
        inflater = LayoutInflater.from(context);
        screenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        screenPaint.setColor(Color.YELLOW);

        paintSeat = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSeat.setColor(Color.GREEN);

        textPaint = new TextPaint();
        textPaint.setTextSize(20);
        textPaint.setColor(Color.BLACK);
    }

    private void initGesture(Context context) {
        scale = 1.0f;
        //图片完全显示的伸缩值
        mCanvasMatrix.postTranslate(translateX, translateY);
        mCanvasMatrix.postScale(scale, scale);
        //缩放手势
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
//                LogUtil.i("focusX = " + detector.getFocusX());       // 缩放中心，x坐标
//                LogUtil.i("focusY = " + detector.getFocusY());       // 缩放中心y坐标
//                LogUtil.i("scale = " + detector.getScaleFactor());   // 缩放因子
                float scaleFactor = detector.getScaleFactor();
                //当前的缩放比例
//                scale = scaleFactor;
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
//                float[] points = mapPoint(fx, fy, mCanvasMatrix);
                float realScaleFactor = getRealScaleFactor(scaleFactor);
                mCanvasMatrix.postScale(realScaleFactor, realScaleFactor, 0, 0);
                invalidate();
                return true;
            }

        });
        //移动手势
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                LogUtil.i("onScroll = " + getMatrixTranslateX() + "---" + getMatrixTranslateY());
                if (getMatrixTranslateX() > (measuredWidth - seatWidth) * getMatrixScaleX()) {
                    mCanvasMatrix.setTranslate(0, 0);
                } else if (getMatrixTranslateY() > (measuredHeight - seatWidth) * getMatrixScaleY()) {
                    mCanvasMatrix.setTranslate(0, 0);
                } else {
                    mCanvasMatrix.postTranslate(-distanceX, -distanceY);
                }
                invalidate();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
//                LogUtil.i(event.getX() + "--onSingleTapConfirmed--" + event.getY());
                //这里做点击了处理，开始的x、y坐标
                float currentX = event.getX();
                float currentY = event.getY();
                //需要做坐标点转换,转换为原始的点，在进行点击事件
                LogUtil.i(currentX + "--before--" + currentY);
//              LogUtil.i(getMatrixScaleX() + "--scale--" + getMatrixScaleY());
//              LogUtil.i(getMatrixTranslateX() + "--translate--" + getMatrixTranslateY());
//                currentX = currentX * getMatrixScaleX() + 1 * getMatrixTranslateX();
//                currentY = currentY * getMatrixScaleY() + 1 * getMatrixTranslateY();
                LogUtil.i(currentX + "--after--" + currentY);
                currentPoint.set((int) currentX, (int) currentY);
                clickSeat(currentPoint);
                return super.onSingleTapConfirmed(event);
            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽和高都是AT_MOST，则设置宽度所有子元素的宽度的和；高度设置为第一个元素的高度；
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureWidth(heightMeasureSpec));
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
    }

    /**
     * 宽度计算
     *
     * @param widthMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = widthMeasureSpec;
        } else {
            result = 500;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.concat(mCanvasMatrix);
        //绘制座位
        drawSeatView(canvas);
        //绘制行数
        drawRowIndex(canvas);
        //绘制电影屏幕
        drawFilmScreen(canvas);
    }

    private void drawRowIndex(Canvas canvas) {
        Rect rect = new Rect(0, marginTopScreen-seatWidth, seatWidth, marginTopScreen + seatWidth * row);
        canvas.drawRect(rect, screenPaint);
        for (int i = 0; i < row; i++) {
            int startY;
            if (i == 0) {
                startY = marginTopScreen;
            } else {
                startY = i * seatWidth + marginTopScreen;
            }
            //绘制一下左边的排数
            canvas.drawText((i + 1) + "", margiLeft, startY == 0 ? seatWidth : startY, textPaint);
        }
    }

    private void drawFilmScreen(Canvas canvas) {
        float newLeft = (translateX - 150) * getMatrixScaleX();
        float newRight = (translateX - screenHeight * 2) * getMatrixScaleY();
        float newTop = (translateX + screenHeight * 2) * getMatrixScaleX();
        float newBottom = (translateX + 150) * getMatrixScaleX();
        Path path1 = new Path();
        path1.moveTo(newLeft, 0);
        path1.lineTo(newRight, screenHeight * getMatrixScaleY());
        path1.lineTo(newTop, screenHeight * getMatrixScaleY());
        path1.lineTo(newBottom, 0);
        path1.close();
        canvas.drawPath(path1, screenPaint);
        canvas.drawText("屏幕", translateX - screenHeight / 2, screenHeight / 2, textPaint);
    }

    private void drawSeatView(Canvas canvas) {
        //绘制多少排座位
        for (int i = 0; i < seatList.length; i++) {
            int startY;
            if (i == 0) {
                startY = marginTopScreen;
            } else {
                startY = i * seatWidth + marginTopScreen;
            }
            //每排多少座位
            for (int x = 0; x < seatList[i].length; x++) {
                int left;
                //开始绘制矩阵图
                if (i == 0) {
                    left = seatWidth * 2;
                } else {
                    left = (x + 1) * seatWidth;
                }
                int top = startY - seatWidth / 2 - margiVertical;
//                LogUtil.i(left + "--" + top + "--" + (left + seatWidth) + "--" + (top + seatWidth));
                int seatState = seatList[i][x];
                SelectRectBean selectRectBean = new SelectRectBean();
                Rect rect = new Rect(left + margiHorizontal, top + margiVertical, left + seatWidth, top + seatWidth);
                selectRectBean.setRect(rect);
                selectRectBean.setRow(i + 1);
                selectRectBean.setColumn(x + 1);
                selectRectBean.setSeatState(seatState);
                //需要计算从中间开始绘制座位
                switch (seatState) {
                    case 0:
                        paintSeat.setColor(Color.TRANSPARENT);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case 1:
                        paintSeat.setColor(Color.WHITE);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case 2:
                        paintSeat.setColor(Color.RED);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case 3:
                        paintSeat.setColor(Color.GREEN);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    default:
                        paintSeat.setColor(Color.TRANSPARENT);
                        canvas.drawRect(rect, paintSeat);
                        break;
                }

                //收集所有的位置信息
                mRectList.add(selectRectBean);

            }
        }
        //绘制变化的
        if (selectList.size() > 0) {
            for (int i = 0; i < selectList.size(); i++) {
                SelectRectBean selectRectBean = selectList.get(i);
                Rect rect = selectRectBean.getRect();
                paintSeat.setColor(Color.GREEN);
                canvas.drawRect(rect, paintSeat);
                canvas.drawText(selectRectBean.getRow() + "排" + selectRectBean.getColumn() + "列", rect.left, rect.top + seatWidth / 2, textPaint);
            }

        }


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //事件分发给手势处理器进行缩放和平移
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }


    private void clickSeat(Point currentPoint) {
        for (int i = 0; i < mRectList.size(); i++) {
            Rect rect = mRectList.get(i).getRect();
            SelectRectBean selectRectBean = mRectList.get(i);
            if (selectRectBean.getSeatState() == sellSeat) {
                continue;
            }
            float newLeft = rect.left * getMatrixScaleX() + 1 * getMatrixTranslateX();
            float newRight = rect.right * getMatrixScaleY() + 1 * getMatrixTranslateX();
            if (currentPoint.x > newLeft && currentPoint.x < newRight) {
                float newTop = rect.top * getMatrixScaleX() + 1 * getMatrixTranslateY();
                float newBottom = rect.bottom * getMatrixScaleY() + 1 * getMatrixTranslateY();
                if (currentPoint.y > newTop && currentPoint.y < newBottom) {
                    //点击到了某一个
                    if (selectList.contains(selectRectBean)) {
                        selectList.remove(selectRectBean);
                    } else {
                        selectList.add(selectRectBean);
                    }
                    //更新界面
                    invalidate();
                    break;
                }
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.i("onSizeChanged");
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
        //view的中心点
        translateX = measuredWidth / 2;
        translateY = measuredHeight / 2;
    }

    /**
     * --------------工具方法------------------
     */

    //获取屏幕尺寸
    private int[] getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int[] screenSize = new int[2];
        screenSize[0] = displayMetrics.widthPixels;
        screenSize[1] = displayMetrics.heightPixels;
        return screenSize;
    }


    float[] m = new float[9];

    private float getMatrixTranslateX() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MTRANS_X];
    }

    private float getMatrixTranslateY() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MTRANS_Y];
    }

    private float getMatrixScaleY() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MSCALE_Y];
    }

    private float getMatrixScaleX() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MSCALE_X];
    }

    private float getRealScaleFactor(float currentScaleFactor) {
        float realScale = 1.0f;
        // 用户当前的缩放比例
        float userScale = getMatrixScaleX();
        // 理论缩放数值
        float theoryScale = userScale * currentScaleFactor;
        // 如果用户在执行放大操作并且理论缩放数据大于4.0
        if (currentScaleFactor > 1.0f && theoryScale > MAX_SCALE) {
            realScale = MAX_SCALE / userScale;
        } else if (currentScaleFactor < 1.0f && theoryScale < MIN_SCALE) {
            realScale = MIN_SCALE / userScale;
        } else {
            realScale = currentScaleFactor;
        }
        return realScale;
    }

    //--- 将坐标转换为画布坐标 ---
    private float[] mapPoint(float x, float y, Matrix matrix) {
        float[] temp = new float[2];
        temp[0] = x;
        temp[1] = y;
        matrix.mapPoints(temp);
        return temp;
    }

    /**
     * 转换为新的数值
     *
     * @param num
     * @return
     */
    private float transformNewCoordX(float num) {
        return num * getMatrixScaleX() + 1 * getMatrixTranslateX();

    }

    private float transformNewCoordY(float num) {
        return num * getMatrixScaleY() + 1 * getMatrixTranslateY();

    }

    /**
     * 转换为原数值
     *
     * @param num
     * @return
     */
    private float transformOldCoordX(float num) {
        return num * getMatrixScaleX() - 1 * getMatrixTranslateX();

    }

    private float transformOldCoordY(float num) {
        return num * getMatrixScaleY() - 1 * getMatrixTranslateY();

    }
}

