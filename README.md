An attempt to port [MaxLanana](url)'s ColorFlow from Fabric 1.21.4 to Neoforge 1.21.1

All credit and original idea goes to Max, and a huge thank to him aswell for letting me try.
ColorFlow's original description:




Colorflow
Stop Guessing Which Block Matches Your Color Palette

You've been there. You're building something big — a castle, a city, a terraformed landscape, a pixel art piece — and you need a specific color. Not "kinda brown." Not "something between terracotta and concrete." You need a precise shade, and you need to know which Minecraft block is the closest match.

The usual process: open a color picker on your phone, pick a color, then spend 20 minutes swapping between the game and a wiki trying to figure out which block is closest. Or you eyeball it and end up with something that looks off.

Colorflow puts the entire workflow inside Minecraft. Pick a color with professional tools. See the 9 closest blocks instantly. Generate smooth gradients between any two blocks. Fill your hotbar with one click. Never alt-tab for a color match again.
How It Works

Colorflow is a client-side Fabric mod. It adds a color picker GUI that opens with a single keybind. Everything happens on your client — no server installation required. It works on singleplayer, multiplayer, realms, build servers, creative worlds, anywhere you have Fabric installed.

The mod scans every block's default texture, calculates its average color value, and stores a color database at startup. When you pick a color, it runs a Delta-E 2000 comparison against every block in the database and returns the 9 closest matches, ranked by perceptual similarity.

Delta-E 2000 isn't a simple RGB distance calculation. It's the same color comparison algorithm used in professional print and design work. It accounts for how the human eye actually perceives color differences — a 10-point difference in red looks different from a 10-point difference in blue. The matches it produces LOOK right, not just mathematically close.
Features — In Detail
Color Picker (Professional Grade)

This isn't a janky in-game color wheel. The color picker interface is modeled after what you'd find in Figma or Photoshop:

2D Saturation/Value Palette A two-dimensional picker where you drag a cursor across a gradient surface. Horizontal axis = saturation. Vertical axis = value (brightness). You see the color update in real time as you move. This is how designers pick colors, and there's a reason — it's fast and intuitive.

Hue Spectrum Bar A vertical rainbow bar next to the main palette. Drag it to change the base hue. The 2D palette updates to show all saturation/brightness combinations for that hue. You can get to any color in the visible spectrum in two clicks and a drag.

RGB Sliders Three sliders — Red, Green, Blue — from 0 to 255. For when you know the exact values you want. If someone sends you "R: 180, G: 120, B: 75" for a medieval build, you punch it in and you're there.

HEX Input Type a hex code directly. Copy a color from a website, a texture pack preview, a design tool, or another builder's palette. Paste it in. Done. This alone saves minutes per color if you're working from a reference palette.

Live Preview with Variations The selected color shows in a preview panel with lighter and darker variations stacked beside it. Useful for deciding between adjacent shades — sometimes the slightly darker version of your color is a better match for what you're building.
Smart Block Matching

After you pick a color, Colorflow shows you the 9 closest Minecraft blocks ranked by similarity.

Delta-E 2000 Algorithm As mentioned above — the industry standard for perceptual color comparison. Not RGB Euclidean distance. Not HSL difference. Delta-E 2000, the same thing Adobe uses in Photoshop's color matching tools. The top result isn't just the mathematically closest color — it's the color that a human would say looks the most similar.

Works With All Blocks Every block in the game is included. All vanilla blocks, all 1.21.4 blocks, and — this is important — all modded blocks too. If you have Biomes O' Plenty or Create or Chipped installed, those blocks get scanned and added to the database automatically at startup. No configuration. No compatibility packs. It just reads what's available.

Full Blocks Only Toggle Some builders only want solid, full-cube blocks for cleaner constructions. A toggle in the settings filters out slabs, stairs, walls, fences, and other non-full blocks. Flip it on and every suggestion is a clean 1x1x1 cube.
Gradient Generator

