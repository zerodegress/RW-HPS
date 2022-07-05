package cn.rwhps.server.game.simulation.units.air;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.CommonUtils;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class Helicopter extends AirUnit {
    float animFrame;
    float fallSpeed;
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_SHADOW = null;
    static BitmapOrTexture IMAGE_TURRET = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    boolean hitGround = false;
    float heightCos = 0.0f;
    Rect _temp = new Rect();
    float rotorSpeed = 0.14f;

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.helicopter;
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.helicopter);
        IMAGE_SHADOW = game.graphics.loadImage(R.drawable.helicopter_shadow);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.helicopter_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        if (this instanceof Helicopter) {
            this.image = IMAGE_TEAMS[id];
        }
        super.setTeam(id);
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public boolean destroyEffectAndWreak() {
        GameEngine.getInstance().effects.emitSmallExplosion(this.x, this.y, this.height);
        this.image = IMAGE_WREAK;
        setDrawLayer(1);
        this.collidable = false;
        return true;
    }

    public Helicopter() {
        this.objectWidth = 42;
        this.objectHeight = 42;
        this.radius = 13.0f;
        this.displayRadius = this.radius + 2.0f;
        this.maxHp = 150.0f;
        this.hp = this.maxHp;
        this.image = IMAGE;
        this.imageShadow = IMAGE_SHADOW;
        this.height = 0.0f;
        setDrawLayer(4);
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void fromLoadedMap() {
        super.fromLoadedMap();
        this.height = 20.0f;
        this.rotorSpeed = 0.5f;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canMove() {
        return this.height > 15.0f;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.Unit
    public boolean isFlying() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.air.AirUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        GameEngine.getInstance();
        if (!this.dead) {
            this.rotorSpeed = CommonUtils.toValue(this.rotorSpeed, 0.5f, 0.003f * deltaSpeed);
            this.animFrame += this.rotorSpeed * deltaSpeed;
            if (this.animFrame >= 4.0f) {
                this.animFrame = 0.0f;
            }
            if (this.rotorSpeed > 0.4f) {
                this.heightCos += 2.0f * deltaSpeed;
                if (this.heightCos > 360.0f) {
                    this.heightCos -= 360.0f;
                }
                this.height = CommonUtils.toValue(this.height, 20.0f + (CommonUtils.sin(this.heightCos) * 1.5f), 0.1f * deltaSpeed);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        PointF turretEnd = getTurretEnd();
        Projectile p = Projectile.createProjectile(this, turretEnd.x, turretEnd.y);
        p.height = this.height;
        p.directDamage = 17.0f;
        p.target = target;
        p.lifeTimer = 30.0f;
        p.speed = 8.0f;
        p.visible = false;
        p.color = Color.argb(255, 180, 180, 0);
        p.instant = true;
        p.hitSound = false;
        GameEngine game = GameEngine.getInstance();
        game.audio.playSound(AudioEngine.gun_fire, 0.2f, turretEnd.x, turretEnd.y);
        game.effects.emitSmallFlame(turretEnd.x, turretEnd.y, this.height, this.turretDir);
        game.effects.emitLight(turretEnd.x, turretEnd.y, this.height, -1118720);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 130.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 60.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 2.4f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 8.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int getMoveSlidingDir() {
        return 180;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 12.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Rect getImageSrcRect(boolean shadow) {
        if (this.dead && !shadow) {
            return super.getImageSrcRect(shadow);
        }
        int offsetX = 1 + (((int) this.animFrame) * (this.objectWidth + 1));
        this._temp.set(offsetX, 1, this.objectWidth + offsetX, this.objectHeight + 1);
        return this._temp;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        super.draw(deltaSpeed);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveAccelerationSpeed() {
        return 0.02f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveDecelerationSpeed() {
        return 0.1f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurretSize() {
        return 5.0f;
    }
}
