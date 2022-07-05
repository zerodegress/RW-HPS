package cn.rwhps.server.game.simulation.units.buildings;

import android.graphics.Color;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.AudioEngine;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public class AntiAirTurret extends Turret {
    static BitmapOrTexture IMAGE_TURRET = null;
    static BitmapOrTexture IMAGE_ICON = null;
    static BitmapOrTexture[] IMAGE_ICON_TEAMS = new BitmapOrTexture[8];

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.buildings.Building, com.corrodinggames.rts.game.units.Unit
    public BitmapOrTexture getIcon() {
        return IMAGE_ICON_TEAMS[this.team.id];
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE_TURRET = game.graphics.loadImage(R.drawable.anti_air_top);
        IMAGE_ICON = game.graphics.loadImage(R.drawable.unit_icon_building_air_turrent);
        for (int n = 0; n < IMAGE_ICON_TEAMS.length; n++) {
            IMAGE_ICON_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE_ICON.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 270.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 80.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.buildings.Factory, com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        Projectile p = Projectile.createProjectile(this, this.x, this.y);
        p.color = Color.argb(255, 230, 230, 50);
        p.directDamage = 60.0f;
        p.target = target;
        p.lifeTimer = 190.0f;
        p.speed = 4.0f;
        p.ballistic = true;
        p.ballistic_delaymove_height = 0.0f;
        p.ballistic_height = 0.0f;
        p.trailEffect = true;
        p.largeHitEffect = true;
        GameEngine.getInstance().audio.playSound(AudioEngine.missile_fire, 0.3f, this.x, this.y);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.Unit
    public UnitType getUnitType() {
        return UnitType.antiAirTurret;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret
    void drawTurret() {
        GameEngine game = GameEngine.getInstance();
        this._srcRect.set(0, 0, IMAGE_TURRET.getWidth(), IMAGE_TURRET.getHeight());
        game.graphics.drawImageCentered(IMAGE_TURRET, this._srcRect, this.x - GameEngine.getInstance().viewpointX_rounded, this.y - GameEngine.getInstance().viewpointY_rounded, this.turretDir, getBuildingPaint());
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttackUnit(Unit unit) {
        return unit.isFlying();
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.buildings.Factory
    public void completeQueueItem(Factory.BuildQueueItem item) {
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.Unit
    public int numSpecialActions() {
        return 0;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Turret, com.corrodinggames.rts.game.units.Unit
    public Unit.SpecialAction getSpecialAction(int index) {
        return null;
    }
}
