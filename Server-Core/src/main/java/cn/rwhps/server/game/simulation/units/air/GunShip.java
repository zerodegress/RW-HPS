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
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;

/* loaded from: classes.dex */
public class GunShip extends AirUnit {
    float animFrame;
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_SHADOW = null;
    static BitmapOrTexture IMAGE_TURRET = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    float heightCos = 0.0f;
    Rect _temp = new Rect();

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.airShip;
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.gunship);
        IMAGE_SHADOW = game.graphics.loadImage(R.drawable.gunship_shadow);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.gunship_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        if (this instanceof GunShip) {
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

    public GunShip() {
        this.objectWidth = 25;
        this.objectHeight = 35;
        this.radius = 16.0f;
        this.displayRadius = this.radius + 0.0f;
        this.maxHp = 260.0f;
        this.hp = this.maxHp;
        this.image = IMAGE;
        this.imageShadow = IMAGE_SHADOW;
        this.height = 0.0f;
        setDrawLayer(4);
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
            this.heightCos += 2.0f * deltaSpeed;
            if (this.heightCos > 360.0f) {
                this.heightCos -= 360.0f;
            }
            this.height = CommonUtils.toValue(this.height, 20.0f + (CommonUtils.sin(this.heightCos) * 1.5f), 0.1f * deltaSpeed);
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF getTurretEnd() {
        float tSize = getTurretSize();
        float tDir = this.dir;
        _pointF.set(this.x + (CommonUtils.cos(tDir) * tSize), this.y + (CommonUtils.sin(tDir) * tSize));
        return _pointF;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        PointF turretEnd = getTurretEnd();
        Projectile p = Projectile.createProjectile(this, turretEnd.x, turretEnd.y);
        p.height = this.height;
        p.color = Color.argb(255, (int) NetworkEngine.PACKET_SEND_KICK, 230, 40);
        p.directDamage = 35.0f;
        p.target = target;
        p.lifeTimer = 80.0f;
        p.speed = 4.0f;
        p.drawSize = 2;
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLight(turretEnd.x, turretEnd.y, this.height, -1127220);
        game.effects.emitSmallFlame(turretEnd.x, turretEnd.y, this.height, this.turretDir);
        game.audio.playSound(AudioEngine.firing4, 0.3f, this.x, this.y);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 140.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 40.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 1.4f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 99.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean isFixedFiring() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int getMoveSlidingDir() {
        return 180;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveAccelerationSpeed() {
        return 0.2f;
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
    public boolean canAttackUnit(Unit unit) {
        return !unit.isFlying();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurretSize() {
        return 15.0f;
    }
}
