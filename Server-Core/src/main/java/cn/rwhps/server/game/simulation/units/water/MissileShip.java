package cn.rwhps.server.game.simulation.units.water;

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
public class MissileShip extends WaterUnit {
    Rect _srcRect = new Rect();
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_TURRET = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static PointF _pointF = new PointF();

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.missileShip;
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.scout_ship);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.scout_ship_dead);
        IMAGE_TURRET = game.graphics.loadImage(R.drawable.ship_scout_turret);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        if (this instanceof MissileShip) {
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

    public MissileShip() {
        this.objectWidth = 17;
        this.objectHeight = 29;
        this.radius = 15.0f;
        this.displayRadius = this.radius - 2.0f;
        this.maxHp = 260.0f;
        this.hp = this.maxHp;
        this.image = IMAGE;
    }

    @Override // com.corrodinggames.rts.game.units.water.WaterUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF getTurretEnd() {
        float tDir = this.dir;
        _pointF.set(this.x + (CommonUtils.cos(tDir) * 6.0f), this.y + (CommonUtils.sin(tDir) * 6.0f));
        return _pointF;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        PointF turretEnd = getTurretEnd();
        Projectile p = Projectile.createProjectile(this, turretEnd.x, turretEnd.y);
        p.color = Color.argb(255, 230, 230, 50);
        p.directDamage = 62.0f;
        p.target = target;
        p.lifeTimer = 190.0f;
        p.speed = 2.0f;
        p.ballistic = true;
        p.trailEffect = true;
        p.largeHitEffect = true;
        GameEngine game = GameEngine.getInstance();
        game.audio.playSound(AudioEngine.missile_fire, 0.8f, this.x, this.y);
        game.effects.emitLight(this.x, this.y, this.height, -1118720);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 180.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 160.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 1.2f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 1.9f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 99.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveAccelerationSpeed() {
        return 0.05f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveDecelerationSpeed() {
        return 0.1f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        super.draw(deltaSpeed);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return true;
    }
}
