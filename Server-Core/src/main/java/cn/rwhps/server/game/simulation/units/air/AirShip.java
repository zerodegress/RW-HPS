package cn.rwhps.server.game.simulation.units.air;

import android.graphics.Color;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.CommonUtils;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class AirShip extends AirUnit {
    float animFrame;
    float rotorSpeed = 0.18f;

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.airShip;
    }

    public static void load() {
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
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

    public AirShip() {
        this.objectWidth = 24;
        this.objectHeight = 22;
        this.radius = 11.0f;
        this.displayRadius = this.radius + 2.0f;
        this.maxHp = 250.0f;
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
            this.height = CommonUtils.toValue(this.height, 20.0f, 0.3f * deltaSpeed);
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        Projectile p = Projectile.createProjectile(this, this.x, this.y);
        p.height = this.height;
        p.directDamage = 30.0f;
        p.target = target;
        p.lifeTimer = 70.0f;
        p.speed = 5.0f;
        p.drawSize = 3;
        p.color = Color.argb(255, 0, 0, 170);
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLight(this.x, this.y, this.height, -16776978);
        game.audio.playSound(AudioEngine.plasma_fire, 0.2f, this.x, this.y);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 140.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 30.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 2.2f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 6.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean isFixedFiring() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveAccelerationSpeed() {
        return 0.03f;
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
        return unit.isFlying();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int getMoveSlidingDir() {
        return 181;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void rotateBody(float diff) {
        this.dir += diff;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getDrawBaseDir() {
        return this.turretDir + 90.0f;
    }
}
