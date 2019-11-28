/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.api.item;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmRegistryImpl;

@API(status = EXPERIMENTAL)
public class XmItemRegistry {
	private XmItemRegistry() {}

	public static void addItem(Item item, Function<ItemStack, MutableModelState> modelFunction) {
		XmRegistryImpl.register(item, modelFunction);
	}
}
