package cn.rwhps.server.game.simulation.units.alien;

import cn.rwhps.server.game.simulation.units.Unit;
import cn.rwhps.server.game.simulation.units.UnitType;
import cn.rwhps.server.game.simulation.units.land.LandUnit;
import cn.rwhps.server.util.CommonUtils;
import com.corrodinggames.rts.game.Projectile;

/* loaded from: classes.dex */
public class LadyBug extends LandUnit {
    int drawFrame = 0;
    float attackedTimer = 0.0f;

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.ladybug;
    }

    public static void load() {
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        super.setTeam(id);
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public boolean destroyEffectAndWreak() {
        return false;
    }

    public LadyBug() {
        this.objectWidth = 17;
        this.objectHeight = 26;
        this.radius = 3.0f;
        this.displayRadius = this.radius + 5.0f;
        this.maxHp = 130.0f;
        this.hp = this.maxHp;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        if (this.moving) {
            if (this.drawFrame == 0) {
                this.drawFrame = 1;
            } else {
                this.drawFrame = 0;
            }
        }
        if (this.attackedTimer != 0.0f) {
            this.attackedTimer = CommonUtils.toZero(this.attackedTimer, deltaSpeed);
            this.drawFrame = 2;
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int getMoveSlidingDir() {
        return 181;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        Projectile.damageUnit(this, target, 14);
        this.attackedTimer = 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 43.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 17.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 1.7f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 5.5f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 99.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        if (shouldDrawCheck()) {
            super.draw(deltaSpeed);
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
        return !unit.isFlying();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurretSize() {
        return 7.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean isFixedFiring() {
        return true;
    }
}
