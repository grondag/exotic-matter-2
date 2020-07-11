# Introduction
Exotic Matter (XM) is modeling library for Minecraft modding with powerful features:

A partial list of current capabilities: 
* Dynamic mesh specification, generation and baking
* Low-allocation design with high-performance caching, suitable for run-time operation
* Texture library and registry
* Automatic mesh painting based on texture and surface metadata
* Connected textures and shapes
* Multi-layer texture effects (currently up to three textures per surface)
* Multi-block textures
* Built-in mesh primitives
* Constructive Solid Geometry operarations on meshes
* Support for custom textures, primitives

# Things to Know
* XM is a work in progress. Expect significant bugs and breaking changes to be possible for some time.

* Canvas renderer is recommended but not required.  Canvas will be necessary to support some advanced planned features and becomes especially important for scenes with large numbers of multi-textured blocks. (Indigo handles extra texture layers as translucent quads. This works but increases buffer sizes and draw counts and causes expensive translucency resorts whenever the player moves. Canvas renders a multi-textured quad as a single quad in one pass, handling all texture blending in the fragment shader.)

* Code quality is variable.  Work on XM started in Minecraft 1.7.10 days, while I was still learning Java. 
Originally it was never meant to be offered as a library for other mods. It has been ported and refactored multiple times. Many things are the way they are based on hard-won experience. Constructive suggestions or even pull requests regarding specific functionality are welcome - stylistic complaints are likely to be ignored. 

# Texture Libraries
Starting with version 2.0, texture content is available in separate `exotic-art` mod libraries, also in this repo. Mods can also ship their own texture content for XM. See [Facility](https://github.com/grondag/facility) for an example of this.

# Getting started
There is no external documentation and the javadocs are sparse.

Until those problems are corrected, the best way to learn the basics is by cloning or forking [Exotic-Blocks](https://github.com/grondag/exotic-blocks) and studying the examples there.  Exotic Blocks is what I use to test currently-working features and the project itself demonstrated how to set up the dev environment.

You can also leave questions (or look for answers) on [my discord](https://discord.gg/7NaqR2e)

## Dev Environment Setup

XM has a hard dependency on a handful of Fermion libraries, Jankson and Cloth Config. It is recommended you include these and XM, along with required texture content, in your mod jar.   

XM also requires Fabric API but that should not be bundled in your jar. 

XM has soft dependencies on FREX, REI and Mod Menu.

In gradle this looks as follows:

```gradle
repositories {
    maven {
    	name = "dblsaiko"
    	url = "https://maven.dblsaiko.net/"
    }
}

dependencies {
    // other dependencies...

    // optional dev env annotation support
    compileOnly "org.apiguardian:apiguardian-api:1.0.0"
    compileOnly "com.google.code.findbugs:jsr305:3.0.2"
		
    modImplementation ("io.github.cottonmc:Jankson-Fabric:${jankson_version}") {
        exclude group :"net.fabricmc.fabric-api"
        exclude group :"net.fabricmc.fabric-loader"
    }
    
    modImplementation ("me.shedaniel:RoughlyEnoughItems:${project.rei_version}") {
        exclude module:  "ModMenu"
        exclude group: "net.fabricmc.fabric-api"
        exclude group :"net.fabricmc.fabric-loader"
	}
    
    modImplementation ("me.shedaniel.cloth:config-2:${clothconfig_version}") {
        exclude group :"io.github.prospector.modmenu"
        exclude group :"net.fabricmc.fabric-api"
        exclude group :"net.fabricmc.fabric-loader"
    }
	
    modImplementation ("io.github.prospector:modmenu:${project.modmenu_version}") {
        exclude group :"net.fabricmc.fabric-api"
        exclude group :"net.fabricmc.fabric-loader"
    }
    
    modImplementation ("grondag:exotic-matter-${project.mc_tag}:${project.exotic_matter_version}.+") { transitive = false }
    modImplementation ("grondag:exotic-art-core-${project.mc_tag}:${project.exotic_art_core_version}.+") { transitive = false }
    modImplementation ("grondag:frex-${project.mc_tag}:${project.frex_version}.+") { transitive = false }
    modImplementation ("grondag:fermion-${project.mc_tag}:${project.fermion_version}.+") { transitive = false }
    modImplementation ("grondag:fermion-modkeys-${project.mc_tag}:${project.fermion_modkeys_version}.+") { transitive = false }
    modImplementation ("grondag:fermion-varia-${project.mc_tag}:${project.fermion_varia_version}.+") { transitive = false }
    modImplementation ("grondag:fermion-orientation-${project.mc_tag}:${project.fermion_orientation_version}.+") { transitive = false }
    modImplementation ("grondag:special-circumstances-${project.mc_tag}:${project.special_circumstances_version}.+") { transitive = false }
	
    if (!(gradle.startParameter.taskNames.contains("publish") || gradle.startParameter.taskNames.contains("publishToMavenLocal"))) {
        include "grondag:exotic-matter-${project.mc_tag}:${project.exotic_matter_version}.+"
        include "grondag:exotic-art-core-${project.mc_tag}:${project.exotic_art_core_version}.+"
        include "grondag:fermion-${project.mc_tag}:${project.fermion_version}.+"
        include "grondag:fermion-modkeys-${project.mc_tag}:${project.fermion_modkeys_version}.+"
        include "grondag:fermion-varia-${project.mc_tag}:${project.fermion_varia_version}.+"
        include "grondag:fermion-orientation-${project.mc_tag}:${project.fermion_orientation_version}.+"
        include "grondag:special-circumstances-${project.mc_tag}:${project.special_circumstances_version}.+"
        include "io.github.cottonmc:Jankson-Fabric:${jankson_version}"
        include "me.shedaniel.cloth:config-2:${clothconfig_version}"
    }
}
```

XM is built for specific Minecraft versions, indicated by `mc_tag`. Minecraft 1.16.x has tag `mc116`.  You can hard-code this as `grondag:exotic-matter-mc116:${project.exotic_matter_version}.+` if you prefer. 

For the latest version numbers, see the [gradle.properties file](https://github.com/grondag/exotic-matter-2/blob/master/gradle.properties).

# Code License
Code in this mod is [licensed under the Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0). This means no warranty is provided.

# Texture License
<a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png" /></a><br />Textures in this work are licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License</a>.

