package cn.rwhps.server.game.simulation.units.air;

import cn.rwhps.server.game.simulation.units.MovableUnit;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public abstract class AirUnit extends MovableUnit {
    float fallSpeed;
    boolean hitGround = false;

    @Override // com.corrodinggames.rts.game.units.Unit
    public BitmapOrTexture getIcon() {
        return IMAGE_ICON_TEAMS[this.team.id];
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE_ICON = game.graphics.loadImage(R.drawable.unit_icon_air);
        for (int n = 0; n < IMAGE_ICON_TEAMS.length; n++) {
            IMAGE_ICON_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE_ICON.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.MovementType getMovementType() {
        return Unit.MovementType.AIR;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        GameEngine game = GameEngine.getInstance();
        if (!this.dead) {
            return;
        }
        if (this.height > 0.0f) {
            this.fallSpeed += 0.08f * deltaSpeed;
            this.height -= this.fallSpeed * deltaSpeed;
        } else if (!this.hitGround) {
            this.height = 0.0f;
            this.fallSpeed = 0.0f;
            this.hitGround = true;
            game.effects.emitSmallExplosion(this.x, this.y, this.height);
            leaveScorchMark();
        } else if (isOverWater() && this.height > -10.0f) {
            this.fallSpeed += 0.002f * deltaSpeed;
            this.height -= this.fallSpeed * deltaSpeed;
        }
    }
}
