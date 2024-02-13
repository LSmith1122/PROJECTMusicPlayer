# Music Player
### Overview
This is an example music player mobile Android application. 
The goal of this project is to showcase my current coding patterns and practices, while also utilizing this concept and space as a sandbox for practicing modern Android-specific technologies. 
This project began as a coursework project submission for the Udacity "Grow with Google Challenge" and "Android Basics Nanodegree" programs in 2018 (created / submitted by me, Lloyd). 
After leaping into the tech industry, this music player app project has been used as a "checkpoint" for me throughout the years; an occassional tool for identifying areas for improvement, 
and a reference for others to review my software development progress every step of the way.

#### Important Note:
Although this is currently an incomplete, WIP proof-of-concept, I plan to use publish and distribute this project later for others to consume. With that said, this project is not open-sourced
or available for others to consume, copy, revise, publish nor distribute in any manner.

---

### Core Functions / Implementations
- The primary language of this project has been migrated from Java to Kotlin
- UI implementation patterns have been upgraded to utilize Android Jetpack Compose
- Basic music player controls:
  - Audio selection
  - Play/pause
  - Skip/previous
  - Skip/next
  - Seek (via slider UI component)
- Basic music selection functionality:
  - View audio track info
  - View audio track album art
    - Note: This is now being retrieved and cached asynchronously once the list item is being shown on-screen
