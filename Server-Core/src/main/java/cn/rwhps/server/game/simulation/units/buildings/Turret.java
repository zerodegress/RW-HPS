package cn.rwhps.server.game.simulation.units.buildings;

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
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;

/* loaded from: classes.dex */
public class Turret extends Factory {
    Rect _srcRect = new Rect();
    boolean upgraded;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_TURRET = null;
    static BitmapOrTexture IMAGE_TURRET_L2 = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static BitmapOrTexture IMAGE_WREAK = null;
    static BitmapOrTexture IMAGE_ICON = null;
    static BitmapOrTexture[] IMAGE_ICON_TEAMS = new BitmapOrTexture[8];
    public static int actionId_queueUpgrade = 0;
    static Unit.SpecialAction queueUpgrade = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.Turret.1
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return Turret.actionId_queueUpgrade;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "-Increases HP, attack damage, and range";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Upgrade";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 600;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.001f;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public boolean isActive(Unit unit) {
            Turret turret = (Turret) unit;
            if (turret.upgraded || turret.getItemCountInQueue(getIndex()) > 0) {
                return false;
            }
            return super.isActive(unit);
        }
    };

    @Override // com.corrodinggames.rts.game.units.buildings.Building, com.corrodinggames.rts.game.units.Unit
    public BitmapOrTexture getIcon() {
        return IMAGE_ICON_TEAMS[this.team.id];
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.turret_base);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.turret_base_dead);
        IMAGE_TURRET = game.graphics.loadImage(R.drawable.turret_top);
        IMAGE_TURRET_L2 = game.graphics.loadImage(R.drawable.turret_top_l2);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
        IMAGE_ICON = game.graphics.loadImage(R.drawable.unit_icon_building_turrent);
        for (int n2 = 0; n2 < IMAGE_ICON_TEAMS.length; n2++) {
            IMAGE_ICON_TEAMS[n2] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE_ICON.bitmap, n2));
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Building
    public boolean destroyEffectAndWreakForBuilding() {
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLargeExplosion(this.x, this.y, this.height);
        this.image = IMAGE_WREAK;
        setDrawLayer(1);
        this.collidable = false;
        game.audio.playSound(AudioEngine.building_explode, 0.8f, this.x, this.y);
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        this.image = IMAGE_TEAMS[id];
        super.setTeam(id);
    }

    public Turret() {
        this.objectWidth = 35;
        this.objectHeight = 42;
        this.radius = 15.0f;
        this.displayRadius = this.radius;
        this.maxHp = 700.0f;
        this.hp = this.maxHp;
        this.image = IMAGE;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        PointF turretEnd = getTurretEnd();
        Projectile p = Projectile.createProjectile(this, turretEnd.x, turretEnd.y);
        p.target = target;
        p.lifeTimer = 60.0f;
        if (!this.upgraded) {
            p.speed = 4.0f;
            p.color = Color.argb(255, 100, 30, 30);
            p.directDamage = 41.0f;
        } else {
            p.speed = 5.0f;
            p.color = Color.argb(255, 40, 30, (int) NetworkEngine.PACKET_REGISTER_CONNECTION);
            p.directDamage = 44.0f;
        }
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLight(turretEnd.x, turretEnd.y, this.height, -1127220);
        game.effects.emitSmallFlame(turretEnd.x, turretEnd.y, this.height, this.turretDir);
        game.audio.playSound(AudioEngine.firing3, 0.3f, turretEnd.x, turretEnd.y);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return !this.upgraded ? 165.0f : 185.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return !this.upgraded ? 30.0f : 20.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean resetTurret() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.buildings.Building, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        if (shouldDrawCheck()) {
            super.draw(deltaSpeed);
            if (!this.dead) {
                drawTurret();
            }
        }
    }

    void drawTurret() {
        BitmapOrTexture turret;
        GameEngine game = GameEngine.getInstance();
        if (!this.upgraded) {
            turret = IMAGE_TURRET;
        } else {
            turret = IMAGE_TURRET_L2;
        }
        this._srcRect.set(0, 0, turret.getWidth(), turret.getHeight());
        game.graphics.drawImageCentered(turret, this._srcRect, this.x - GameEngine.getInstance().viewpointX_rounded, this.y - GameEngine.getInstance().viewpointY_rounded, this.turretDir, getBuildingPaint());
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.turret;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttackUnit(Unit unit) {
        return !unit.isFlying();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurretSize() {
        return 23.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public void completeQueueItem(Factory.BuildQueueItem item) {
        if (item.type == queueUpgrade.getIndex()) {
            upgrade();
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public int numSpecialActions() {
        return 1;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.SpecialAction getSpecialAction(int index) {
        if (index == queueUpgrade.getIndex()) {
            return queueUpgrade;
        }
        return null;
    }

    public void upgrade() {
        if (!this.upgraded) {
            this.upgraded = true;
            this.maxHp += 400.0f;
            this.hp += 400.0f;
        }
    }
}