This is the feature that makes Colorflow more than just a color picker.

Visual Block Browser A searchable grid of every block in the game, displayed as their actual textures. Type "terracotta" and you see all the terracotta variants. Type "concrete" and you see all 16 concrete colors. Scroll through everything or search by name.

Click-to-Select Start & End Block Click one block as your gradient start. Click another as your gradient end. Colorflow calculates the smoothest possible path between them through available block colors.

9-Step Gradients The gradient generator produces a 9-block sequence that transitions smoothly from your start block to your end block. 9 slots = 9 hotbar positions. Every step is chosen to minimize the visible color jump between adjacent blocks. The result looks like a smooth gradient when placed in-world, not a choppy mess of random colors.

One-Click Hotbar Fill Once you're happy with the gradient, click the hotbar button. All 9 blocks are placed into your hotbar in order. Ready to build. No creative inventory searching, no hand-placing blocks from a chest, no /item commands. Click and build.

This is transformative for:

    Terraforming (natural terrain gradients from stone to grass)
    Architecture (smooth color transitions on facades)
    Pixel art (consistent color stepping between shades)
    Landscaping (gradient flower beds, ocean depth, mountain shading)
    Interior design (matching furniture blocks to wall colors)

Who Is This For?

Builders in Creative Mode The obvious one. If you build seriously in creative — whether it's for a server, a YouTube series, a portfolio, or just personal projects — Colorflow removes the guesswork from color selection entirely.

Survival Builders Building in survival? You don't have instant access to every block. Colorflow helps you plan what materials you need before you start gathering. Pick your palette, see the blocks, make a shopping list.

Texture Pack Creators If you make resource packs, Colorflow works with any pack. The block database is generated from whatever textures are loaded. Use it to compare your new textures against vanilla blocks or to ensure color consistency across your pack.

Build Server Players Servers like BuildTheEarth, creative plot servers, building competitions — anywhere that block choice matters. Colorflow is client-side, so it works on any server without the server needing to install anything.

Pixel Artists Pixel art requires precise block selection for each pixel. The Delta-E 2000 matching eliminates the "I think this wool is close enough" guesswork. Pick your target color, get the closest block, place it.

Modpack Players Running a modpack with hundreds of new blocks? Colorflow auto-detects all of them. Every modded block gets included in the color database. No manual configuration.
Installation

Colorflow is a Fabric mod. You need Fabric Loader installed.

    Install Fabric Loader for Minecraft 1.21.4 → fabricmc.net

    Install Fabric API (the standard Fabric library mod) → drag it into your /mods/ folder

    Download Colorflow.jar from this page → drag it into your /mods/ folder

    Launch Minecraft with the Fabric profile

    Press the keybind (default: C) to open the color picker

That's it. Three files in your mods folder: Fabric API, and Colorflow. No configuration files to edit. No setup required.
Controls
Action 	Default Key 	Customizable?
Open color picker 	C 	Yes
Confirm selection 	Left Click 	—
Cancel / close 	Escape 	—
Toggle full blocks only 	In-GUI button 	—
Generate gradient 	In-GUI button 	—
Fill hotbar 	In-GUI button 	—

All keybinds are remappable through Minecraft's controls menu.
Configuration

Colorflow works out of the box with zero configuration. But if you want to tweak things:

The mod stores its settings in the Fabric mod config folder. Options include:

    Default color picker mode (HSV vs RGB)
    Full blocks only filter (on/off by default)
    Gradient step count (default 9, adjustable)
    Block database refresh behavior (auto on mod change, or manual)
    Keybind customization

That's it. The config is small because the mod doesn't need much configuration. It's a tool, not a system.
Performance

Colorflow is client-side only. It has zero server impact. Zero network overhead. No packets sent. No server-side processing.

Startup cost: The block color database is generated once at game start. On a vanilla instance, this takes under a second. With 200+ modded blocks, it might take 2-3 seconds. The database is cached, so subsequent color lookups are instant.

