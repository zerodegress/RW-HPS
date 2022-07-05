package cn.rwhps.server.game.simulation.units;

import cn.rwhps.server.game.replay.block.PointF;
import cn.rwhps.server.game.simulation.GameObject;
import cn.rwhps.server.game.simulation.units.buildings.Building;
import cn.rwhps.server.util.CommonUtils;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

/* loaded from: classes.dex */
public abstract class OrderableUnit extends ActiveUnit {
    static final int maxPathNodes = 60;
    static final int maxWaypoints = 30;
    float collidingTimer;
    protected float currentShootDelay;
    public float currentWaypointTime;
    public boolean isLeader;
    public OrderableUnit leader;
    private float passiveAttackCheckDelay;
    private Unit passivelyAttacking;
    float pathToLeaderTimer;
    boolean pathing_active;
    float pathing_delay;
    float pathing_pointX;
    float pathing_pointY;
    private float repairEffectDelay;
    public boolean wasLeader;
    static PointF lowerLeft = new PointF();
    static PointF upperRight = new PointF();
    static PointF upperLeft = new PointF();
    static PointF lowerRight = new PointF();
    protected static PointF _pointF = new PointF();
    Paint _shadowPaint = new Paint();
    private int waypointsCount = 0;
    private Waypoint[] waypoints = new Waypoint[30];
    public AttackMode attackMode = AttackMode.onlyInRange;
    Rect r = new Rect();
    PointF v1 = new PointF();
    PointF v2 = new PointF();
    PointF _return = new PointF();
    public boolean formationHasOffset = false;
    public float formationXOffset = 0.0f;
    public float formationYOffset = 0.0f;
    public float formationDir = 0.0f;
    public int formationStartTime = 0;
    private PathNode[] activePath = new PathNode[maxPathNodes];
    private int activePathCount = 0;
    private int activePathTotalCount = 0;
    RectF _tempF = new RectF();
    Rect _temp = new Rect();

    /* loaded from: classes.dex */
    public strict enum AttackMode {
        outOfRange,
        onlyInRange,
        returnFire,
        holdFire
    }

    public abstract boolean canAttack();

    public abstract boolean canMove();

    public abstract void fireProjectile(Unit unit);

    public abstract float getMaxAttackRange();

    public abstract float getMoveSpeed();

    public abstract float getShootDelay();

    public abstract float getTurnSpeed();

    public abstract float getTurrentTurnSpeed();

    public OrderableUnit() {
        for (int n = 0; n < 30; n++) {
            this.waypoints[n] = new Waypoint();
        }
        for (int n2 = 0; n2 < maxPathNodes; n2++) {
            this.activePath[n2] = new PathNode();
        }
    }

    public PointF collideWithTile(float oldX, float oldY, float newX, float newY, int mapX, int mapY) {
        GameEngine game = GameEngine.getInstance();
        this.r.set(mapX, mapY, mapX + 1, mapY + 1);
        this.v1.set(oldX, oldY);
        this.v2.set(newX, newY);
        this._return.set(this.v2);
        int dir = -1;
        lowerLeft.set(this.r.left, this.r.bottom);
        upperRight.set(this.r.right, this.r.top);
        upperLeft.set(this.r.left, this.r.top);
        lowerRight.set(this.r.right, this.r.bottom);
        if (this.v1.y < this.v2.y) {
            if (!game.path.isTileImpassable(getMovementType(), mapX, mapY - 1) && CommonUtils.lineIntersectLine(this.v1, this.v2, upperLeft, upperRight)) {
                dir = 3;
            }
        } else if (!game.path.isTileImpassable(getMovementType(), mapX, mapY + 1) && CommonUtils.lineIntersectLine(this.v1, this.v2, lowerLeft, lowerRight)) {
            dir = 1;
        }
        if (this.v1.x < this.v2.x) {
            if (!game.path.isTileImpassable(getMovementType(), mapX - 1, mapY) && CommonUtils.lineIntersectLine(this.v1, this.v2, upperLeft, lowerLeft)) {
                dir = 2;
            }
        } else if (!game.path.isTileImpassable(getMovementType(), mapX + 1, mapY) && CommonUtils.lineIntersectLine(this.v1, this.v2, upperRight, lowerRight)) {
            dir = 0;
        }
        if (dir == -1) {
            return null;
        }
        if (dir == 0) {
            this._return.x = mapX + 1 + 0.01f;
        }
        if (dir == 2) {
            this._return.x = mapX - 0.01f;
        }
        if (dir == 1) {
            this._return.y = mapY + 1 + 0.01f;
        }
        if (dir == 3) {
            this._return.y = mapY - 0.01f;
        }
        return this._return;
    }

