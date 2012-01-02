/*
Copyright 2011 codeoedoc

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.box.game.planeandroid;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/*
 * Bullet class handles bullet movement and redering on the canvas
 */
public class Bullet {

    private Canvas c;
    private Drawable bullet;
    private int[][] Locus;
    private Random rnd;
    private int SWidth, SHeight;
    private int bulletWidth, bulletHeight;
    private int bulletSpeed;

    public Bullet(Drawable bullet, int SWidth, int SHeight, int bulletNum,
            int bulletSpeed) {
        this.bullet = bullet;
        Locus = new int[bulletNum][5];
        rnd = new Random();
        this.SWidth = SWidth;
        this.SHeight = SHeight;
        this.bulletWidth = bullet.getIntrinsicWidth();
        this.bulletHeight = bullet.getIntrinsicHeight();
        this.bulletSpeed = bulletSpeed;
    }

    public void setCanvas(Canvas c) {
        this.c = c;
    }

    public void InitBulletsLocus() {
        for (int i = 0; i < Locus.length; i++) {
            InitBulletLocus(i);
        }
    }

    private void InitBulletLocus(int i) {

        Locus[i][0] = (rnd.nextInt() & 0x7fffffff) % 4;
        switch (Locus[i][0]) {
            case 0:
                Locus[i][1] = -5;
                Locus[i][2] = (rnd.nextInt() & 0x7fffffff) % SHeight;
                Locus[i][3] = (rnd.nextInt() & 0x7fffffff) % this.bulletSpeed
                        + 1;
                Locus[i][4] = (rnd.nextInt()) % this.bulletSpeed;
                break;
            case 1:
                Locus[i][1] = SWidth + 5;
                Locus[i][2] = (rnd.nextInt() & 0x7fffffff) % SHeight;
                Locus[i][3] = ((rnd.nextInt() & 0x7fffffff) % this.bulletSpeed + 1)
                        * -1;
                Locus[i][4] = (rnd.nextInt()) % this.bulletSpeed;
                break;
            case 2:
                Locus[i][1] = (rnd.nextInt() & 0x7fffffff) % SWidth;
                Locus[i][2] = -5;
                Locus[i][3] = (rnd.nextInt()) % this.bulletSpeed;
                Locus[i][4] = (rnd.nextInt() & 0x7fffffff) % this.bulletSpeed
                        + 1;
                break;
            case 3:
                Locus[i][1] = (rnd.nextInt() & 0x7fffffff) % SWidth;
                Locus[i][2] = SHeight + 5;
                Locus[i][3] = (rnd.nextInt()) % this.bulletSpeed;
                Locus[i][4] = ((rnd.nextInt() & 0x7fffffff) % this.bulletSpeed + 1)
                        * -1;
                break;
        }
    }

    public boolean paint(int planeX, int planeY) {

        boolean r = false;

        for (int i = 0; i < Locus.length; i++) {

            if (isCollision(planeX, planeY, i, 10)) {
                r = true;
                break;
            }
            bullet.setBounds(Locus[i][1], Locus[i][2], Locus[i][1]
                    + bulletWidth, Locus[i][2] + bulletHeight);
            bullet.draw(c);
            UpdataBulletLocus(i);
        }
        return r;
    }

    private void UpdataBulletLocus(int i) {
        Locus[i][1] += Locus[i][3];
        Locus[i][2] += Locus[i][4];
        if (Locus[i][1] < -5 || Locus[i][1] > SWidth + 5) {
            Locus[i][3] *= -1;
        }
        if (Locus[i][2] < -5 || Locus[i][2] > SHeight + 5) {
            Locus[i][4] *= -1;
        }
    }

    private boolean isCollision(int planeX, int planeY, int i, int range) {
        boolean result = false;

        int planeXCenter = planeX + 12;
        int planeYCenter = planeY + 12;

        int bulletXCenter = Locus[i][1] + 3;
        int bulletYCenter = Locus[i][2] + 3;

        if (Math.abs(planeXCenter - bulletXCenter) < range) {
            if (Math.abs(planeYCenter - bulletYCenter) < range) {
                result = true;
            }
        }
        return result;
    }

    public void BombBullets(int planeX, int planeY) {
        for (int i = 0; i < Locus.length; i++) {
            if (isCollision(planeX, planeY, i, 32)) {
                InitBulletLocus(i);
            }
        }
    }

}
