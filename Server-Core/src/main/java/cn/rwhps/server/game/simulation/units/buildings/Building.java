package cn.rwhps.server.game.simulation.units.buildings;

import cn.rwhps.server.game.simulation.units.OrderableUnit;
import cn.rwhps.server.game.simulation.units.UnitType;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.map.MapTile;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.util.Iterator;

/* loaded from: classes.dex */
public abstract class Building extends OrderableUnit {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$corrodinggames$rts$game$units$UnitType;
    static Point _point = new Point();
    static Paint buildingPaint = new Paint();
    public Rect footprint = new Rect();
    public Rect softFootprint = new Rect();
    int drawFrame = 0;

    static synchronized int[] $SWITCH_TABLE$com$corrodinggames$rts$game$units$UnitType() {
        int[] iArr = $SWITCH_TABLE$com$corrodinggames$rts$game$units$UnitType;
        if (iArr == null) {
            iArr = new int[UnitType.values().length];
            try {
                iArr[UnitType.airFactory.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[UnitType.airShip.ordinal()] = 13;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[UnitType.antiAirTurret.ordinal()] = 7;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[UnitType.artillery.ordinal()] = 11;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[UnitType.builder.ordinal()] = 8;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[UnitType.commandCenter.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[UnitType.extractor.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[UnitType.gunBoat.ordinal()] = 16;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[UnitType.gunShip.ordinal()] = 14;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[UnitType.helicopter.ordinal()] = 12;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[UnitType.hoverTank.ordinal()] = 10;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[UnitType.hovercraft.ordinal()] = 19;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[UnitType.ladybug.ordinal()] = 20;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[UnitType.landFactory.ordinal()] = 2;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[UnitType.laserTank.ordinal()] = 18;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[UnitType.megaTank.ordinal()] = 17;
            } catch (NoSuchFieldError e16) {
            }
            try {
                iArr[UnitType.missileShip.ordinal()] = 15;
            } catch (NoSuchFieldError e17) {
            }
            try {
                iArr[UnitType.seaFactory.ordinal()] = 4;
            } catch (NoSuchFieldError e18) {
            }
            try {
                iArr[UnitType.tank.ordinal()] = 9;
            } catch (NoSuchFieldError e19) {
            }
            try {
                iArr[UnitType.turret.ordinal()] = 6;
            } catch (NoSuchFieldError e20) {
            }
            $SWITCH_TABLE$com$corrodinggames$rts$game$units$UnitType = iArr;
        }
        return iArr;
    }

    public static void load() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Building() {
        this.dir = -90.0f;
        this.collidable = false;
    }

    public boolean destroyEffectAndWreakForBuilding() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public boolean destroyEffectAndWreak() {
        GameEngine.getInstance().path.updateAllBuildingCosts();
        if (this.built < 1.0f) {
            return false;
        }
        this.drawFrame = 0;
        return destroyEffectAndWreakForBuilding();
    }

    public boolean canBeBuiltHere() {
        if (overOtherBuilding()) {
            return false;
        }
        if (getUnitType() != UnitType.extractor) {
            Rect testFootprint = this.softFootprint;
            GameEngine game = GameEngine.getInstance();
            game.map.toGrid(this.x, this.y);
            int buildingX = game.map.returnX;
            int buildingY = game.map.returnY;
            for (int loopX = buildingX + testFootprint.left; loopX <= testFootprint.right + buildingX; loopX++) {
                for (int loopY = buildingY + testFootprint.top; loopY <= testFootprint.bottom + buildingY; loopY++) {
                    if (!isTileValid(getUnitType(), loopX, loopY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isTileValidForInterface(UnitType type, int tileX, int tileY) {
        GameEngine game = GameEngine.getInstance();
        if (!game.map.isInMap(tileX, tileY)) {
            return false;
        }
        if ((!game.map.fogOfWar_active || game.map.fogOfWar_map[tileX][tileY] != 10) && isTileValid(type, tileX, tileY)) {
            if (type != UnitType.extractor) {
                return true;
            }
            MapTile tile = game.map.getTileFromObjectLayer(tileX, tileY);
            return tile != null && tile.resPool;
        }
        return false;
    }

    public static boolean isTileValid(UnitType type, int tileX, int tileY) {
        MapTile tile;
        GameEngine game = GameEngine.getInstance();
        if (!game.map.isInMap(tileX, tileY)) {
            return false;
        }
        if (type == UnitType.commandCenter || type == UnitType.extractor || type == UnitType.landFactory || type == UnitType.airFactory || type == UnitType.turret || type == UnitType.antiAirTurret) {
            if ((type != UnitType.extractor || (tile = game.map.getTileFromObjectLayer(tileX, tileY)) == null || !tile.resPool) && game.path.isTileImpassable(game.path.MovementType_LAND, tileX, tileY)) {
                return false;
            }
            return true;
        }
        if (type == UnitType.seaFactory && !game.path.isTileImpassable(game.path.MovementType_WATER, tileX, tileY)) {
            return true;
        }
        return false;
    }

    public static Building getBuilding(int tileX, int tileY) {
        GameEngine game = GameEngine.getInstance();
        game.map.fromGrid(tileX, tileY);
        _point.set(game.map.returnX, game.map.returnY);
        Iterator<Unit> it = Unit.fastUnitList.iterator();
        while (it.hasNext()) {
            Unit unit = it.next();
            if ((unit instanceof Building) && !unit.dead && unit.overlapping(_point)) {
                return (Building) unit;
            }
        }
        return null;
    }

    public boolean overOtherBuilding() {
        Iterator<Unit> it = Unit.fastUnitList.iterator();
        while (it.hasNext()) {
            Unit unit = it.next();
            if (unit != this && (unit instanceof Building) && !unit.dead && overlapping(unit)) {
                return true;
            }
        }
        return false;
    }

    public Building getOtherBuildingOverOfSameType() {
        Iterator<Unit> it = Unit.fastUnitList.iterator();
        while (it.hasNext()) {
            Unit unit = it.next();
            if (unit != this && (unit instanceof Building)) {
                Building building = (Building) unit;
                if (!building.dead && building.team == this.team && building.getUnitType() == getUnitType() && overlapping(building)) {
                    return building;
                }
            }
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public int getBuildRange() {
        return 70;
    }

    public static int getPrice(UnitType type) {
        if (type == null) {
            throw new RuntimeException("type is null");
        }
        switch ($SWITCH_TABLE$com$corrodinggames$rts$game$units$UnitType()[type.ordinal()]) {
            case 1:
                return 800;
            case 2:
                return 700;
            case R.styleable.com_admob_android_ads_AdView_keywords /* 3 */:
                return 1000;
            case 4:
                return 1000;
            case 5:
                return 6000;
            case 6:
                return 500;
            case 7:
                return 600;
            default:
                throw new RuntimeException("type: " + type + " not handled");
        }
    }

    public static Building createBuilding(UnitType type) {
        if (type != null) {
            return (Building) type.createInstance();
        }
        throw new RuntimeException("type is null");
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canMove() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.MovementType getMovementType() {
        return Unit.MovementType.NONE;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public boolean isFlying() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMoveSpeed() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurnSpeed() {
        return 0.0f;
    }

    public Paint getBuildingPaint() {
        GameEngine game = GameEngine.getInstance();
        buildingPaint.reset();
        if (this.built < 1.0f) {
            buildingPaint.setARGB((int) (20.0f + (this.built * 220.0f)), NetworkEngine.PACKET_SEND_CHAT_TO_SERVER, 255, NetworkEngine.PACKET_SEND_CHAT_TO_SERVER);
            buildingPaint.setColorFilter(buildingColorFilter);
        }
        if (this.preview) {
            buildingPaint.setARGB(200, 20, 255, 20);
            if (this.previewValidSpot) {
                buildingPaint.setColorFilter(previewColorFilterValid);
            } else {
                buildingPaint.setColorFilter(previewColorFilterNotValid);
            }
        }
        if (game.settings.renderAntiAlias) {
            buildingPaint.setAntiAlias(true);
            buildingPaint.setFilterBitmap(true);
            buildingPaint.setDither(true);
        }
        return buildingPaint;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        GameEngine game = GameEngine.getInstance();
        if (shouldDrawCheck()) {
            int offsetX = this.drawFrame * this.objectWidth;
            float drawX = (this.x - (this.objectWidth / 2)) - GameEngine.getInstance().viewpointX_rounded;
            float drawY = (this.y - (this.objectHeight / 2)) - GameEngine.getInstance().viewpointY_rounded;
            this._dst.set(drawX, drawY, this.objectWidth + drawX, this.objectHeight + drawY);
            if (RectF.intersects(game.viewpointRectF, this._dst)) {
                this._src.set(offsetX, 0, this.objectWidth + offsetX, this.objectHeight + 0);
                game.graphics.drawImage(this.image, this._src, this._dst, getBuildingPaint());
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void drawUnder(float deltaSpeed) {
        if (this.image_back != null && shouldDrawCheck()) {
            GameEngine game = GameEngine.getInstance();
            this._dst.set(this.x - (this.objectWidth / 2), this.y - (this.objectHeight / 2), this.x + (this.objectWidth / 2), this.y + (this.objectHeight / 2));
            this._dst.offset(-GameEngine.getInstance().viewpointX_rounded, -GameEngine.getInstance().viewpointY_rounded);
            if (RectF.intersects(game.viewpointRectF, this._dst)) {
                this._src.set(0, 0, this.objectWidth + 0, this.objectHeight + 0);
                game.graphics.drawImage(this.image_back, this._src, this._dst, getBuildingPaint());
            }
        }
    }
}
