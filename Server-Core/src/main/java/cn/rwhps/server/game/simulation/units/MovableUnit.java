package cn.rwhps.server.game.simulation.units;

/**
 * 移动中的单位
 */
public abstract class MovableUnit extends OrderableUnit {
    // com.corrodinggames.rts.game.units.OrderableUnit
    @Override
    public boolean canMove() {
        return true;
    }

    // com.corrodinggames.rts.game.units.Unit
    @Override
    public boolean isFlying() {
        return false;
    }
}
