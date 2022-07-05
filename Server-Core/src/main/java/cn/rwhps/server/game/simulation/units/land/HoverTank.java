package cn.rwhps.server.game.simulation.units.land;

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
public class HoverTank extends HoverUnit {
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_SHADOW = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    float heightCos = 0.0f;
    Rect _srcRect = new Rect();

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.hoverTank;
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.hover_tank);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.hover_tank_dead);
        IMAGE_SHADOW = game.graphics.loadImage(R.drawable.hover_tank_shadow);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        if (this instanceof HoverTank) {
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

    public HoverTank() {
        this.objectWidth = 20;
        this.objectHeight = 20;
        this.radius = 7.0f;
        this.displayRadius = this.radius + 2.0f;
        this.maxHp = 130.0f;
        this.hp = this.maxHp;
        this.image = IMAGE;
        this.imageShadow = IMAGE_SHADOW;
    }

    @Override // com.corrodinggames.rts.game.units.land.HoverUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        if (!this.dead && isActive()) {
            this.heightCos += 3.0f * deltaSpeed;
            if (this.heightCos > 360.0f) {
                this.heightCos -= 360.0f;
            }
            this.height = CommonUtils.toValue(this.height, 4.0f + (CommonUtils.sin(this.heightCos) * 1.5f), 0.1f * deltaSpeed);
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        PointF turretEnd = getTurretEnd();
        Projectile p = Projectile.createProjectile(this, turretEnd.x, turretEnd.y);
        p.color = Color.argb(255, 50, 230, 50);
        p.directDamage = 25.0f;
        p.target = target;
        p.lifeTimer = 60.0f;
        p.speed = 3.0f;
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLight(turretEnd.x, turretEnd.y, this.height, -14483678);
        game.audio.playSound(AudioEngine.plasma_fire2, 0.3f, turretEnd.x, turretEnd.y);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean isFixedFiring() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 140.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 80.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return isOverWater() ? 1.7f : 1.6f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 3.5f;
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
    public float getMoveAccelerationSpeed() {
        return 0.06f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveDecelerationSpeed() {
        return 0.09f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 2.5f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getDrawBaseDir() {
        return this.turretDir + 90.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        super.draw(deltaSpeed);
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
        return 2.0f;
    }
}
