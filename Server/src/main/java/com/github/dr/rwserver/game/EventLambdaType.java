package com.github.dr.rwserver.game;

/**
 * @author Dr
 */
public class EventLambdaType {
    public static class PlayerUnit {
        public final GameUnitType.GameActions gameActions;
        public final String unitName;
        public boolean result = true;

        public PlayerUnit(GameUnitType.GameActions gameActions,String unitName) {
            this.gameActions = gameActions;
            this.unitName = unitName;
        }
    }
}
