# Lazy AI
<img src="https://github.com/PixelIndieDev/LazyAI/blob/main/documentation/logo/logo.png?raw=true" width="400" height="400">

Lazy AI optimizes Minecraft’s AI calculations to improve performance with as minimal gameplay impact as possible.

## Overview
Lazy AI dynamically reduces the frequency and precision of mob AI calculations based on their distance from players. Mobs close to players behave normally, while those farther away update their goals, pathfinding, and other goals less often. This ensures smoother performance, especially in mob-heavy worlds.

> [!IMPORTANT]
> Lazy AI bases its distance calculations of the simulation distance * *distance scaling* of your game

> [!WARNING]
> Lazy AI requires [fabric api](https://modrinth.com/mod/fabric-api)

## Performance difference
### Vanilla performance
![Vanilla performance](https://github.com/PixelIndieDev/LazyAI/blob/main/documentation/previewImages/MC_performance_Vanilla.png?raw=true)

### Lazy AI performance (using AIOptimizationType = Minimal)
![Lazy AI performance (using AIOptimizationType = Minimal)](https://github.com/PixelIndieDev/LazyAI/blob/main/documentation/previewImages/MC_performance_Minimal.png?raw=true)

### Lazy AI performance (using AIOptimizationType = Default)
![Lazy AI performance (using AIOptimizationType = Default)](https://github.com/PixelIndieDev/LazyAI/blob/main/documentation/previewImages/MC_performance_Default.png?raw=true)

### Lazy AI performance (using AIOptimizationType = Aggressive)
![Lazy AI performance (using AIOptimizationType = Aggressive)](https://github.com/PixelIndieDev/LazyAI/blob/main/documentation/previewImages/MC_performance_Aggressive.png?raw=true)

## Features
- **Distance-based AI scaling** | *Reduces AI updates for mobs that are far from players*
- **Pathfinding optimization** | *Simplifies A** *path calculations at long distances*
- **Reduced goal frequency** | *Look, wander, and tempt goals run less often when distant*
- **Configurable behavior** | *Easily balance performance and gameplay through a simple config accessible through [mod menu](https://modrinth.com/mod/modmenu)*
- **Server-wide improvement** | *Decreases tick load even on servers*

## Settings
- **AI Optimization Type** | *This settings controls how aggressive the optimizations should be*
- **Distance Scaling** | *This setting controls what % range of your simulation distance is considered close and far range*
- **Mob Tempting Delay** | *This setting controls how much delay animals have to being tempted by an item*
- **Disable Zombie Egg Stomping** | *This setting controls the prevention of zombies wanting to destroy turtle eggs*

![Lazy AI settings menu)](https://github.com/PixelIndieDev/LazyAI/blob/main/documentation/previewImages/MC_LazyAI_settings.png?raw=true)

## Compatibility
Minecraft: 1.21.6 — 1.21.8

## FAQ
### Can this mod reduce my TPS?
Yes, this mod can improve your TPS.

### What modloader do I need?
You need Fabric

### I installed the mod, but didn't see as much difference?
The preview images are taken in a stress test situation. It can also be because your simulation distance or this mods *distance scaling* is set too high for your use case.

### Where is the config located?
.minecraft/config/lazy-ai.json

### Can this mod be used on a server?
Yes, you can use this mod on a server. The mod works on both the server and the client.

### Can this mod be used on a client?
Yes, you can use this mod on a client. The mod works on both the server and the client.
