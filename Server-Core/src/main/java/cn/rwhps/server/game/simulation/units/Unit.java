package cn.rwhps.server.game.simulation.units;

import cn.rwhps.server.data.global.Data;
import cn.rwhps.server.data.player.Player;
import cn.rwhps.server.game.simulation.PhysicalObject;
import cn.rwhps.server.game.simulation.units.air.AirUnit;
import cn.rwhps.server.game.simulation.units.buildings.Building;
import cn.rwhps.server.game.simulation.units.land.HoverUnit;
import cn.rwhps.server.game.simulation.units.land.LandUnit;
import cn.rwhps.server.game.simulation.units.water.WaterUnit;
import cn.rwhps.server.io.GameInputStream;
import cn.rwhps.server.io.GameOutputStream;
import cn.rwhps.server.util.CommonUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

/* loaded from: classes.dex */
public abstract class Unit extends PhysicalObject {
    public static final float seaFloorHeight = -10.0f;
    public float dir;
    public float displayRadius;
    public Unit dontCollideExit;
    public float dontCollideExitDelay;
    public float factoryExitDelay;
    public float hp;
    public float maxHp;
    public boolean miniMapCacheDraw;
    public int miniMapXCache;
    public int miniMapYCache;
    public boolean moving;
    public float radius;
    public boolean selected;
    public Player team;
    public boolean tempBoolean;
    public float tempFloat;
    public int tempInt;
    public float turretDir;
    public float turretLockDelay;
    public static ArrayList<Unit> fastUnitList = new ArrayList<>();
    public static ArrayList<Unit> fastLiveUnitList = new ArrayList<>();
    public static HashMap<UnitType, Unit> sharedUnitTypeList = new HashMap<>();

    public boolean createdFromMap = false;
    public Unit dontCollideWith = null;
    public boolean collidable = true;
    public boolean dead = false;
    public long dead_time = 0;
    public float xPush = 0.0f;
    public float yPush = 0.0f;
    public float xSpeed = 0.0f;
    public float ySpeed = 0.0f;
    public float speed = 0.0f;
    public float built = 1.0f;
    public boolean preview = false;
    public boolean previewValidSpot = false;
    public Unit transportedBy = null;

    /* loaded from: classes.dex */
    public enum MovementType {
        NONE,
        LAND,
        AIR,
        WATER,
        HOVER
    }

    public abstract MovementType getMovementType();

    public abstract UnitType getUnitType();

    public abstract boolean isFlying();

    public abstract boolean isOnScreen();

    @Override // com.corrodinggames.rts.gameFramework.SyncedObject
    public void writeOut(GameOutputStream stream) throws IOException {
        throw new RuntimeException("not completed");
    }

    @Override // com.corrodinggames.rts.gameFramework.SyncedObject
    public void readIn(GameInputStream stream) throws IOException {
        throw new RuntimeException("not completed");
    }

    public static void loadAllUnits() {
        LandUnit.load();
        Building.load();
        HoverUnit.load();
        WaterUnit.load();
        AirUnit.load();
        Iterator it = EnumSet.allOf(UnitType.class).iterator();
        while (it.hasNext()) {
            ((UnitType) it.next()).load();
        }
    }

    public static void loadSharedUnitTypeList() {
        sharedUnitTypeList.clear();
        Iterator it = EnumSet.allOf(UnitType.class).iterator();
        while (it.hasNext()) {
            UnitType unitType = (UnitType) it.next();
            Unit unit = unitType.createInstance();
            if (unit != null) {
                unit.remove();
            }
            sharedUnitTypeList.put(unitType, unit);
        }
    }

    public static Unit getSharedObjectFromUnitType(UnitType type) {
        return sharedUnitTypeList.get(type);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Unit() {
        fastUnitList.add(this);
        fastLiveUnitList.add(this);
    }

    @Override // com.corrodinggames.rts.gameFramework.PhysicalObject, com.corrodinggames.rts.gameFramework.GameObject
    public void remove() {
        fastUnitList.remove(this);
        fastLiveUnitList.remove(this);
        super.remove();
    }

    public float getMass() {
        return 3000.0f;
    }

    public boolean isActive() {
        return this.transportedBy == null && this.built >= 1.0f;
    }

    public float getHpBar() {
        if (this.hp < this.maxHp) {
            return this.hp / this.maxHp;
        }
        return -1.0f;
    }

    public float getSecBar() {
        if (this.built < 1.0f) {
            return this.built;
        }
        return -1.0f;
    }

    public int getBlockBar() {
        return -1;
    }

    public int getBlockBarMax() {
        return -1;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void drawOver(float deltaSpeed) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void drawUnder(float deltaSpeed) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void drawInterface(float deltaSpeed) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean drawIcon(float deltaSpeed) {
        return true;
    }


    public boolean notInFogOfWarForPlayer() {
        return true;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        if (!this.dead && this.hp < 0.0f) {
            destroy();
        }
    }

    public void fromLoadedMap() {
    }

    public boolean destroyEffectAndWreak() {
        return false;
    }

    public void destroy() {
        this.dead = true;
        this.dead_time = Data.game.getTickGame().get();
        fastLiveUnitList.remove(this);
        if (!destroyEffectAndWreak()) {
            remove();
        }
    }

    public boolean overlapping(Point point) {
        float disSq = CommonUtils.distanceSq(this.x, this.y, point.x, point.y);
        float minDis = this.radius;
        return disSq < minDis * minDis;
    }

    public boolean overlapping(Unit otherUnit) {
        float disSq = CommonUtils.distanceSq(this.x, this.y, otherUnit.x, otherUnit.y);
        float minDis = this.radius + otherUnit.radius;
        return disSq < minDis * minDis;
    }

    public void setTeam(Player team) {
        setTeam(team.getSite());
    }

    public void setTeam(int id) {
        this.team = Data.game.getPlayerManage().getPlayerArray(id);
    }

    /* loaded from: classes.dex */
    public static abstract class SpecialAction {
        public abstract String getDescription();

        public abstract int getIndex();

        public abstract int getPrice();

        public abstract String getText();

        public abstract int queueCount(Unit unit);

        public boolean showPopupWindow() {
            return false;
        }

        public boolean queueAble() {
            return false;
        }

        public boolean isActive(Unit unit) {
            return unit.team.canBuy(getPrice());
        }

        public UnitType getCreatedUnitType() {
            return null;
        }

        public String getButtonText(Unit unit) {
            String buttonText = getText();
            int queueCount = queueCount(unit);
            if (queueCount == -1 || queueCount == 0) {
                return buttonText;
            }
            return String.valueOf(buttonText) + " (" + queueCount + ")";
        }

        public String getInfoBoxText() {
            return String.valueOf(getText()) + " ($" + getPrice() + ")\n" + getDescription();
        }
    }

    public void giveSpecialActionOrder(SpecialAction action, boolean stopOrUndo) {
    }

    public SpecialAction getSpecialAction(int index) {
        return null;
    }

    public String getSpecialActionText(int index) {
        return getSpecialAction(index).getText();
    }

    public boolean getSpecialActionActive(int index) {
        return getSpecialAction(index).isActive(this);
    }

    public int numSpecialActions() {
        return 0;
    }

    public boolean canBeTransported(Unit unit) {
        return false;
    }

    public boolean transportUnit(Unit unit) {
        return false;
    }

    public int getBuildRange() {
        return 70;
    }
}