Runtime cost: Negligible. The color picker GUI is a standard screen. The Delta-E 2000 calculation runs against the cached database when you click "find matches" — this takes a few milliseconds. You will not see an FPS drop. You will not notice any lag. The mod does almost nothing when you're not actively using the picker.
Compatibility

Works with:

    All vanilla Minecraft blocks (1.21.4)
    All modded blocks (auto-detected at startup)
    All Fabric mods
    Any resource pack (reads loaded textures, not hardcoded values)
    Multiplayer / servers (client-side only, no server install needed)
    OptiFine / Sodium / Iris (no rendering conflicts — it's a GUI overlay)

Requirements:

    Minecraft Java Edition 1.21.4
    Fabric Loader
    Fabric API

Does NOT require:

    Server installation
    Any other mods
    Any resource packs
    Any configuration

FAQ

Does the server need to install this? No. Colorflow is 100% client-side. It reads block data from your own game. The server doesn't know it exists. You can use it on any server — vanilla, modded, Paper, Spigot, whatever. As long as YOU have Fabric, it works.

Will it get me banned on servers? Colorflow doesn't send any packets, modify any server interactions, or give any competitive advantage. It's a design tool — like having a color picker open on your second monitor, except it's inside the game. No anti-cheat will flag it because it doesn't do anything a cheat would do.

Does it work with shaders? Yes. The color picker reads block texture data, not rendered colors. Shaders change how things look on screen but don't change the base textures. Your color matches will be accurate regardless of whether you're running shaders.

Does it work with modded blocks? Yes, automatically. On startup, Colorflow scans every block registered in the game — vanilla and modded — and extracts the average color from each block's texture. If you install a new mod, the new blocks will be in the database next time you launch.

What if a block has multiple textures (like grass with a side and top)? The database uses the block's primary texture (the one most commonly seen in builds). For blocks with notably different faces, the most iconic texture is used. In most cases this is the top face.

How accurate is the color matching? Delta-E 2000 is the most accurate perceptual color distance metric available. A Delta-E value under 2 is generally imperceptible to the human eye. Colorflow's top matches are almost always visually indistinguishable from the target color. Second and third matches are close enough that the difference only matters in large flat surfaces.

Can I use this in a modpack? Yes. MIT license. Include it freely. Credit is appreciated but not required.

Why Fabric only? Fabric is the modding platform Colorflow was developed on. Forge port is possible in the future if there's demand. If you want Forge support, leave a comment — it helps gauge interest.

I found a block that's missing from the database. Some blocks with purely transparent or animated textures may not produce a meaningful average color. If you find a specific block that should match but doesn't appear, open a GitHub issue with the block name and your mod list.
Why This Exists

I build a lot. Terraforming, architecture, pixel art, interior design — I've been building in Minecraft for over a decade. And the single most time-consuming part of building, beyond planning and placing blocks, is choosing the RIGHT blocks.

Which concrete powder is closest to this terra cotta? Which wood type matches this wool? What's the smoothest gradient from stone to deepslate? These questions used to mean alt-tabbing to a wiki, guessing, placing a block, breaking it, trying another one, flying back to see how it looks from a distance, deciding it's wrong, trying again.

Colorflow answers those questions in seconds. Pick a color. See the blocks. Place them. Move on to the actually creative part of building.

It was inspired by the color tools in design software — Figma, Photoshop, Coolors — adapted for Minecraft's unique constraint: you can't use any color you want. You can only use the colors that exist as blocks. So the tool needs to work within that constraint, and it does.
Open Source

Colorflow is open source under the MIT License.

    Bug reports: https://discord.gg/pnJhKuU2QK
    Feature requests: https://discord.gg/pnJhKuU2QK

Contributions welcome. If you're a Fabric developer and want to improve the color algorithm, add features, or fix bugs, pull requests are open.

If Colorflow made your builds better and you want to support development:

    Share it with other builders

Built by a builder, for builders.
