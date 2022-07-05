package cn.rwhps.server.game.simulation.units.land;

import android.graphics.PointF;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class LaserTank extends LandUnit {
    Rect _srcRect = new Rect();
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_TURRET = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.laserTank;
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.laser_tank_base);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.laser_tank_dead);
        IMAGE_TURRET = game.graphics.loadImage(R.drawable.laser_tank_turrent);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        if (this instanceof LaserTank) {
            this.image = IMAGE_TEAMS[id];
        }
        super.setTeam(id);
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public boolean destroyEffectAndWreak() {
        GameEngine game = GameEngine.getInstance();
        game.effects.emitSmallExplosion(this.x, this.y, this.height);
        this.image = IMAGE_WREAK;
        setDrawLayer(1);
        this.collidable = false;
        game.audio.playSound(AudioEngine.unit_explode, 0.8f, this.x, this.y);
        leaveScorchMark();
        return true;
    }

    public LaserTank() {
        this.objectWidth = 20;
        this.objectHeight = 20;
        this.radius = 9.0f;
        this.displayRadius = this.radius + 2.0f;
        this.maxHp = 300.0f;
        this.hp = this.maxHp;
        this.image = IMAGE;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        PointF turretEnd = getTurretEnd();
        Projectile p = Projectile.createProjectile(this, turretEnd.x, turretEnd.y);
        p.directDamage = 560.0f;
        p.target = target;
        p.lifeTimer = 8.0f;
        p.laserEffect = true;
        p.instant = true;
        p.largeHitEffect = true;
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLight(turretEnd.x, turretEnd.y, this.height, -1127220);
        game.effects.emitSmallFlame(turretEnd.x, turretEnd.y, this.height, this.turretDir);
        game.audio.playSound(AudioEngine.plasma_fire, 0.3f, turretEnd.x, turretEnd.y);
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public float getSecBar() {
        return this.currentShootDelay != 0.0f ? 1.0f - (this.currentShootDelay / getShootDelay()) : super.getSecBar();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 170.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 800.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 0.7f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 1.5f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        if (shouldDrawCheck()) {
            super.draw(deltaSpeed);
            if (!this.dead) {
                GameEngine game = GameEngine.getInstance();
                this._srcRect.set(0, 0, IMAGE_TURRET.getWidth(), IMAGE_TURRET.getHeight());
                game.graphics.drawImageCentered(IMAGE_TURRET, this._srcRect, this.x - GameEngine.getInstance().viewpointX_rounded, this.y - GameEngine.getInstance().viewpointY_rounded, this.turretDir, this._imagePaint);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveAccelerationSpeed() {
        return 0.07f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveDecelerationSpeed() {
        return 0.12f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttackUnit(Unit unit) {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurretSize() {
        return 10.0f;
    }
}
