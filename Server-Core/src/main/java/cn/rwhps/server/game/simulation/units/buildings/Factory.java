package cn.rwhps.server.game.simulation.units.buildings;

import android.graphics.Rect;
import com.corrodinggames.rts.game.units.Unit;
import com.corrodinggames.rts.game.units.UnitType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/* loaded from: classes.dex */
public abstract class Factory extends Building {
    float queueBuilt;
    LinkedList<BuildQueueItem> buildQueue = new LinkedList<>();
    Rect _dst = new Rect();
    Rect _src = new Rect();

    public abstract void completeQueueItem(BuildQueueItem buildQueueItem);

    public boolean queueEmpty() {
        return this.buildQueue.size() == 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class BuildQueueItem {
        int amount;
        float buildSpeed;
        int type;

        BuildQueueItem() {
        }
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public float getSecBar() {
        return (!isActive() || queueEmpty()) ? super.getSecBar() : this.queueBuilt;
    }

    public int getSpecialActionFor(UnitType type) {
        return -1;
    }

    @Override // com.corrodinggames.rts.game.units.Unit
    public void giveSpecialActionOrder(Unit.SpecialAction action, boolean stopOrUndo) {
        if (action instanceof SpecialQueueAction) {
            SpecialQueueAction queueAction = (SpecialQueueAction) action;
            if (!stopOrUndo) {
                if (action.isActive(this) && this.team.tryToBuy(queueAction.getPrice())) {
                    addItemToQueue(queueAction);
                }
            } else if (removeItemInQueue(queueAction)) {
                this.team.credits += queueAction.getPrice();
            }
        }
    }

    public BuildQueueItem addItemToQueue(SpecialQueueAction specialAction) {
        BuildQueueItem item = new BuildQueueItem();
        item.type = specialAction.getIndex();
        item.amount = 1;
        item.buildSpeed = specialAction.getBuildSpeed();
        this.buildQueue.add(item);
        return item;
    }

    public boolean removeItemInQueue(SpecialQueueAction specialAction) {
        ListIterator<BuildQueueItem> li = this.buildQueue.listIterator(this.buildQueue.size());
        while (li.hasPrevious()) {
            if (li.previous().type == specialAction.getIndex()) {
                li.remove();
                return true;
            }
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void update(float deltaSpeed) {
        super.update(deltaSpeed);
        if (isActive() && !this.dead) {
            if (!queueEmpty()) {
                BuildQueueItem active = this.buildQueue.get(0);
                this.queueBuilt += active.buildSpeed * deltaSpeed;
                if (this.queueBuilt > 1.0f) {
                    this.queueBuilt = 0.0f;
                    completeQueueItem(active);
                    active.amount--;
                    if (active.amount <= 0) {
                        this.buildQueue.remove(0);
                        return;
                    }
                    return;
                }
                return;
            }
            this.queueBuilt = 0.0f;
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.Building, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.Unit, com.corrodinggames.rts.gameFramework.GameObject
    public void draw(float deltaSpeed) {
        super.draw(deltaSpeed);
    }

    public int getItemCountInQueue(int index) {
        int count = 0;
        Iterator<BuildQueueItem> it = this.buildQueue.iterator();
        while (it.hasNext()) {
            BuildQueueItem item = it.next();
            if (item.type == index) {
                count += item.amount;
            }
        }
        return count;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean canAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void fireProjectile(Unit target) {
        throw new RuntimeException("Unit cannot shoot");
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getMaxAttackRange() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getShootDelay() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float getTurrentTurnSpeed() {
        return 0.0f;
    }

    /* loaded from: classes.dex */
    public static abstract class SpecialQueueAction extends Unit.SpecialAction {
        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public int queueCount(Unit unit) {
            return ((Factory) unit).getItemCountInQueue(getIndex());
        }

        public float getBuildSpeed() {
            return 0.01f;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public boolean showPopupWindow() {
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.Unit.SpecialAction
        public boolean queueAble() {
            return true;
        }
    }
}
