/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm;

import static grondag.xm.XmConfig.DEFAULTS;
import static grondag.xm.XmConfig.debugCollisionBoxes;

import java.util.Arrays;
import java.util.stream.Collectors;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfigScreen {
	private static ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

	static Component[] parse(String key) {
		return Arrays.stream(I18n.get("config.xblocks.help.force_key").split(";")).map(s -> new TextComponent(s)).collect(Collectors.toList()).toArray(new Component[0]);
	}

	static Screen getScreen(Screen parent) {
		final ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableComponent("config.xm.title")).setSavingRunnable(ConfigScreen::saveUserInput);

		// DEBUG
		final ConfigCategory blocks = builder.getOrCreateCategory(new TranslatableComponent("config.xm.category.debug"));

		blocks.addEntry(ENTRY_BUILDER.startBooleanToggle(new TranslatableComponent("config.xm.value.debug_collision_boxes"), debugCollisionBoxes)
				.setDefaultValue(DEFAULTS.debugCollisionBoxes)
				.setSaveConsumer(b -> debugCollisionBoxes = b)
				.setTooltip(parse("config.xm.help.debug_collision_boxes"))
				.build());

		return builder.build();
	}

	private static void saveUserInput() {
		XmConfig.saveConfig();
	}
}
