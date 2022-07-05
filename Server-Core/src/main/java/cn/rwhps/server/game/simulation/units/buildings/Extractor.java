package cn.rwhps.server.game.simulation.units.buildings;

import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.map.MapTile;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.CommonUtils;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class Extractor extends Factory {
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static BitmapOrTexture IMAGE_WREAK = null;
    public static int actionId_queueUpgrade = 0;
    static Unit.SpecialAction queueUpgrade = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.Extractor.1
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return Extractor.actionId_queueUpgrade;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "-Generates credits 50% faster";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Upgrade";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 700;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 5.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public boolean isActive(Unit unit) {
            Extractor extractor = (Extractor) unit;
            if (extractor.upgraded || extractor.getItemCountInQueue(getIndex()) > 0) {
                return false;
            }
            return super.isActive(unit);
        }
    };
    float addDelay;
    boolean upgraded;
    float frameUpdate = 0.0f;
    int fakeFrame = 0;
    Rect _dst = new Rect();
    Rect _src = new Rect();

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.extractor;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Building
    public boolean canBeBuiltHere() {
        GameEngine game = GameEngine.getInstance();
        game.map.toGrid(this.x, this.y);
        MapTile tile = game.map.getTileFromObjectLayer(game.map.returnX, game.map.returnY);
        if (tile == null || !tile.resPool) {
            return false;
        }
        return super.canBeBuiltHere();
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.extractor);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.extractor_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
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
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void setTeam(int id) {
        this.image = IMAGE_TEAMS[id];
        super.setTeam(id);
    }

    public Extractor() {
        this.image = IMAGE;
        this.objectWidth = 37;
        this.objectHeight = 50;
        this.radius = 18.0f;
        this.displayRadius = this.radius;
        this.maxHp = 800.0f;
        this.hp = this.maxHp;
        this.footprint.set(0, -1, 0, 0);
        this.softFootprint.set(this.footprint);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        if (isActive() && !this.dead) {
            this.frameUpdate = CommonUtils.toZero(this.frameUpdate, deltaSpeed);
            if (this.frameUpdate == 0.0f) {
                this.frameUpdate = 17.0f;
                this.fakeFrame++;
                if (this.fakeFrame > 7) {
                    this.fakeFrame = 0;
                }
                if (this.fakeFrame <= 3) {
                    this.drawFrame = this.fakeFrame;
                } else {
                    this.drawFrame = 7 - this.fakeFrame;
                }
            }
            this.addDelay += deltaSpeed;
            if (this.addDelay > 60.0f) {
                this.addDelay -= 60.0f;
                if (!this.upgraded) {
                    this.team.credits += 10;
                    return;
                }
                this.team.credits += 15;
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.buildings.Building, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        super.draw(deltaSpeed);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        throw new RuntimeException("Unit cannot shoot");
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 0.0f;
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
            this.maxHp += 200.0f;
            this.hp += 200.0f;
        }
    }
}
