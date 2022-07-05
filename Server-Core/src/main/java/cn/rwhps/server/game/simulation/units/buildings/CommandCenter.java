package cn.rwhps.server.game.simulation.units.buildings;

import android.graphics.Color;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.land.Builder;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.CommonUtils;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class CommandCenter extends Factory {
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static BitmapOrTexture IMAGE_WREAK = null;
    public static Unit.SpecialAction queueBuilder = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.CommandCenter.1
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Constructs and repairs buildings.\nCan not attack.";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Builder";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 300;
        }
    };
    float addDelay;
    float frameUpdate = 20.0f;
    int fakeFrame = 0;

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.base);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.base_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.commandCenter;
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

    public CommandCenter() {
        this.image = IMAGE;
        this.objectWidth = 53;
        this.objectHeight = 68;
        this.radius = 30.0f;
        this.displayRadius = this.radius;
        this.maxHp = 3000.0f;
        this.hp = this.maxHp;
        this.footprint.set(-1, -1, 1, 1);
        this.softFootprint.set(-1, -1, 1, 2);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        if (isActive() && !this.dead) {
            this.frameUpdate = CommonUtils.toZero(this.frameUpdate, deltaSpeed);
            if (this.frameUpdate == 0.0f) {
                this.frameUpdate = 5.0f;
                this.fakeFrame++;
                if (this.fakeFrame > 6) {
                    this.fakeFrame = 0;
                    this.frameUpdate = 70.0f;
                }
                if (this.fakeFrame <= 3) {
                    this.drawFrame = this.fakeFrame;
                } else {
                    this.drawFrame = 6 - this.fakeFrame;
                }
            }
            this.addDelay += deltaSpeed;
            if (this.addDelay > 60.0f) {
                this.addDelay -= 60.0f;
                this.team.credits += 10;
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public void completeQueueItem(Factory.BuildQueueItem item) {
        OrderableUnit unit = null;
        if (item.type == queueBuilder.getIndex()) {
            unit = new Builder();
        }
        unit.x = this.x;
        unit.y = this.y + 15.0f;
        unit.dir = 90.0f;
        unit.setTeam(this.team.id);
        unit.factoryExitDelay = 40.0f;
        unit.addMoveWaypoint(this.x, this.y + (this.radius * 3.0f));
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        Projectile p = Projectile.createProjectile(this, this.x, this.y);
        p.color = Color.argb(255, 230, 230, 50);
        p.directDamage = 70.0f;
        p.target = target;
        p.lifeTimer = 180.0f;
        p.speed = 3.0f;
        p.ballistic = true;
        p.trailEffect = true;
        p.largeHitEffect = true;
        GameEngine.getInstance().audio.playSound(AudioEngine.missile_fire, 0.8f, this.x, this.y);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 280.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 70.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 999.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean resetTurret() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.SpecialAction getSpecialAction(int index) {
        if (index == queueBuilder.getIndex()) {
            return queueBuilder;
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public int numSpecialActions() {
        return 1;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public int getSpecialActionFor(UnitType type) {
        if (type.equals(UnitType.builder)) {
            return queueBuilder.getIndex();
        }
        return -1;
    }
}
