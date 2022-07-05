package cn.rwhps.server.game.simulation.units.land;

import cn.rwhps.server.game.simulation.units.MovableUnit;

/* loaded from: classes.dex */
public abstract class LandUnit extends MovableUnit {
    public static void load() {
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public MovementType getMovementType() {
        return MovementType.LAND;
    }
}
