package cn.rwhps.server.game.simulation.units.land;

import android.graphics.Color;
import android.graphics.PointF;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.EffectEngine;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class ArtilleryTank extends LandUnit {
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.artillery;
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.artillery1);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.artillery1_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        if (this instanceof ArtilleryTank) {
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

    public ArtilleryTank() {
        this.objectWidth = 19;
        this.objectHeight = 44;
        this.radius = 13.0f;
        this.displayRadius = this.radius;
        this.maxHp = 80.0f;
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
        p.directDamage = 70.0f;
        p.target = target;
        p.lifeTimer = 150.0f;
        p.speed = 3.5f;
        p.largeHitEffect = true;
        p.color = Color.argb(255, 190, 190, 80);
        p.drawSize = 2;
        GameEngine game = GameEngine.getInstance();
        game.audio.playSound(AudioEngine.cannon_firing, 0.3f, turretEnd.x, turretEnd.y);
        game.effects.emitSmallFlame(turretEnd.x, turretEnd.y, this.height, this.turretDir);
        EffectEngine.EffectObject effectObject = game.effects.emitLight(turretEnd.x, turretEnd.y, this.height, -1118482);
        if (effectObject != null) {
            effectObject.timer = 15.0f;
            effectObject.startTimer = effectObject.timer;
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public float getSecBar() {
        return this.currentShootDelay != 0.0f ? 1.0f - (this.currentShootDelay / getShootDelay()) : super.getSecBar();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 260.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 150.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 0.9f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 1.3f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 99.0f;
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
    public boolean isFixedFiring() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurretSize() {
        return 20.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveAccelerationSpeed() {
        return 0.05f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveDecelerationSpeed() {
        return 0.12f;
    }
}
