/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation.gameFramework.miss

import cn.rwhps.server.game.simulation.SyncedObject
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.util.alone.annotations.GameSimulationLayer
import cn.rwhps.server.util.log.exp.RwGamaException

@GameSimulationLayer.GameSimulationLayer_Processing
class MissionEngine : SyncedObject {
    //var allyTeam: Team? = null
    var firstLoop = false
    var lastUpdateTime = 0
    var mapWinCondition: WinConditions? = null
    var skirmish = false
    var survival = false
    var survival_against_aliens = false
    //var waveTextPaint: Paint? = null
    var winText: String? = null
    var mapLoseCondition: WinConditions = WinConditions.allUnitsAndBuildings
    var survival_wave = 0
    var survival_unitType = 0
    var survival_unitNumber = 2
    var survival_unitMegaTankNumber = 2
    var survival_unitPower = 0
    var survival_nextwave = 200.0f
    var survival_showmessage = 0.0f
    var survival_showmessagewait = 0.0f
    //var survival_attackPoints: ArrayList<PointF> = ArrayList<PointF>()

    override fun readIn(inputNetStream: GameInputStream) {
    }

    override fun writeOut(outputNetStream: GameOutputStream) {
    }

    fun init(boolean: Boolean) {
        firstLoop = true
        //val mapInfo: GroupObject = game.map.objectGroup_triggers.getGroupObjectByName("map_info")
        //survival = mapInfo.props.getProperty("type").equals("survival", ignoreCase = true)
        if (survival) {
            survival_against_aliens = false
        }
        //skirmish = mapInfo.props.getProperty("type").equals("skirmish", ignoreCase = true)
        if (skirmish) {
            mapWinCondition = WinConditions.allUnitsAndBuildings
        }
        val winConditionText: String = WinConditions.allUnitsAndBuildings.name
        if (winConditionText != null || skirmish) {
            if (winConditionText != null) {
                if (winConditionText.equals("none",ignoreCase = true)) {
                    mapWinCondition = WinConditions.none
                } else if (winConditionText.equals("allUnitsAndBuildings",ignoreCase = true)) {
                    mapWinCondition = WinConditions.allUnitsAndBuildings
                } else if (winConditionText.equals("allBuildings",ignoreCase = true)) {
                    mapWinCondition = WinConditions.allBuildings
                } else if (winConditionText.equals("mainBuilings",ignoreCase = true)) {
                    mapWinCondition = WinConditions.mainBuildings
                } else if (winConditionText.equals("mainBuildings",ignoreCase = true)) {
                    mapWinCondition = WinConditions.mainBuildings
                } else if (winConditionText.equals("commandCenter",ignoreCase = true)) {
                    mapWinCondition = WinConditions.commandCenter
                } else if (winConditionText.equals("requiredObjectives",ignoreCase = true)) {
                    mapWinCondition = WinConditions.requiredObjectives
                } else {
                    throw RwGamaException.RwGamaOtherException("unknown win condition:$winConditionText")
                }

            }
        }
    }
}