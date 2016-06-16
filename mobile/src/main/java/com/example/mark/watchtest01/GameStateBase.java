package com.example.mark.watchtest01;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mark on 6/15/2016.
 */
public class GameStateBase {
    public void Enter(com.example.mark.watchtest01.GameView.GameThread game) {}
    public boolean Update() {return true;}
    public void Exit() {}
    public void Draw(Canvas c) {}
    public boolean onTouch(View v, MotionEvent e) {return false;}
}
