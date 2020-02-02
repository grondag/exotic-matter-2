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
* XM is *big*.  As more textures are added, it will only get bigger.  Do not nest it.  If you are looking for a lightweight, minimal-dependency library, this isn't it.
* XM is currently in early alpha. Expect significant bugs and breaking changes to be the norm for some time.

* Canvas renderer is recommended but not required.  Canvas will be necessary to support some advanced planned features and becomes especially important for scenes with large numbers of multi-textured blocks. (Indigo handles extra texture layers as translucent quads. This works but increases buffer sizes and draw counts and causes expensive translucency resorts whenever the player moves. Canvas renders a multi-textured quad as a single quad in one pass, handling all texture blending in the fragment shader.)

* Code quality is variable.  Work on XM started in Minecraft 1.7.10 days, while I was still learning Java. 
Originally it was never meant to be offered as a library for other mods. It has been ported and refactored multiple times. Many things are the way they are based on hard-won experience. Constructive suggestions or even pull requests regarding specific functionality are welcome - stylistic complaints are likely to be ignored. 

# Getting started
There is no external documentation (this wiki is a lie!) and the javadocs, when they exist at all, may be actively misleading due to the many changes this codebase has seen.

Until those problems are corrected, the best way to learn the basics is by cloning or forking [Exotic-Blocks](https://github.com/grondag/exotic-blocks) and studying the examples there.  Exotic Blocks is what I use to test currently-working features and the project itself demonstrated how to set up the dev environment.

You can also leave questions (or look for answers) on [my discord](https://discord.gg/7NaqR2e)

## Dev Environment Setup

```gradle
repositories {
    // Grondag's crappy maven repo...
    maven {
        name = "grondag"
        url = "https://grondag-repo.appspot.com"
        credentials {
            username "guest"
            password ""
	}
    }
}

dependencies {
    // other dependencies...

    modImplementation ("grondag:exotic-matter-${project.mc_tag}:${project.exotic_matter_version}.+") {
        exclude group :"net.fabricmc.fabric-api"
        exclude group :"io.github.prospector.modmenu"
    }
}
```

XM is built for specific Minecraft versions, indicated by `mc_tag`. Minecraft 1.15.x has tag `mc115`.  You can hard-code this as `grondag:exotic-matter-mc115:${project.exotic_matter_version}.+` if you prefer. 

For the latest XM version number, see the [gradle.properties file](https://github.com/grondag/exotic-matter-2/blob/master/gradle.properties).

Lastly, be sure to point users of your mod to the [XM download on Curse](https://www.curseforge.com/minecraft/mc-mods/exotic-matter-library) and add XM to your `fabric.mod.json` file as a dependency, for example:

```json
"depends": {
    "fabricloader": ">=0.7.1",
    "minecraft": ">=1.15.2",
    "fabric": "*",
    "exotic-matter": "*"
}
```

# Code License
Code in this mod is [licensed under the Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0). This means no warranty is provided.

# Texture License
<a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png" /></a><br />Textures in this work are licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License</a>.

