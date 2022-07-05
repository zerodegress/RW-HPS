package cn.rwhps.server.game.simulation.units.buildings;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.land.Hovercraft;
import com.corrodinggames.rts.game.units.water.GunBoat;
import com.corrodinggames.rts.game.units.water.MissileShip;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class SeaFactory extends Factory {
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_BACK = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static BitmapOrTexture IMAGE_WREAK = null;
    static Unit.SpecialAction queueGunBoat = new SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.SeaFactory.1
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Fast\nCan attack ground";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Gun Boat";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 400;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.01f;
        }
    };
    static Unit.SpecialAction queueMissileShip = new SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.SeaFactory.2
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 1;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground and air";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Missile Ship";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 900;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.005f;
        }
    };
    static Unit.SpecialAction queueHovercraft = new SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.SeaFactory.3
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return 2;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Transports units\nAble to move over land and water.\nCan not attack";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Hovercraft";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 800;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.003f;
        }
    };

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.sea_factory);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.sea_factory);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.seaFactory;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Building
    public boolean destroyEffectAndWreakForBuilding() {
        GameEngine game = GameEngine.getInstance();
        game.effects.emitLargeExplosion(this.x, this.y, this.height);
        this.image_back = null;
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

    public SeaFactory() {
        this.image = IMAGE;
        this.objectWidth = 73;
        this.objectHeight = 80;
        this.radius = 40.0f;
        this.displayRadius = this.radius;
        this.maxHp = 1000.0f;
        this.hp = this.maxHp;
        setDrawLayer(2);
        this.footprint.set(-1, -1, 1, 1);
        this.softFootprint.set(-1, -1, 1, 2);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public void completeQueueItem(BuildQueueItem item) {
        OrderableUnit unit = null;
        if (item.type == queueGunBoat.getIndex()) {
            unit = new GunBoat();
        }
        if (item.type == queueMissileShip.getIndex()) {
            unit = new MissileShip();
        }
        if (item.type == queueHovercraft.getIndex()) {
            unit = new Hovercraft();
        }
        unit.x = this.x;
        unit.y = this.y + 5.0f;
        unit.dir = 90.0f;
        unit.turretDir = 90.0f;
        unit.setTeam(this.team.id);
        unit.factoryExitDelay = 40.0f;
        unit.addMoveWaypoint(this.x, this.y + (this.radius * 3.0f));
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.SpecialAction getSpecialAction(int index) {
        if (index == queueGunBoat.getIndex()) {
            return queueGunBoat;
        }
        if (index == queueMissileShip.getIndex()) {
            return queueMissileShip;
        }
        if (index == queueHovercraft.getIndex()) {
            return queueHovercraft;
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Building, com.corrodinggames.rts.game.units.Unit
    public int getBuildRange() {
        return 180;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public int numSpecialActions() {
        return 3;
    }
}
