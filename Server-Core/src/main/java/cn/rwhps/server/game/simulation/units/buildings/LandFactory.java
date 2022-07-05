package cn.rwhps.server.game.simulation.units.buildings;

import cn.rwhps.server.game.simulation.units.land.*;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class LandFactory extends Factory {
    static BitmapOrTexture IMAGE = null;
    static BitmapOrTexture IMAGE_BACK = null;
    static BitmapOrTexture[] IMAGE_TEAMS = new BitmapOrTexture[8];
    static BitmapOrTexture IMAGE_WREAK = null;
    public static int actionId_queueBuilder = 0;
    public static int actionId_queueTank = 1;
    public static int actionId_queueArtillery = 2;
    public static int actionId_queueMegaTank = 3;
    public static int actionId_queueHoverTank = 4;
    public static int actionId_queueLaserTank = 5;
    static Unit.SpecialAction queueBuilder = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.LandFactory.1
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return LandFactory.actionId_queueBuilder;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return CommandCenter.queueBuilder.getDescription();
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return CommandCenter.queueBuilder.getText();
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return CommandCenter.queueBuilder.getPrice();
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.005f;
        }
    };
    static Unit.SpecialAction queueTank = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.LandFactory.2
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return LandFactory.actionId_queueTank;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground only";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Tank";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 200;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.004f;
        }
    };
    static Unit.SpecialAction queueArtillery = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.LandFactory.3
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return LandFactory.actionId_queueArtillery;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground only\nLong range";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Artillery";
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
    static Unit.SpecialAction queueMegaTank = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.LandFactory.4
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return LandFactory.actionId_queueMegaTank;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "heavily armed\nCan attack ground\nLight air attack";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Mega Tank";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 800;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.0015f;
        }
    };
    static Unit.SpecialAction queueHoverTank = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.LandFactory.5
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return LandFactory.actionId_queueHoverTank;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground only\nAble to move over water";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Hover Tank";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 500;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.005f;
        }
    };
    static Unit.SpecialAction queueLaserTank = new Factory.SpecialQueueAction() { // from class: com.corrodinggames.rts.game.units.buildings.LandFactory.6
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getIndex() {
            return LandFactory.actionId_queueLaserTank;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getDescription() {
            return "Can attack ground and air.\nWeak vs multiple units\nPowerful single shot, slow recharge";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public String getText() {
            return "Laser Tank";
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int getPrice() {
            return 1200;
        }

        @Override // com.corrodinggames.rts.game.units.buildings.Factory.SpecialQueueAction
        public float getBuildSpeed() {
            return 0.002f;
        }
    };

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE = game.graphics.loadImage(R.drawable.land_factory_front);
        IMAGE_BACK = game.graphics.loadImage(R.drawable.land_factory_back);
        IMAGE_WREAK = game.graphics.loadImage(R.drawable.land_factory_dead);
        for (int n = 0; n < IMAGE_TEAMS.length; n++) {
            IMAGE_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.landFactory;
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
        this.image_back = IMAGE_BACK;
        super.setTeam(id);
    }

    public LandFactory() {
        this.image = IMAGE;
        this.objectWidth = 50;
        this.objectHeight = 70;
        this.radius = 30.0f;
        this.displayRadius = this.radius;
        this.maxHp = 1000.0f;
        this.hp = this.maxHp;
        setDrawLayer(3);
        this.footprint.set(-1, -1, 1, 1);
        this.softFootprint.set(-1, -1, 1, 2);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public void completeQueueItem(Factory.BuildQueueItem item) {
        OrderableUnit unit = null;
        if (item.type == queueBuilder.getIndex()) {
            unit = new Builder();
        }
        if (item.type == queueTank.getIndex()) {
            unit = new Tank();
        }
        if (item.type == queueArtillery.getIndex()) {
            unit = new ArtilleryTank();
        }
        if (item.type == queueMegaTank.getIndex()) {
            unit = new MegaTank();
        }
        if (item.type == queueHoverTank.getIndex()) {
            unit = new HoverTank();
        }
        if (item.type == queueLaserTank.getIndex()) {
            unit = new LaserTank();
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
        if (index == queueBuilder.getIndex()) {
            return queueBuilder;
        }
        if (index == queueTank.getIndex()) {
            return queueTank;
        }
        if (index == queueArtillery.getIndex()) {
            return queueArtillery;
        }
        if (index == queueMegaTank.getIndex()) {
            return queueMegaTank;
        }
        if (index == queueHoverTank.getIndex()) {
            return queueHoverTank;
        }
        if (index == queueLaserTank.getIndex()) {
            return queueLaserTank;
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Factory
    public int getSpecialActionFor(UnitType type) {
        if (type == UnitType.builder) {
            return queueBuilder.getIndex();
        }
        if (type == UnitType.tank) {
            return queueTank.getIndex();
        }
        if (type == UnitType.artillery) {
            return queueArtillery.getIndex();
        }
        if (type == UnitType.megaTank) {
            return queueMegaTank.getIndex();
        }
        if (type == UnitType.hoverTank) {
            return queueHoverTank.getIndex();
        }
        if (type == UnitType.laserTank) {
            return queueLaserTank.getIndex();
        }
        return -1;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public int numSpecialActions() {
        return 6;
    }
}
