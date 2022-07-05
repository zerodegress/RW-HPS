package cn.rwhps.server.game.simulation.units;

import cn.rwhps.server.game.simulation.units.buildings.*;
import cn.rwhps.server.game.simulation.units.land.*;
import com.corrodinggames.rts.game.units.air.AirShip;
import com.corrodinggames.rts.game.units.air.GunShip;
import com.corrodinggames.rts.game.units.air.Helicopter;
import com.corrodinggames.rts.game.units.alien.LadyBug;
import com.corrodinggames.rts.game.units.water.GunBoat;
import com.corrodinggames.rts.game.units.water.MissileShip;

/* loaded from: classes.dex */
public enum UnitType {
    extractor {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new Extractor();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            Extractor.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Extractor";
        }
    },
    landFactory {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new LandFactory();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            LandFactory.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Land Factory";
        }
    },
    airFactory {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new AirFactory();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            AirFactory.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Air Factory";
        }
    },
    seaFactory {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new SeaFactory();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            SeaFactory.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Sea Factory";
        }
    },
    commandCenter {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new CommandCenter();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            CommandCenter.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Command";
        }
    },
    turret {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new Turret();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            Turret.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Turret";
        }
    },
    antiAirTurret {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new AntiAirTurret();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            AntiAirTurret.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Anti-air Turret";
        }
    },
    builder {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new Builder();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            Builder.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Builder";
        }
    },
    tank {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new Tank();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            Tank.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Tank";
        }
    },
    hoverTank {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new HoverTank();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            HoverTank.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Hover Tank";
        }
    },
    artillery {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new ArtilleryTank();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            ArtilleryTank.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Artillery";
        }
    },
    helicopter {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new Helicopter();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            Helicopter.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Helicopter";
        }
    },
    airShip {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new AirShip();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            AirShip.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Interceptor";
        }
    },
    gunShip {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new GunShip();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            GunShip.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Gun Ship";
        }
    },
    missileShip {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new MissileShip();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            MissileShip.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Missile Ship";
        }
    },
    gunBoat {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new GunBoat();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            GunBoat.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Gun Boat";
        }
    },
    megaTank {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new MegaTank();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            MegaTank.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Mega Tank";
        }
    },
    laserTank {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new LaserTank();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            LaserTank.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Laser Tank";
        }
    },
    hovercraft {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new Hovercraft();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            Hovercraft.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Hovercraft";
        }
    },
    ladybug {
        @Override // com.corrodinggames.rts.game.units.UnitType
        public Unit createInstance() {
            return new LadyBug();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public void load() {
            LadyBug.load();
        }

        @Override // com.corrodinggames.rts.game.units.UnitType
        public String getText() {
            return "Ladybug";
        }
    };

    public abstract Unit createInstance();

    public abstract String getText();

    public abstract void load();

    /* synthetic */ UnitType(UnitType unitType) {
        this();
    }

    public static UnitType getFromString(String typeString) {
        UnitType[] values = values();
        for (UnitType unitType : values) {
            if (unitType.name().equalsIgnoreCase(typeString)) {
                return unitType;
            }
        }
        return null;
    }
}
