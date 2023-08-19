/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.game.enums

import net.rwhps.server.struct.IntMap
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.log.exp.VariableException

/**
 * @date 2023/8/6 20:06
 * @author RW-HPS/Dr
 */
enum class GameExternalUnits {
    aaBeamGunship,
    bomber,
    bugGeneratorN,
    bugGeneratorNT2,
    bugMeleeT31,
    bugBee,
    bugExtractor,
    bugExtractorT2,
    bugFly,
    bugGenerator,
    bugGeneratorT2,
    bugMelee,
    bugMeleeLarge,
    bugMeleeSmall,
    bugNest,
    bugRanged,
    bugSpore,
    bugTurret,
    bugPickup,
    bugWasp,
    bugRangedT2,
    combatEngineer,
    experiementalCarrier,
    experimentalDropship,
    experimentalGunship,
    experimentalGunshipLanded,
    experimentalSpider,
    c_experimentalTank,
    extractorT1,
    extractorT2,
    extractorT3,
    extractorT3_overclocked,
    extractorT3_reinforced,
    fabricatorT1,
    fabricatorT2,
    fabricatorT3,
    fireBee,
    heavyBattleship,
    heavyInterceptor,
    heavyMissileShip,
    heavySub,
    laboratory,
    lightGunship,
    lightSub,
    mechEngineer,
    mechFactory,
    mechFactoryT2,
    mechArtillery,
    mechBunker,
    mechBunkerDeployed,
    mechFlame,
    mechFlyingLanded,
    mechFlyingTakeoff,
    mechHeavyMissile,
    mechLaser,
    mechLightning,
    mechMinigun,
    mechGun,
    mechMissile,
    missileAirship,
    missileTank,
    missing,
    modularSpider_antiair,
    modularSpider_antiairFlak,
    modularSpider_antiairT2,
    modularSpider_antinuke,
    modularSpider_artillery,
    modularSpider_emptySlot,
    modularSpider_fabricator,
    modularSpider_fabricatorT2,
    modularSpider_gunturret,
    modularSpider_gunturretT2,
    modularSpider_laserdefense,
    modularSpider_lightning,
    modularSpider,
    modularSpider_nonEmpty,
    modularSpider_shieldGen,
    modularSpider_smallgunturret,
    modularSpider_smallgunturretT2,
    nautilusSubmarine,
    nautilusSubmarineLand,
    nautilusSubmarineSurface,
    robotCrab,
    robotCrabWater,
    outpostT1,
    outpostT2,
    plasmaTank,
    creditsCrates,
    crystal_mid,
    scout,
    heavyArtillery,
    antiAirTurretFlak,
    UNKNOWN;

    companion object {
        private val customUnitMap: IntMap<GameExternalUnits> = IntMap(entries.size)

        init {
            entries.forEach {
                if (customUnitMap.containsKey(it.ordinal)) {
                    throw VariableException.RepeatAddException("[GameUnitType -> GameCustomUnits]")
                }
                customUnitMap[it.ordinal] = it
            }
        }

        // 进行全匹配 查看是否在游戏内置列表中
        fun from(type: String?): GameExternalUnits? = entries.find { it.name == type || it.name.lowercase() == type?.lowercase() }

        fun from(type: Int): GameExternalUnits = customUnitMap[type].ifNullResult(UNKNOWN) { it }
    }
}