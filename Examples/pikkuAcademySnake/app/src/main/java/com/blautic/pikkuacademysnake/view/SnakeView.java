package com.blautic.pikkuacademysnake.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.blautic.pikkuacademysnake.data.GameType;
import com.blautic.pikkuacademysnake.data.GridPosition;
import com.blautic.pikkuacademysnake.data.GridSquare;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.blautic.pikkuacademysnake.utils.Utils.dp2px;
import static java.lang.Thread.sleep;

public class SnakeView extends View implements Runnable {

    private final List<List<GridSquare>> mGridSquare = new ArrayList<>();
    private final List<GridPosition> mSnakePositions = new ArrayList<>();
    private GridPosition mSnakeHeader;//Snake head position
    private GridPosition mFoodPosition;//Food position
    private final int LENGTH_INIT = 12;
    private int mSnakeLength = LENGTH_INIT;
    private long mSpeed = 6;
    private int mSnakeDirection = GameType.RIGHT;
    private volatile boolean mIsEndGame = false;
    private int mGridSize = 22;
    private final Paint mGridPaint = new Paint();
    private final Paint mStrokePaint = new Paint();
    private final int mRectSize = dp2px(getContext(), 18);
    private int mStartX, mStartY;
    private SnakeCallback callback;
    private Thread mGameThread = null;


    public SnakeView(Context context) {
        this(context, null);
    }

    public SnakeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnakeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        List<GridSquare> squares;
        for (int i = 0; i < mGridSize; i++) {
            squares = new ArrayList<>();
            for (int j = 0; j < mGridSize; j++) {
                squares.add(new GridSquare(GameType.GRID));
            }
            mGridSquare.add(squares);
        }
        mSnakeHeader = new GridPosition(10, 10);
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        mFoodPosition = new GridPosition(0, 0);
        mIsEndGame = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStartX = w / 2 - mGridSize * mRectSize / 2;
        mStartY = dp2px(getContext(), 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mStartY * 2 + mGridSize * mRectSize;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#2d2d2d"));
        mGridPaint.reset();
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Paint.Style.FILL);
        mGridPaint.setAntiAlias(true);

        mStrokePaint.reset();
        mStrokePaint.setColor(Color.GRAY);
        mStrokePaint.setStrokeWidth(5);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias(true);

