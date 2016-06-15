package com.example.mark.watchtest01;

/**
 * Created by Mark on 6/9/2016.
 */
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public interface IGameState {
    public void Enter(GameView.GameThread game);

    // Update should return 'true' when the state has finished updating.
    public boolean Update();

    public void Exit();
    public void Draw(Canvas c);
    public boolean onTouch(View v, MotionEvent e);
}