    @Override // com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        GameEngine game = GameEngine.getInstance();
        this.factoryExitDelay = CommonUtils.toZero(this.factoryExitDelay, deltaSpeed);
        float newX = this.x;
        float newY = this.y;
        if (isActive() && !this.dead) {
            this.turretLockDelay = CommonUtils.toZero(this.turretLockDelay, deltaSpeed);
            if (resetTurret() && this.turretLockDelay == 0.0f) {
                float diff = CommonUtils.getRotationDir(this.turretDir, this.dir, getTurrentTurnSpeed() * deltaSpeed);
                if (diff == 0.0f) {
                    this.turretLockDelay = 10.0f;
                }
                this.turretDir += diff;
            }
            doWaypoints(deltaSpeed);
            boolean changingPosition = false;
            if (this.speed != 0.0f && canMove()) {
                newX += CommonUtils.cos(this.dir) * getMoveSpeed() * this.speed * deltaSpeed;
                newY += CommonUtils.sin(this.dir) * getMoveSpeed() * this.speed * deltaSpeed;
                changingPosition = true;
            }
            if (!(this.xPush == 0.0f && this.yPush == 0.0f)) {
                newX += this.xPush;
                newY += this.yPush;
                this.xPush = 0.0f;
                this.yPush = 0.0f;
                changingPosition = true;
            }
            if (canMove() && changingPosition) {
                Iterator<Unit> it = Unit.fastUnitList.iterator();
                while (it.hasNext()) {
                    Unit unit = it.next();
                    if (unit != this && unit.collidable && isFlying() == unit.isFlying() && unit.transportedBy == null && this.dontCollideWith != unit && unit.dontCollideWith != this) {
                        float disSq = CommonUtils.distanceSq(this.x + this.xPush, this.y + this.yPush, unit.x + unit.xPush, unit.y + unit.yPush);
                        float minDis = this.radius + unit.radius;
                        if (disSq < minDis * minDis) {
                            float targetDir = CommonUtils.getDirection(this.x + this.xPush, this.y + this.yPush, unit.x + unit.xPush, unit.y + unit.yPush);
                            float push = (minDis - ((float) Math.sqrt(disSq))) + 0.01f;
                            float massRatio = 0.0f;
                            if (unit instanceof MovableUnit) {
                                massRatio = getMass() / (getMass() + unit.getMass());
                            }
                            float massRatioInv = 1.0f - massRatio;
                            if (unit instanceof MovableUnit) {
                                unit.xPush += CommonUtils.cos(targetDir) * push * massRatio;
                                unit.yPush += CommonUtils.sin(targetDir) * push * massRatio;
                            }
                            this.xPush -= (CommonUtils.cos(targetDir) * push) * massRatioInv;
                            this.yPush -= (CommonUtils.sin(targetDir) * push) * massRatioInv;
                        }
                    }
                }
            }
            float f = this.x;
            game.map.getClass();
            float oldMapX = 0.05f * f;
            float f2 = this.y;
            game.map.getClass();
            float oldMapY = 0.05f * f2;
            game.map.getClass();
            float newMapX = newX * 0.05f;
            game.map.getClass();
            float newMapY = newY * 0.05f;
            PointF collidePoint = null;
            int oldMapXRounded = CommonUtils.roundDown(oldMapX);
            int oldMapYRounded = CommonUtils.roundDown(oldMapY);
            int newMapXRounded = CommonUtils.roundDown(newMapX);
            int newMapYRounded = CommonUtils.roundDown(newMapY);
            if (!(oldMapXRounded == newMapXRounded && oldMapYRounded == newMapYRounded)) {
                seeThoughFogOfWar();
                if (this.factoryExitDelay == 0.0f && !isFlying() && game.path.isTileImpassable(getMovementType(), newMapXRounded, newMapYRounded)) {
                    if (!(oldMapXRounded == newMapXRounded || oldMapYRounded == newMapYRounded)) {
                        if (game.path.isTileImpassable(getMovementType(), oldMapXRounded, newMapYRounded) && game.path.isTileImpassable(getMovementType(), newMapXRounded, oldMapYRounded)) {
                            this._return.set(oldMapX, oldMapY);
                            collidePoint = this._return;
                        }
                        if (collidePoint == null && game.path.isTileImpassable(getMovementType(), oldMapXRounded, newMapYRounded)) {
                            collidePoint = collideWithTile(oldMapX, oldMapY, newMapX, newMapY, oldMapXRounded, newMapYRounded);
                        }
                        if (collidePoint == null && game.path.isTileImpassable(getMovementType(), newMapXRounded, oldMapYRounded)) {
                            collidePoint = collideWithTile(oldMapX, oldMapY, newMapX, newMapY, newMapXRounded, oldMapYRounded);
                        }
                    }
                    if (collidePoint == null) {
                        collidePoint = collideWithTile(oldMapX, oldMapY, newMapX, newMapY, newMapXRounded, newMapYRounded);
                    }
                }
            }
            if (collidePoint != null) {
                float f3 = collidePoint.x;
                game.map.getClass();
                newX = f3 * 20.0f;
                float f4 = collidePoint.y;
                game.map.getClass();
                newY = f4 * 20.0f;
                this.collidingTimer += deltaSpeed;
            } else {
                this.collidingTimer = 0.0f;
            }
            this.x = newX;
            this.y = newY;
        }
    }

    public void rotateBody(float diff) {
        this.dir += diff;
        this.turretDir += diff;
    }

    public void doWaypoints(float deltaSpeed) {
        PathNode futureNode;
        GameEngine game = GameEngine.getInstance();
        this.dontCollideWith = null;
        if (this.dontCollideExit != null) {
            this.dontCollideExitDelay = CommonUtils.toZero(this.dontCollideExitDelay, deltaSpeed);
            this.dontCollideWith = this.dontCollideExit;
            if (this.dontCollideExitDelay == 0.0f) {
                this.dontCollideExit = null;
            }
        }
        this.pathing_delay = CommonUtils.toZero(this.pathing_delay, deltaSpeed);
        if (this.speed != 0.0f) {
            this.pathToLeaderTimer = CommonUtils.toZero(this.pathToLeaderTimer, deltaSpeed);
        }
        Waypoint active = getActiveWaypoint();
        float targetSpeed = 0.0f;
        if (active == null) {
            this.moving = false;
        }
        float pathing_minRange = 10.0f;
        boolean canFollowLeader = true;
        this.pathing_active = false;
        if (active != null) {
            this.moving = true;
            this.currentWaypointTime += deltaSpeed;
            float ux = active.getRealX();
            float uy = active.getRealY();
            float targetDir = CommonUtils.getDirection(this.x, this.y, ux, uy);
            float diffDir = CommonUtils.getRotationDir(this.dir, targetDir, 360.0f);
            float disSq = CommonUtils.distanceSq(this.x, this.y, ux, uy);
            if (active.type == Waypoint.WaypointType.move) {
                this.pathing_active = true;
                this.pathing_pointX = ux;
                this.pathing_pointY = uy;
                if (disSq < 256.0f) {
                    completeActiveWaypoint();
                    active = null;
                }
            } else if (active.type == Waypoint.WaypointType.attack) {
                if (active.getTargetUnit().dead) {
                    completeActiveWaypoint();
                }
                if (this.leader != null) {
                    if (disSq < 48400.0f) {
                        canFollowLeader = false;
                    }
                    if (this.leader.passivelyAttacking == active.targetUnit) {
                        canFollowLeader = false;
                    }
                }
                if (disSq < getMaxAttackRange() * getMaxAttackRange()) {
                    this.passivelyAttacking = active.targetUnit;
                    this.passiveAttackCheckDelay = 10.0f;
                } else {
                    this.pathing_active = true;
                    this.pathing_pointX = ux;
                    this.pathing_pointY = uy;
                    if (this.pathing_delay > 50.0f) {
                        this.pathing_delay = 50.0f;
                    }
                }
            } else if (active.type == Waypoint.WaypointType.build) {
                int minRange = Unit.getSharedObjectFromUnitType(active.build).getBuildRange();
                if (this.leader != null && (this.leader.getActiveWaypoint() == null || this.leader.getActiveWaypoint().type != Waypoint.WaypointType.build)) {
                    canFollowLeader = false;
                }
                if (disSq > minRange * minRange) {
                    this.pathing_active = true;
                    this.pathing_pointX = ux;
                    this.pathing_pointY = uy;
                    pathing_minRange = 30.0f;
                } else if (Math.abs(diffDir) > 30.0f) {
                    rotateBody(CommonUtils.getRotationDir(this.dir, targetDir, getTurnSpeed() * deltaSpeed));
                } else {
                    boolean builtOk = false;
                    boolean builtExisting = false;
                    Building building = Building.createBuilding(active.build);
                    building.built = 0.0f;
                    game.map.snapToGrid(active.x, active.y);
                    int i = game.map.returnX;
                    game.map.getClass();
                    building.x = i + 10;
                    int i2 = game.map.returnY;
                    game.map.getClass();
                    building.y = i2 + 10;
                    building.setTeam(this.team.getSite());
                    if (!building.canBeBuiltHere()) {
                        Building existingOfSameType = building.getOtherBuildingOverOfSameType();
                        if (existingOfSameType != null) {
                            builtExisting = true;
                            active.clear();
                            active.type = Waypoint.WaypointType.repair;
                            active.targetUnit = existingOfSameType;
                            clearPath();
                        }
                    } else if (this.team.tryToBuy(Building.getPrice(active.build))) {
                        builtOk = true;
                    }
                    if (!builtOk) {
                        building.remove();
                        if (!builtExisting) {
                            completeActiveWaypoint();
                        }
                    } else {
                        active.clear();
                        active.type = Waypoint.WaypointType.repair;
                        active.targetUnit = building;
                        game.path.updateAllBuildingCosts();
                        clearPath();
                    }
                }
            } else if (active.type == Waypoint.WaypointType.repair) {
                if (active.targetUnit.dead) {
                    completeActiveWaypoint();
                }
                int minRange2 = active.targetUnit.getBuildRange() + 15;
                if (disSq > minRange2 * minRange2) {
                    this.pathing_active = true;
                    this.pathing_pointX = ux;
                    this.pathing_pointY = uy;
                    pathing_minRange = 30.0f;
                } else if (Math.abs(diffDir) > 30.0f) {
                    rotateBody(CommonUtils.getRotationDir(this.dir, targetDir, getTurnSpeed() * deltaSpeed));
                } else {
                    Unit targetUnit = active.targetUnit;
                    this.repairEffectDelay = CommonUtils.toZero(this.repairEffectDelay, deltaSpeed);
                    if (this.repairEffectDelay == 0.0f) {
                        this.repairEffectDelay = 5.0f;
                        EffectEngine.EffectObject effect = game.effects.emitEffect(this.x, this.y, this.height, EffectEngine.EffectType.custom, false);
                        if (effect != null) {
                            float effectDir = CommonUtils.getDirection(this.x, this.y, (float) (targetUnit.x + (-8.0d) + (Math.random() * 16.0d)), (float) (targetUnit.y + (-8.0d) + (Math.random() * 16.0d)));
                            effect.xSpeed = CommonUtils.cos(effectDir) * CommonUtils.rnd(5.0f, 6.0f);
                            effect.ySpeed = CommonUtils.sin(effectDir) * CommonUtils.rnd(5.0f, 6.0f);
                            effect.frameIndex = 6;
                            effect.timer = 20.0f;
                            effect.startTimer = effect.timer;
                            effect.fadeOut = true;
                            effect.startingAlpha = 1.0f;
                        }
                    }
                    if (targetUnit.built < 1.0f) {
                        targetUnit.built = (float) (targetUnit.built + (0.001d * deltaSpeed));
                    } else {
                        targetUnit.built = 1.0f;
                        targetUnit.hp = (float) (targetUnit.hp + (0.5d * deltaSpeed));
                        if (targetUnit.hp > targetUnit.maxHp) {
                            targetUnit.hp = targetUnit.maxHp;
                            completeActiveWaypoint();
                        }
                    }
                }
            } else if (active.type == Waypoint.WaypointType.loadInto) {
                if (active.targetUnit.dead) {
                    completeActiveWaypoint();
                }
                if (!active.targetUnit.canBeTransported(this)) {
                    completeActiveWaypoint();
                }
                this.dontCollideWith = active.targetUnit;
                if (disSq > 441.0f) {
                    this.pathing_active = true;
                    this.pathing_pointX = ux;
                    this.pathing_pointY = uy;
                    pathing_minRange = 8.0f;
                    if (disSq < 48400.0f) {
                        canFollowLeader = false;
                    }
                } else {
                    active.targetUnit.transportUnit(this);
                    completeActiveWaypoint();
                }
            }
        }
        if (canAttack()) {
            boolean notInFiringRange = false;
            if (this.passivelyAttacking != null && !isPassiveTarget(this.passivelyAttacking)) {
                this.passivelyAttacking = null;
            }
            if (this.passivelyAttacking != null) {
                notInFiringRange = !isInFiringRange(this.passivelyAttacking);
            }
            this.passiveAttackCheckDelay = CommonUtils.toZero(this.passiveAttackCheckDelay, deltaSpeed);
            if ((this.passivelyAttacking == null || notInFiringRange) && this.passiveAttackCheckDelay == 0.0f) {
                this.passiveAttackCheckDelay = 20.0f;
                float currentDisSq = -1.0f;
                Iterator<Unit> it = Unit.fastLiveUnitList.iterator();
                while (it.hasNext()) {
                    Unit unit = it.next();
                    if (isPassiveTarget(unit)) {
                        float disSq2 = CommonUtils.distanceSq(this.x, this.y, unit.x, unit.y);
                        if (currentDisSq == -1.0f || disSq2 < currentDisSq) {
                            currentDisSq = disSq2;
                            this.passivelyAttacking = unit;
                        }
                    }
                }
            }
            if (this.passivelyAttacking != null) {
                float disSq3 = CommonUtils.distanceSq(this.x, this.y, this.passivelyAttacking.x, this.passivelyAttacking.y);
                if (this.passivelyAttacking != null) {
                    float targetDir2 = CommonUtils.getDirection(this.x, this.y, this.passivelyAttacking.x, this.passivelyAttacking.y);
                    if (disSq3 < getMaxAttackRange() * getMaxAttackRange()) {
                        boolean readyToFire = false;
                        if (!isFixedFiring()) {
                            float diffTurretDir = CommonUtils.getRotationDir(this.turretDir, targetDir2, 360.0f);
                            this.turretLockDelay = 40.0f;
                            this.turretDir += CommonUtils.getRotationDir(this.turretDir, targetDir2, getTurrentTurnSpeed() * deltaSpeed);
                            if (Math.abs(diffTurretDir) < 5.0f) {
                                readyToFire = true;
                            }
                        } else if (!this.pathing_active) {
                            float diffDir2 = CommonUtils.getRotationDir(this.dir, targetDir2, 360.0f);
                            rotateBody(CommonUtils.getRotationDir(this.dir, targetDir2, getTurnSpeed() * deltaSpeed));
                            if (Math.abs(diffDir2) < 5.0f) {
                                readyToFire = true;
                            }
                        }
                        if (readyToFire && this.currentShootDelay == 0.0f) {
                            this.currentShootDelay = getShootDelay();
                            fireProjectile(this.passivelyAttacking);
                        }
                    } else if (!this.pathing_active && this.attackMode.equals(AttackMode.outOfRange)) {
                        this.pathing_active = true;
                        this.pathing_pointX = this.passivelyAttacking.x;
                        this.pathing_pointY = this.passivelyAttacking.y;
                    }
                }
            }
        }
        if (this.leader != null && (this.leader.dead || !this.leader.isActive())) {
            this.leader = null;
        }
        boolean needsToBeSlower = false;
        if (this.pathing_active) {
            PathNode node = getActivePathNode();
            float ux2 = 0.0f;
            float uy2 = 0.0f;
            boolean moving = false;
            if (this.leader != null && canFollowLeader) {
                if (this.collidingTimer > 1.0f) {
                    this.pathToLeaderTimer = 20.0f;
                }
                if (this.pathToLeaderTimer == 0.0f) {
                    clearPath();
                    ux2 = this.leader.x + this.formationXOffset;
                    uy2 = this.leader.y + this.formationYOffset;
                    if (this.leader.activePathTotalCount > 2 && this.leader.activePathCount + 4 >= this.leader.activePathTotalCount && (futureNode = this.leader.getPathNodeByIndex(3)) != null) {
                        needsToBeSlower = true;
                        ux2 = futureNode.x + this.formationXOffset;
                        uy2 = futureNode.y + this.formationYOffset;
                    }
                    if (CommonUtils.distanceSq(this.x, this.y, ux2, uy2) >= 256.0f) {
                        moving = true;
                    } else if (this.leader.getActiveWaypoint() == null) {
                        float diff = CommonUtils.getRotationDir(this.dir, this.formationDir, getTurnSpeed() * deltaSpeed);
                        rotateBody(diff);
                        if (Math.abs(diff) < 3.0f && active != null && active.type == Waypoint.WaypointType.move) {
                            completeActiveWaypoint();
                        }
                    }
                } else if (node != null) {
                    ux2 = node.x;
                    uy2 = node.y;
                    moving = true;
                } else if (this.pathing_delay == 0.0f) {
                    this.pathing_delay = 500.0f;
                    getNewPath(this.leader.x, this.leader.y);
                }
            } else if (this.factoryExitDelay != 0.0f) {
                ux2 = this.pathing_pointX;
                uy2 = this.pathing_pointY;
                moving = true;
            } else if (node != null) {
                ux2 = node.x;
                uy2 = node.y;
                moving = true;
            } else if (this.pathing_delay == 0.0f) {
                this.pathing_delay = 500.0f;
                getNewPath(this.pathing_pointX, this.pathing_pointY);
            }
            if (moving) {
                float targetDir3 = CommonUtils.getDirection(this.x, this.y, ux2, uy2);
                float diffDir3 = CommonUtils.getRotationDir(this.dir, targetDir3, 360.0f);
                float disSq4 = CommonUtils.distanceSq(this.x, this.y, ux2, uy2);
                rotateBody(CommonUtils.getRotationDir(this.dir, targetDir3, getTurnSpeed() * deltaSpeed));
                float moveDirRange = 20.0f;
                if (disSq4 > 400.0f) {
                    moveDirRange = 45.0f;
                }
                if (disSq4 > 3600.0f) {
                    moveDirRange = 93.0f;
                }
                boolean finalNode = this.activePathCount == 1;
                if ((!finalNode || disSq4 >= pathing_minRange * pathing_minRange) && Math.abs(diffDir3) < moveDirRange) {
                    targetSpeed = 1.0f;
                    if (needsToBeSlower) {
                        targetSpeed = 1.0f - 0.0f;
                    } else if (this.leader != null && disSq4 > 400.0f) {
                        targetSpeed = 1.0f + 0.2f;
                    }
                }
                if ((!finalNode && disSq4 < 256.0f) || (finalNode && disSq4 < pathing_minRange * pathing_minRange)) {
                    completeActivePathNode();
                }
            }
        }
        this.currentShootDelay = CommonUtils.toZero(this.currentShootDelay, deltaSpeed);
        if (this.speed < targetSpeed) {
            this.speed = CommonUtils.toValue(this.speed, targetSpeed, getMoveAccelerationSpeed() * deltaSpeed);
        }
        if (this.speed > targetSpeed) {
            this.speed = CommonUtils.toValue(this.speed, targetSpeed, getMoveDecelerationSpeed() * deltaSpeed);
        }
    }

    public boolean isInFiringRange(Unit unit) {
        return CommonUtils.distanceSq(this.x, this.y, unit.x, unit.y) < getMaxAttackRange() * getMaxAttackRange();
    }

    public boolean canAttackUnit(Unit unit) {
        return true;
    }

    public boolean isPassiveTarget(Unit unit) {
        float minTargetDisSq;
        if (!(this.attackMode == AttackMode.holdFire || this.attackMode == AttackMode.returnFire)) {
            if (unit.dead || !this.team.isEnemy(unit.team)) {
                return false;
            }
            if (unit.transportedBy == null && canAttackUnit(unit)) {
                float disSq = CommonUtils.distanceSq(this.x, this.y, unit.x, unit.y);
                if (this.attackMode == AttackMode.onlyInRange || this.attackMode == AttackMode.outOfRange) {
                    if (this.attackMode == AttackMode.onlyInRange) {
                        minTargetDisSq = getMaxAttackRange() * getMaxAttackRange();
                    } else {
                        minTargetDisSq = (getMaxAttackRange() + 200.0f) * (getMaxAttackRange() + 200.0f);
                    }
                    if (disSq < minTargetDisSq) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public Waypoint getNextFreeWaypoint() {
        Waypoint waypoint = this.waypoints[this.waypointsCount];
        waypoint.clear();
        if (this.waypointsCount < 29) {
            this.waypointsCount++;
        }
        return waypoint;
    }

    public Waypoint addBuildWaypoint(float x, float y, UnitType build) {
        Waypoint waypoint = getNextFreeWaypoint();
        waypoint.setBuild(x, y, build);
        return waypoint;
    }

    public Waypoint addMoveWaypoint(float x, float y) {
        Waypoint waypoint = getNextFreeWaypoint();
        waypoint.setMove(x, y);
        return waypoint;
    }

    public Waypoint addAttackWaypoint(Unit unit) {
        Waypoint waypoint = getNextFreeWaypoint();
        waypoint.setAttack(unit);
        return waypoint;
    }

    public Waypoint addWaypointByCopy(Waypoint srcWaypoint) {
        Waypoint waypoint = getNextFreeWaypoint();
        waypoint.set(srcWaypoint);
        return waypoint;
    }

    public boolean isIdle() {
        return getActiveWaypoint() == null;
    }

    public Waypoint getActiveWaypoint() {
        if (this.waypointsCount == 0) {
            return null;
        }
        return this.waypoints[0];
    }

    public void completeActiveWaypoint() {
        this.currentWaypointTime = 0.0f;
        if (this.waypointsCount != 0) {
            if (this.waypointsCount == 1) {
                this.waypointsCount = 0;
                return;
            }
            Waypoint first = this.waypoints[0];
            for (int n = 0; n < 29; n++) {
                this.waypoints[n] = this.waypoints[n + 1];
            }
            this.waypoints[29] = first;
            this.waypointsCount--;
            clearPath();
        }
    }

    public void clearAllWaypoints() {
        this.currentWaypointTime = 0.0f;
        this.waypointsCount = 0;
        clearPath();
        stopFollowersFollowing();
        this.leader = null;
    }

    public void stopFollowersFollowing() {
        Waypoint waypoint;
        GroupController.UnitGroup group;
        OrderableUnit last = null;
        Iterator<GameObject> it = GameObject.fastGameObjectList.iterator();
        while (it.hasNext()) {
            GameObject obj = it.next();
            if (obj instanceof OrderableUnit) {
                OrderableUnit mUnit = (OrderableUnit) obj;
                if (mUnit.leader == this) {
                    mUnit.leader = null;
                    last = mUnit;
                }
            }
        }
        if (last != null && (waypoint = last.getActiveWaypoint()) != null && (group = waypoint.group) != null) {
            group.updateGroupLeaders();
        }
    }

    public PathNode getActivePathNode() {
        if (this.activePathCount == 0) {
            return null;
        }
        return this.activePath[0];
    }

    public void clearPath() {
        this.activePathCount = 0;
        this.activePathTotalCount = 0;
        this.pathing_delay = 0.0f;
    }

    public void delayPathing() {
        this.pathing_delay = (float) (Math.random() * 120.0d);
    }

    public void completeActivePathNode() {
        if (this.activePathCount != 0) {
            if (this.activePathCount == 1) {
                this.activePathCount = 0;
                return;
            }
            PathNode first = this.activePath[0];
            for (int n = 0; n < 59; n++) {
                this.activePath[n] = this.activePath[n + 1];
            }
            this.activePath[59] = first;
            this.activePathCount--;
        }
    }

    public void getNewPath(float x, float y) {
        PathEngine path = GameEngine.getInstance().path;
        Map map = GameEngine.getInstance().map;
        this.moving = true;
        if (isFlying()) {
            this.activePathCount = 0;
            float endX = map.restrictXInPixels(x);
            float endY = map.restrictYInPixels(y);
            this.activePath[this.activePathCount].x = endX;
            this.activePath[this.activePathCount].y = endY;
            this.activePathCount++;
            this.activePathTotalCount = this.activePathCount;
            return;
        }
        path.startNew();
        map.toGrid(this.x, this.y);
        path.setSrc(getMovementType(), (short) map.returnX, (short) map.returnY);
        map.toGrid(x, y);
        path.setEnd((short) map.returnX, (short) map.returnY, (short) 0);
        path.findPath();
        this.activePathCount = 0;
        Iterator<PathEngine.Node> it = path.foundPath.iterator();
        while (it.hasNext()) {
            PathEngine.Node node = it.next();
            map.fromGrid(node.x, node.y);
            int i = map.returnX;
            map.getClass();
            float pointX = i + 10;
            int i2 = map.returnY;
            map.getClass();
            this.activePath[this.activePathCount].x = pointX;
            this.activePath[this.activePathCount].y = i2 + 10;
            this.activePathCount++;
            if (this.activePathCount >= maxPathNodes) {
                break;
            }
        }
        this.activePathTotalCount = this.activePathCount;
    }

    PathNode getPathNodeByIndex(int index) {
        if (index >= this.activePathCount) {
            return null;
        }
        return this.activePath[index];
    }

    /* loaded from: classes.dex */
    public strict class PathNode {
        float x;
        float y;

        public PathNode() {
        }
    }

    public RectF getImageDstRect() {
        GameEngine game = GameEngine.getInstance();
        this._tempF.set((this.x - (this.objectWidth / 2)) - game.viewpointX_rounded, (this.y - (this.objectHeight / 2)) - game.viewpointY_rounded, (this.x + (this.objectWidth / 2)) - game.viewpointX_rounded, (this.y + (this.objectHeight / 2)) - game.viewpointY_rounded);
        return this._tempF;
    }

    public Rect getImageSrcRect(boolean shadow) {
        this._temp.set(0, 0, this.objectWidth + 0, this.objectHeight + 0);
        return this._temp;
    }

    @Override // com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void drawUnder(float deltaSpeed) {
        super.drawUnder(deltaSpeed);
    }

    public float getDrawBaseDir() {
        return this.dir + 90.0f;
    }

    @Override // com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        // NO draw
    }

    public void drawShadow() {
        // NO draw
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public boolean isOnScreen() {
        GameEngine game = GameEngine.getInstance();
        this._dst.set(getImageDstRect());
        return RectF.intersects(game.viewpointRectF, this._dst);
    }

    public boolean isFixedFiring() {
        return false;
    }

    public int getMoveSlidingDir() {
        return -1;
    }

    public float getMoveAccelerationSpeed() {
        return 99.0f;
    }

    public float getMoveDecelerationSpeed() {
        return 99.0f;
    }

    public boolean resetTurret() {
        return true;
    }

    public float getTurretSize() {
        return 0.0f;
    }

    public PointF getTurretEnd() {
        float tSize = getTurretSize();
        float tDir = isFixedFiring() ? this.dir : this.turretDir;
        _pointF.set(this.x + (CommonUtils.cos(tDir) * tSize), this.y + (CommonUtils.sin(tDir) * tSize));
        return _pointF;
    }

    public void leaveScorchMark() {
        if (!isOverWater()) {
            ScorchMark.create(this.x, this.y);
        }
    }

    public boolean isOverWater() {
        GameEngine game = GameEngine.getInstance();
        game.map.toGrid(this.x, this.y);
        MapTile tile = game.map.getTileFromMainLayer(game.map.returnX, game.map.returnY);
        if (tile == null) {
            return false;
        }
        return tile.water;
    }

    public void seeThoughFogOfWar() {
        GameEngine game = GameEngine.getInstance();
        if (this.team == game.playerTeam && this.transportedBy == null) {
            game.map.seeThoughFogOfWar(this.x, this.y, 14);
        }
    }
}
