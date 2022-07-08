/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util;

import cn.rwhps.server.game.replay.block.PointF;

import java.util.Random;

public class CommonUtils {
    static final Random realRandom = new Random();

    public static final float square(float value) {
        return (float) Math.sqrt(value);
    }

    public static final float toZero(float value, float diff) {
        if (value > diff) {
            return value - diff;
        }
        if (value < (-diff)) {
            return value + diff;
        }
        return 0.0f;
    }

    public static final float toValue(float value, float target, float diff) {
        if (value > target + diff) {
            return value - diff;
        }
        return value < target - diff ? value + diff : target;
    }

    public static final float limit(float value, float limit) {
        return value > limit ? limit : value < (-limit) ? -limit : value;
    }

    public static final float limit(float value, float min, float max) {
        return value > max ? max : value < min ? min : value;
    }

    public static final int limitInt(int value, int min, int max) {
        return value > max ? max : value < min ? min : value;
    }

    public static final int roundDown(float value) {
        if (value > 0.0f) {
            return (int) value;
        }
        if (value < 0.0f) {
            return ((int) value) - 1;
        }
        return 0;
    }

    public static final float distanceSq(float x, float y, float x2, float y2) {
        return ((x - x2) * (x - x2)) + ((y - y2) * (y - y2));
    }

    public static final float distance(float x, float y, float x2, float y2) {
        return (float) Math.sqrt(((x - x2) * (x - x2)) + ((y - y2) * (y - y2)));
    }

    public static final float fixDir(float dir, boolean mode) {
        if (!mode) {
            while (true) {
                if (dir <= 180.0f && dir >= -180.0f) {
                    break;
                }
                if (dir > 180.0f) {
                    dir -= 360.0f;
                }
                if (dir < -180.0f) {
                    dir += 360.0f;
                }
            }
        } else {
            while (true) {
                if (dir <= 360.0f && dir >= 0.0f) {
                    break;
                }
                if (dir > 360.0f) {
                    dir -= 360.0f;
                }
                if (dir < 0.0f) {
                    dir += 360.0f;
                }
            }
        }
        return dir;
    }

    public static final float getRotationDir(float source, float target, float speed) {
        float diff = fixDir(fixDir(target, true) - fixDir(source, true), false);
        return diff > speed ? speed : diff < (-speed) ? -speed : diff;
    }

    public static final float getDirection(float x, float y, float tx, float ty) {
        return (float) Math.toDegrees(Math.atan2(ty - y, tx - x));
    }

    public static final boolean lineIntersectLine(PointF v1, PointF v2, PointF v3, PointF v4) {
        float denom = ((v4.y - v3.y) * (v2.x - v1.x)) - ((v4.x - v3.x) * (v2.y - v1.y));
        float numerator = ((v4.x - v3.x) * (v1.y - v3.y)) - ((v4.y - v3.y) * (v1.x - v3.x));
        float numerator2 = ((v2.x - v1.x) * (v1.y - v3.y)) - ((v2.y - v1.y) * (v1.x - v3.x));
        if (denom == 0.0f) {
            return (numerator == 0.0f && numerator2 == 0.0f) ? false : false;
        }
        float ua = numerator / denom;
        float ub = numerator2 / denom;
        return ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f;
    }

    public static final float rnd(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }

    public static final float sameRnd(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }

    public static final float realRnd(float min, float max) {
        return (realRandom.nextFloat() * (max - min)) + min;
    }

    public static final int realRand(int max) {
        return realRandom.nextInt(max);
    }

    public static final float cos(float dir) {
        return (float) StrictMath.cos(StrictMath.toRadians(dir));
    }

    public static final float sin(float dir) {
        return (float) StrictMath.sin(StrictMath.toRadians(dir));
    }

    public static final float abs(float val) {
        return val < 0.0f ? -val : val;
    }

    public static final int abs(int val) {
        return val < 0 ? -val : val;
    }

    public static final boolean close(float val, float val2) {
        return abs(val - val2) < 0.05f;
    }
}
