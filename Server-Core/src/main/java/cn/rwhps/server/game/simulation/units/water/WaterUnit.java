package cn.rwhps.server.game.simulation.units.water;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Team;
import com.corrodinggames.rts.game.units.MovableUnit;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.gameFramework.BitmapOrTexture;
import com.corrodinggames.rts.gameFramework.EffectEngine;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* loaded from: classes.dex */
public abstract class WaterUnit extends MovableUnit {
    public static BitmapOrTexture IMAGE_ICON = null;
    public static BitmapOrTexture[] IMAGE_ICON_TEAMS = new BitmapOrTexture[8];
    float fallSpeed;
    boolean hitGround = false;
    float splashEffect;

    @Override // com.corrodinggames.rts.game.units.Unit
    public BitmapOrTexture getIcon() {
        return IMAGE_ICON_TEAMS[this.team.id];
    }

    public static void load() {
        GameEngine game = GameEngine.getInstance();
        IMAGE_ICON = game.graphics.loadImage(R.drawable.unit_icon_water);
        for (int n = 0; n < IMAGE_ICON_TEAMS.length; n++) {
            IMAGE_ICON_TEAMS[n] = game.graphics.loadImage(Team.createBitmapForTeam(IMAGE_ICON.bitmap, n));
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public Unit.MovementType getMovementType() {
        return Unit.MovementType.WATER;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        EffectEngine.EffectObject effect;
        super.update(deltaSpeed);
        if (this.dead) {
            if (this.height > -10.0f) {
                this.fallSpeed += 0.002f * deltaSpeed;
                this.height -= this.fallSpeed * deltaSpeed;
                return;
            }
            this.height = -10.0f;
            if (!this.hitGround) {
                this.hitGround = true;
            }
        } else if (isActive() && !this.dead) {
            if (this.speed > 0.0f) {
                this.splashEffect += deltaSpeed;
            }
            if (this.splashEffect > 10.0f) {
                this.splashEffect = 0.0f;
                if (isOnScreen() && (effect = GameEngine.getInstance().effects.emitEffect((float) (this.x + (Math.cos(Math.toRadians(this.dir)) * 4.0d)), (float) (this.y + (Math.sin(Math.toRadians(this.dir)) * 4.0d)), 0.0f, EffectEngine.EffectType.custom, false)) != null) {
                    effect.stripIndex = 0;
                    effect.frameIndex = 13;
                    effect.drawLayer = 1;
                    effect.fadeOut = true;
                    effect.startingAlpha = 0.8f;
                    effect.startTimer = 80.0f;
                    effect.timer = 80.0f;
                    effect.xSpeed = (float) ((-Math.cos(Math.toRadians(this.dir))) * 0.10000000149011612d);
                    effect.ySpeed = (float) ((-Math.sin(Math.toRadians(this.dir))) * 0.10000000149011612d);
                }
            }
        }
    }
}
