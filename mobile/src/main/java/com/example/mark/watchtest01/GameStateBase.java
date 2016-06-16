package com.example.mark.watchtest01;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mark on 6/15/2016.
 */
public class GameStateBase {
    private static Rect _bounds = new Rect();

    protected GameView.GameThread game = null;

    public GameStateBase(GameView.GameThread gameThread) {
        game = gameThread;
    }

    public void Enter(GameView.GameThread game) {}
    public boolean Update() {return true;}
    public void Exit() {}
    public void Draw(Canvas c) {}
    public boolean onTouch(View v, MotionEvent e) {return false;}

    protected void DrawTextCentered(Canvas c, String text) {
        GameView._paint.getTextBounds(text, 0, text.length(), _bounds);

        int x = (c.getWidth() / 2) - (_bounds.width() / 2);
        int y = (c.getHeight() / 2) - (_bounds.height() / 2) - 12;
        c.drawText(text, x, y, GameView._paint);
    }

    protected void DrawTextCenteredAtY(Canvas c, String text, int y) {
        GameView._paint.getTextBounds(text, 0, text.length(), _bounds);

        int x = (c.getWidth() / 2) - (_bounds.width() / 2);
        c.drawText(text, x, y, GameView._paint);
    }
}
