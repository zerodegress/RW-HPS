/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

plugins {
	`kotlin-dsl`
}

repositories {
	mavenLocal()
	google()
	mavenCentral()
	gradlePluginPortal()
}

kotlin {
	sourceSets.all {
		languageSettings.optIn("kotlin.Experimental")
		languageSettings.optIn("kotlin.RequiresOptIn")
		languageSettings.optIn("kotlin.ExperimentalStdlibApi")
	}
}

dependencies {
	api(gradleApi())
}