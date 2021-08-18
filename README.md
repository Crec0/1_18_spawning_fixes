# 1.18 Experimental 3 spawning fixes and optimizations
## Features
added command `improvedSpawning` to switch between different algorithms 
Four different rules are availabe to be choosen from
### vanilla
default vanilla spawning algorithm introduced in 1.18 experimental 3
### vanillaEmptySubchunkOptimization
Optimizes new spawning algorithm to skip empty subchunks.
### oldVanilla
Old heighmap based spawning algorithm which was in the game prior to 1.18 experimental 3
### normalDistributionSpawning
A new normal distribution based spawning algorithm. Mobs in player y level subchunk have highest spawn chance and it decreases further it goes from player (vertically).
This is my take on implementing mojang's reason to change the spawning algorithm. Now players can get more mobs near them. 