        for (int i = 0; i < mGridSize; i++) {
            for (int j = 0; j < mGridSize; j++) {
                int left = mStartX + i * mRectSize;
                int top = mStartY + j * mRectSize;
                int right = left + mRectSize;
                int bottom = top + mRectSize;
                canvas.drawRect(left, top, right, bottom, mStrokePaint);
                mGridPaint.setColor(mGridSquare.get(i).get(j).getColor());
                canvas.drawRect(left, top, right, bottom, mGridPaint);
            }
        }
        canvas.drawRect(0, 0, getWidth(), getHeight(), mStrokePaint);

    }

    private void refreshFood(GridPosition foodPosition) {
        mGridSquare.get(foodPosition.getX()).get(foodPosition.getY()).setType(GameType.FOOD);
    }

    public void setSpeed(long speed) {
        mSpeed = speed;
    }

    public void setGridSize(int gridSize) {
        mGridSize = gridSize;
    }

    public void setSnakeDirection(int snakeDirection) {
        if (mSnakeDirection == GameType.RIGHT && snakeDirection == GameType.LEFT) return;
        if (mSnakeDirection == GameType.LEFT && snakeDirection == GameType.RIGHT) return;
        if (mSnakeDirection == GameType.TOP && snakeDirection == GameType.BOTTOM) return;
        if (mSnakeDirection == GameType.BOTTOM && snakeDirection == GameType.TOP) return;
        mSnakeDirection = snakeDirection;
    }

    @Override
    public void run() {
        while (!mIsEndGame) {
            moveSnake(mSnakeDirection);
            checkCollision();
            refreshGridSquare();
            handleSnakeTail();
            postInvalidate();//Redraw the interface
            try {
                sleep(1000 / mSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }



    public void setCallBack(SnakeCallback callBack) {
        this.callback = callBack;
    }

    //Collision detection
    private void checkCollision() {
        //Detecta si se muerde
        GridPosition headerPosition = mSnakePositions.get(mSnakePositions.size() - 1);
        for (int i = 0; i < mSnakePositions.size() - 2 && !mIsEndGame; i++) {
            GridPosition position = mSnakePositions.get(i);
            if (headerPosition.getX() == position.getX() && headerPosition.getY() == position.getY()) {
                //Se ha mordido, detén el juego
                mIsEndGame = true;
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onGameOver(mSnakeLength - LENGTH_INIT);
                        }
                    });
                }
                return;
            }
        }

        //Determine si ha comido
        if (headerPosition.getX() == mFoodPosition.getX()
                && headerPosition.getY() == mFoodPosition.getY()) {
            mSnakeLength++;
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCatchFood(mSnakeLength - LENGTH_INIT);
                    }
                });
            }
            generateFood();
        }
    }

    public void stopGame() {
        mIsEndGame = true;
        try {
            if(mGameThread!= null) mGameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void reStartGame() {
        stopGame();
        if (!mIsEndGame) return;
        for (List<GridSquare> squares : mGridSquare) {
            for (GridSquare square : squares) {
                square.setType(GameType.GRID);
            }
        }
        if (mSnakeHeader != null) {
            mSnakeHeader.setX(10);
            mSnakeHeader.setY(10);
        } else {
            mSnakeHeader = new GridPosition(10, 10);//La posición inicial de la serpiente.
        }
        mSnakePositions.clear();
        mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
        mSnakeLength = LENGTH_INIT;//La longitud de la serpiente
        mSnakeDirection = GameType.RIGHT;
        mSpeed = 6;//velocidad
        if (mFoodPosition != null) {
            mFoodPosition.setX(5);
            mFoodPosition.setY(5);
        } else {
            mFoodPosition = new GridPosition(0, 0);
        }
        refreshFood(mFoodPosition);
        mIsEndGame = false;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    //Generar comida
    private void generateFood() {
        Random random = new Random();
        int foodX = random.nextInt(mGridSize - 1);
        int foodY = random.nextInt(mGridSize - 1);
        for (int i = 0; i < mSnakePositions.size() - 1; i++) {
            if (foodX == mSnakePositions.get(i).getX() && foodY == mSnakePositions.get(i).getY()) {
                //No se puede generar en serpientes
                foodX = random.nextInt(mGridSize - 1);
                foodY = random.nextInt(mGridSize - 1);
                //Reciclar
                i = 0;
            }
        }
        mFoodPosition.setX(foodX);
        mFoodPosition.setY(foodY);
        refreshFood(mFoodPosition);
    }

    private void moveSnake(int snakeDirection) {
        switch (snakeDirection) {
            case GameType.LEFT:
                if (mSnakeHeader.getX() - 1 < 0) {//Juicio de límites: si llega al lado más a la izquierda, déjelo cruzar la pantalla hacia el lado más a la derecha
                    mSnakeHeader.setX(mGridSize - 1);
                } else {
                    mSnakeHeader.setX(mSnakeHeader.getX() - 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
            case GameType.TOP:
                if (mSnakeHeader.getY() - 1 < 0) {
                    mSnakeHeader.setY(mGridSize - 1);
                } else {
                    mSnakeHeader.setY(mSnakeHeader.getY() - 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
            case GameType.RIGHT:
                if (mSnakeHeader.getX() + 1 >= mGridSize) {
                    mSnakeHeader.setX(0);
                } else {
                    mSnakeHeader.setX(mSnakeHeader.getX() + 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
            case GameType.BOTTOM:
                if (mSnakeHeader.getY() + 1 >= mGridSize) {
                    mSnakeHeader.setY(0);
                } else {
                    mSnakeHeader.setY(mSnakeHeader.getY() + 1);
                }
                mSnakePositions.add(new GridPosition(mSnakeHeader.getX(), mSnakeHeader.getY()));
                break;
        }
    }

    private void refreshGridSquare() {
        for (GridPosition position : mSnakePositions) {
            if (mIsEndGame) break;
            mGridSquare.get(position.getX()).get(position.getY()).setType(GameType.SNAKE);
        }
    }

    private void handleSnakeTail() {
        int snakeLength = mSnakeLength;
        for (int i = mSnakePositions.size() - 1; i >= 0 && !mIsEndGame; i--) {
            if (snakeLength > 0) {
                snakeLength--;
            } else {//Establezca la cuadrícula que excede la longitud en GameType.GRID
                GridPosition position = mSnakePositions.get(i);
                mGridSquare.get(position.getX()).get(position.getY()).setType(GameType.GRID);
            }
        }
        snakeLength = mSnakeLength;
        for (int i = mSnakePositions.size() - 1; i >= 0 && !mIsEndGame; i--) {
            if (snakeLength > 0) {
                snakeLength--;
            } else {
                mSnakePositions.remove(i);
            }
        }
    }

}
