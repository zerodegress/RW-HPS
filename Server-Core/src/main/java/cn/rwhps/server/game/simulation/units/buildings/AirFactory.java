package cn.rwhps.server.game.simulation.units.buildings;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.air.AirShip;
import com.corrodinggames.rts.game.units.air.GunShip;
import com.corrodinggames.rts.game.units.air.Helicopter;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.CommonUtils;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class AirFactory extends Factory {
    float frameUpdate = 0.0f;
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static BitmapOrTexture IMAGE_WREAK = null;
    static Unit.SpecialAction queueHelicopter = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.AirFactory.1
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground and air.";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Helicopter";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 500;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.002f;
        }
    };
    static Unit.SpecialAction queueInterceptor = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.AirFactory.2
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 1;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack air only.";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Interceptor";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 600;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.002f;
        }
    };
    static Unit.SpecialAction queueGunShip = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.AirFactory.3
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 2;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground only\nStrong attack\nHeavily armored";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Gun Ship";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 800;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.001f;
        }
    };

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.air_factory);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.air_factory_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.airFactory;
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

    public AirFactory() {
        this.image = IMAGE;
        this.objectWidth = 40;
        this.objectHeight = 61;
        this.radius = 30.0f;
        this.displayRadius = this.radius;
        this.maxHp = 1000.0f;
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
                this.frameUpdate = 27.0f;
                this.drawFrame++;
                if (this.drawFrame > 4) {
                    this.drawFrame = 0;
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public void completeQueueItem(Factory.BuildQueueItem item) {
        OrderableUnit unit = null;
        if (item.type == queueHelicopter.getIndex()) {
            unit = new Helicopter();
        }
        if (item.type == queueInterceptor.getIndex()) {
            unit = new AirShip();
        }
        if (item.type == queueGunShip.getIndex()) {
            unit = new GunShip();
        }
        unit.x = this.x;
        unit.y = this.y + 5.0f;
        unit.dir = 90.0f;
        unit.turretDir = 90.0f;
        unit.setTeam(this.team.id);
        unit.factoryExitDelay = 50.0f;
        unit.addMoveWaypoint(this.x, this.y + (this.radius * 3.0f));
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.SpecialAction getSpecialAction(int index) {
        if (index == queueHelicopter.getIndex()) {
            return queueHelicopter;
        }
        if (index == queueInterceptor.getIndex()) {
            return queueInterceptor;
        }
        if (index == queueGunShip.getIndex()) {
            return queueGunShip;
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public int getSpecialActionFor(UnitType type) {
        if (type == UnitType.helicopter) {
            return queueHelicopter.getIndex();
        }
        if (type == UnitType.airShip) {
            return queueInterceptor.getIndex();
        }
        if (type == UnitType.gunShip) {
            return queueGunShip.getIndex();
        }
        return -1;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public int numSpecialActions() {
        return 3;
    }
}
