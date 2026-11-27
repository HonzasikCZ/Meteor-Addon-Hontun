<p align="center">
  <img src="src/main/resources/assets/hontun/icon.png" width="120" alt="Hontun">
</p>

# METEOR ADDON - HONTUN

A Meteor Client addon for Minecraft 26.2 and 26.3 - fully recolorable interface that restyles Meteor's ClickGUI and Minecraft's own menus.

🌐 **Website:** [hontun.xyz](https://hontun.xyz) · [Try the ClickGUI in your browser](https://hontun.xyz/demo/)
🗨️ **Discord:** [discord.gg/KvfJnpnBjT](https://discord.gg/invite/KvfJnpnBjT)

Built heavily with AI.

Requires [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) and Fabric API.
[ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus) is optional - without it the version
picker is simply hidden and everything else works unchanged.

## 🧩 MODULES

- `Anticheat Detect` Tells you which anticheat the server runs when you join, then turns itself off.
  It sends nothing itself: it reads commands, plugin channels, ping IDs and setbacks, plus the
  result of `.server plugins` if you ran one.
- `Anti Exploit` Stops incoming packets from crashing or freezing your client. Dangerous packets and
  floods are dropped, illegal items and huge text are kept but cut down for display, and broken item
  or text packets are skipped instead of kicking you. An endless flood of registry data or queued
  packets disconnects you instead of hanging the game. Optional per-type debug log of exactly what
  was blocked.
- `BypassESP` Keeps players and entities drawn when a server anti-ESP plugin tries to hide them. It
  draws what your client receives, keeps the last known spot for `persist-seconds` after the server
  stops sending it, and with `sound-marks` marks the sounds and projectiles of hidden players and
  moves their box there. Boxes and tracers work like Meteor's ESP, in Hontun blue by default.
- `Captcha Solver` Reads a map captcha (Sonar, LimboFilter, nAntiBot, AquaAntiBot, ProstoCaptcha and
  similar) and types the code for you, or only suggests it if you prefer to type it yourself. Blank
  maps and busy map art are left alone instead of guessed.
- `Channel Fetcher` Records the plugin channels a server uses. The full payload data always goes to
  the client log, the chat shows a short version.
- `Channel Sender` Sends a custom payload on any channel, even unregistered ones. The data can be
  plain text, hex bytes or a 4-byte number.
- `DeathFreecam` Adds a `Freecam` button to the death screen so you can fly around after dying, with
  a level view and no inventory. A box and beam mark where you died and stay after you respawn until
  you get close. `R` takes you back to the death screen. Options: `auto-freecam`, `sprint-speed` and
  `bypass-insta-respawn`, which shows the death screen even on servers that respawn you at once.
- `Free Interact` Lets you attack while using an item, and attack or use items in a boat while you
  paddle.
- `Gamemode Notify` Alerts you when someone changes their gamemode.

## ⌨️ COMMANDS

- `.cmdprobe` `enum` Probes `/help` and `/execute run` tab completions to list the commands that plugin hiders try to hide, plus the plugins they reveal.<br>
  &emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`list` Lists the top-level commands in the command tree the server sent.<br>
  &emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`args <command>` Prints the server's tab completions for that command's arguments.<br>
  &emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`raw <buffer>` Sends the text exactly as typed (include the `/`) as a tab-completion request and prints the reply.
- `.copy` `tab`, `scoreboard` or `holograms on/off` Copies the server's tab list, sidebar or the
  holograms you click into configs for TAB, DecentHolograms, FancyHolograms and CMI. Add a plugin
  name to copy for that plugin only. Animated lines are recorded and saved as animations. Copies land
  in `hontun/copy/<server>/`, a new folder for every connection.
- `.inspect` `scoreboard` Shows how the sidebar is built: the plugin that most likely made it, the
  title and every line, and how each line is made. Hover for details, click to copy as MiniMessage.<br>
  &emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;`tab` Shows the tab list header and footer, real, fake and hidden players, TAB layouts and the plugins behind them.
- `.center` `position` Snaps you to the middle of your block. Plain `.center` does the same.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`look` Snaps your view to the nearest of the 8 directions and levels your pitch.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`both` Does `position` and `look` at once.
- `.foreach` Runs a command for every player (`players`) or a number of times, with optional delays.
  Use `%player%` for each player's name or `%iteration%` for the loop number (from 0).
- `.reconnect` Rejoins the current server (alias `.rejoin`). Add a name to rejoin as that offline
  account.
- `.toggletab` Locks the player list (tab) visible so you do not have to hold the key.
- `.server` `info` Opens the server info window on its Overview page. Plain `.server` does the same. Every option below opens its own page.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`anticheat` The detected anticheat and what it is based on, without sending anything.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`channels` The plugin channels captured while `Channel Fetcher` is on.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`plugins` Scans the server's plugins and sorts them into anticheat, anti-exploit, plugin hider and other. Falls back to `bypass` when nothing shows up.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`plugins default` The same scan without the `bypass` fallback.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`plugins bypass` Only the tab-completion tricks that get past plugin hiders.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`players` Finds the real online roster, flags players hidden from the tab list and lets you search them and copy their UUID.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`software` Guesses the server software and any proxy in front, without sending anything.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Paper, Purpur, Folia, Leaf, Canvas, FlameMC, DivineMC, Pufferfish, Pluto, Plazma, UniverseSpigot, Leaves, ShreddedPaper, GolemPaper,<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;CraftBukkit, Spigot, Vanilla, Fabric, Quilt, Forge, NeoForge, Sponge, Youer, Arclight, Mohist, Magma, Ketting, Banner,<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Minestom, Pumpkin, SteelMC, FerrumC, QuasarMC, PicoLimbo, NanoLimbo, LOOHP Limbo.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Proxies: Velocity, Velocity-CTD, VeloFlame, BungeeCord, Waterfall, Gate, ViaProxy.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Proxy limbos: LimboAPI, Sonar and VeloFlame verification worlds.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Behind server-side ViaVersion it reads the real backend version.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Each signal ends in (built-in) or (config), and hovering (config) shows the server setting it depends on.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;The Cookies page lists the cookies the server stored or asked for, also saved to `hontun/cookies/`.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`version` Works out the real backend version and shows every signal it rests on, without sending anything.

## 🎨 THEMES

✨ One color picker drives everything. Set `palette-color` and every accent, gradient and highlight
follows it live - no restart, no rebuilding the GUI.

Hontun's own settings have their own `Hontun` tab in the ClickGUI, right after `GUI`. Six looks,
switchable in `ClickGUI -> Hontun -> ui-mode`:

| Mode | Look |
|---|---|
| `Vanilla` | Minecraft menus keep their vanilla look. The server list still gets Hontun's Accounts, Proxies and version buttons in place of Meteor's and ViaFabricPlus's. The ClickGUI is built from Minecraft's own buttons and panels. |
| `HVanilla` | Minecraft's square shapes kept, drawn as translucent panels with a thin accent outline. |
| `Modern1` | Flat panels, soft gradients, rounded corners. |
| `Modern2` | Default. Chamfered corners, thin accent outlines with a soft glow, corner brackets, pixel icons next to labels. |
| `SmogClient` | Rounded black and white look from SmogClientPro, with the SF font and an animated particle background. Ignores `palette-color`. |
| `Terminal` | Square black console look with a forced monospace font. `palette-color` tints the text, borders and caret. |

Every mode also has its own ClickGUI. `click-gui` sits right under `ui-mode` and switches with it
whenever you change the mode, but you can set it to any of the six. Window positions are shared,
so a window you have dragged stays where you put it when you switch.

The `Terminal` ClickGUI works like a console: numbered tabs along the top, small category windows
and a key bar with the keys for the current screen and your last action, such as a module you
toggled. Module settings open as a file like `esp.conf`, and the GUI and Config tabs as `gui.conf`
and `config.conf`. Move with `j`/`k`, change a line with `Space` or `Enter`, step numbers and
choices with `h`/`l` and fold a section with `z`. The status line toggles the module and copies or
pastes the whole file with `:w copy` and `:r paste`. Typing on the modules screen finds modules and
settings, `Ctrl+F` finds from every tab, and `Esc` on any other tab goes back to the modules.

The `Terminal` group in the Hontun tab sets the `prompt`, how enabled modules are marked (`marker`),
`module-names`, `cursor-blink`, `line-numbers`, `print-animation` and optional `scanlines`. The
`Rain` group tunes the digital rain, also for the `Matrix` menu scene: `charset`, `mirror-glyphs`,
`speed`, `density` and `brightness`.

The ClickGUI also has a light mode. Tick `light-mode` in the same tab. Only the ClickGUI goes
light, Minecraft menus, chat, containers and the HUD stay dark. The `Terminal` ClickGUI has no
light mode.

## 🖥️ INTERFACE

- `Title screen` The Hontun badge replaces the Minecraft wordmark, tinted by the palette with a
  soft glow (`Terminal` draws it without the glow, `SmogClient` shows its own white SMOG logo
  instead). The vanilla splash text is hidden. `Vanilla` mode keeps the original title.
- `Multiplayer` Session header with your skin head, the account you are logged in as, and whether a
  proxy is active. In `Modern2` and `SmogClient` the server list is drawn as cards, and in
  `Terminal` as console rows, each with the server icon, up to two MOTD lines, and the country
  flag, player count and ping along the top right.
- `Server list info` Select a server and the space right of the list shows what Hontun knows about
  it: the address and DNS route, DDoS protection (TCPShield, NeoProtect, Infinity-Filter), country,
  ping history and your visits. Every visit is logged per server in `hontun/server-log/`, with the
  servers a proxy moved you between (Server 1, Server 2 and so on) and the last software, plugins and
  resource packs Hontun saw there. The panel hides itself when the window is too narrow.
- `Server info window` `.server` opens a window with everything Hontun found about the server you
  are on, split into pages: Overview, Software, Anticheat, Version, Plugins, Players, World,
  Connection, Channels and Cookies. The Overview shows the MOTD exactly as the server list does, the
  server icon (click to view or save it) and the address to copy. Connection lists every resource
  pack with when it loaded and, when it can be told, whether the proxy or the server sent it.
- `Country flags` The hosting country of each server, resolved lazily in the background. Lookups
  only start once that server has finished pinging, so the flag never delays its ping result.
  Turn it off with `server-country-flags` if you would rather not send server addresses to a
  third-party lookup.
- `Connect screen` Replaces the single status line with a phase checklist and per-phase timings,
  shown as soon as you start connecting, so a stalled join shows you exactly where it stalled.
- `Accounts` Themed account manager with a 3D model of the active account, search, and offline /
  Microsoft / The Altening / session-token login.
- `Proxies` Themed proxy manager with the full add form, plus status and latency for every proxy
  when you press `Check all`.
- `Versions` Themed protocol-version picker with the same per-version icons ViaFabricPlus uses,
  reachable from the multiplayer screen. Per-server overrides are available from `Edit` and from
  `Direct Connection`.
- `Containers` Inventories, chests, furnaces and other containers use the active theme, recipe book
  button included. The backdrop behind them is configurable (`container-background`).
- `Sliders` FOV, volume and every other slider get a themed track and knob.
- `Menu background` Animated gradient with accent particles instead of the panorama
  (`menu-background`). `menu-scene` picks the scene: `BlockyParticles` (square particles, default),
  `RoundedParticles` (SmogClient's soft round particles in your colours), `BlockyHorizon` (a
  shader-drawn blocky night landscape) or `Matrix` (accent-tinted digital rain) behind every menu
  shown without a world. The pause screen and the other menus over a world show the same scene as
  a see-through overlay, so the game stays visible. SmogClient keeps its black and white colours in
  every scene and draws its round particles for both particle scenes. `Terminal` shows the digital rain whatever `menu-scene` says while its `rain` setting is
  on, which is the default.
- `Cursors` The mouse cursor, the text and resize cursors and the in-game crosshair match the active
  look and colour. Turn them off with `custom-cursor` and `custom-crosshair`. `Vanilla` keeps the
  defaults.
- `Screen transitions` Menus, Hontun screens and the ClickGUI fade from one screen to the next instead
  of switching at once. Pick `FadeZoom`, `Fade` or `Off` in `screen-transition`. In `Terminal`,
  `FadeZoom` slides the old screen down a few pixels instead of growing it, and the top bar and key
  bar stay in place. `Vanilla` keeps instant switching.
- `Lag Notifier` Meteor's own lag-notifier element is restyled in the active theme (themed panel and
  accent), and shows how long the server has gone without responding in both seconds and
  milliseconds.
- `Chat` `[Hontun]` prefix in the accent color.

Every screen follows the palette colour live, except in `SmogClient`, which keeps its own black and
white, and `Vanilla`, which keeps Minecraft's own look. Pick the mode in
`ClickGUI -> Hontun -> ui-mode`, then click a theme below to expand its screenshots.

<details open>
<summary><b>Modern2</b> - chamfered corners, accent glow, corner brackets (default)</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/HModern2_main.png" alt="Modern2 title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/HModern2_multiplayer.png" alt="Modern2 server list"></td>
</tr>
<tr>
<td><b>Reorder by dragging</b><br><img width="100%" src="docs/screenshots/HModern2_multiplayer_drag.png" alt="Modern2 drag to reorder"></td>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/HModern2_accounts.png" alt="Modern2 accounts"></td>
</tr>
<tr>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/HModern2_versions.png" alt="Modern2 versions"></td>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/HModern2_proxies.png" alt="Modern2 proxies"></td>
</tr>
<tr>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/HModern2_clickgui.png" alt="Modern2 ClickGUI"></td>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/HModern2_clickgui_module.png" alt="Modern2 module settings"></td>
</tr>
<tr>
<td width="50%"><b>Server list info</b><br><img width="100%" src="docs/screenshots/HModern2_server_panel.png" alt="Modern2 server list info"></td>
<td width="50%"><b>Server info window</b><br><img width="100%" src="docs/screenshots/HModern2_server_menu.png" alt="Modern2 server info window"></td>
</tr>
</table>
</details>

<details>
<summary><b>Modern1</b> - flat panels, soft gradients, rounded corners</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/HModern1_main.png" alt="Modern1 title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/HModern1_multiplayer.png" alt="Modern1 server list"></td>
</tr>
<tr>
<td><b>Reorder by dragging</b><br><img width="100%" src="docs/screenshots/HModern1_multiplayer_drag.png" alt="Modern1 drag to reorder"></td>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/HModern1_accounts.png" alt="Modern1 accounts"></td>
</tr>
<tr>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/HModern1_versions.png" alt="Modern1 versions"></td>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/HModern1_proxies.png" alt="Modern1 proxies"></td>
</tr>
<tr>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/HModern1_clickgui.png" alt="Modern1 ClickGUI"></td>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/HModern1_clickgui_module.png" alt="Modern1 module settings"></td>
</tr>
<tr>
<td width="50%"><b>Server list info</b><br><img width="100%" src="docs/screenshots/HModern1_server_panel.png" alt="Modern1 server list info"></td>
<td width="50%"><b>Server info window</b><br><img width="100%" src="docs/screenshots/HModern1_server_menu.png" alt="Modern1 server info window"></td>
</tr>
</table>
</details>

<details>
<summary><b>SmogClient</b> - rounded black and white look with the SF font</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/SmogClient_main.png" alt="SmogClient title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/SmogClient_multiplayer.png" alt="SmogClient server list"></td>
</tr>
<tr>
<td><b>Reorder by dragging</b><br><img width="100%" src="docs/screenshots/SmogClient_multiplayer_drag.png" alt="SmogClient drag to reorder"></td>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/SmogClient_accounts.png" alt="SmogClient accounts"></td>
</tr>
<tr>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/SmogClient_versions.png" alt="SmogClient versions"></td>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/SmogClient_proxies.png" alt="SmogClient proxies"></td>
</tr>
<tr>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/SmogClient_clickgui.png" alt="SmogClient ClickGUI"></td>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/SmogClient_clickgui_module.png" alt="SmogClient module settings"></td>
</tr>
<tr>
<td width="50%"><b>Server list info</b><br><img width="100%" src="docs/screenshots/SmogClient_server_panel.png" alt="SmogClient server list info"></td>
<td width="50%"><b>Server info window</b><br><img width="100%" src="docs/screenshots/SmogClient_server_menu.png" alt="SmogClient server info window"></td>
</tr>
</table>
</details>

<details>
<summary><b>Terminal</b> - square black console look with a monospace font</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/Terminal_main.png" alt="Terminal title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/Terminal_multiplayer.png" alt="Terminal server list"></td>
</tr>
<tr>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/Terminal_accounts.png" alt="Terminal accounts"></td>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/Terminal_versions.png" alt="Terminal versions"></td>
</tr>
<tr>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/Terminal_proxies.png" alt="Terminal proxies"></td>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/Terminal_clickgui.png" alt="Terminal ClickGUI"></td>
</tr>
<tr>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/Terminal_clickgui_module.png" alt="Terminal module settings as esp.conf"></td>
<td><b>Find</b><br><img width="100%" src="docs/screenshots/Terminal_find.png" alt="Terminal find window"></td>
</tr>
<tr>
<td width="50%"><b>Server list info</b><br><img width="100%" src="docs/screenshots/Terminal_server_panel.png" alt="Terminal server list info"></td>
<td width="50%"><b>Server info window</b><br><img width="100%" src="docs/screenshots/Terminal_server_menu.png" alt="Terminal server info window"></td>
</tr>
</table>
</details>

<details>
<summary><b>HVanilla</b> - Minecraft's square shapes as translucent panels with a thin accent outline</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/HVanilla_main.png" alt="HVanilla title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/HVanilla_multiplayer.png" alt="HVanilla server list"></td>
</tr>
<tr>
<td><b>Reorder by dragging</b><br><img width="100%" src="docs/screenshots/HVanilla_multiplayer_drag.png" alt="HVanilla drag to reorder"></td>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/HVanilla_accounts.png" alt="HVanilla accounts"></td>
</tr>
<tr>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/HVanilla_versions.png" alt="HVanilla versions"></td>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/HVanilla_proxies.png" alt="HVanilla proxies"></td>
</tr>
<tr>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/HVanilla_clickgui.png" alt="HVanilla ClickGUI"></td>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/HVanilla_clickgui_module.png" alt="HVanilla module settings"></td>
</tr>
<tr>
<td width="50%"><b>Server list info</b><br><img width="100%" src="docs/screenshots/HVanilla_server_panel.png" alt="HVanilla server list info"></td>
<td width="50%"><b>Server info window</b><br><img width="100%" src="docs/screenshots/HVanilla_server_menu.png" alt="HVanilla server info window"></td>
</tr>
</table>
</details>

<details>
<summary><b>Vanilla</b> - vanilla-looking Minecraft menus, ClickGUI made from Minecraft's own textures</summary>
<table>
<tr>
<td width="50%"><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/Vanilla_clickgui.png" alt="Vanilla ClickGUI"></td>
<td width="50%"><b>Module settings</b><br><img width="100%" src="docs/screenshots/Vanilla_clickgui_module.png" alt="Vanilla module settings"></td>
</tr>
<tr>
<td><b>Server list info</b><br><img width="100%" src="docs/screenshots/Vanilla_server_panel.png" alt="Vanilla server list info"></td>
<td><b>Server info window</b><br><img width="100%" src="docs/screenshots/Vanilla_server_menu.png" alt="Vanilla server info window"></td>
</tr>
</table>
</details>

<details>
<summary><b>ClickGUI</b> - dark and light mode</summary>
<table>
<tr>
<td width="50%"><b>Dark</b><br><img width="100%" src="docs/screenshots/ClickGUI_dark.png" alt="ClickGUI dark"></td>
<td width="50%"><b>Light</b><br><img width="100%" src="docs/screenshots/ClickGUI_light.png" alt="ClickGUI light"></td>
</tr>
</table>
</details>

## 🔨 BUILDING

Needs JDK 25.

```
./gradlew build
./gradlew build -Pminecraft_version=26.3
```

The first line builds the Minecraft 26.2 jar, the second the 26.3 jar. Both land in `build/libs/`
as `hontun-<version>-26.2.jar` and `hontun-<version>-26.3.jar`. On Windows use `.\gradlew` and
quote the argument in PowerShell: `.\gradlew build "-Pminecraft_version=26.3"`.

## 🙏 CREDITS

- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus)
- [Catppuccin Addon](https://github.com/X-C-0/catppuccin-addon) by Pindour - ClickGUI
- [DupersUnited](https://github.com/YAYLOLDEV/du-addon-public) by YAYLOLDEV - part of Real Version
- [Meteor Rejects](https://github.com/AntiCope/meteor-rejects) by Cloudburst - `Gamemode Notify`
- [AntiP2W-Addon](https://github.com/AntiP2WDevelopment/AntiP2W-Addon) by 0x06 - `Free Interact`, `Anti Exploit`
- [ParadiseClient-X](https://github.com/ParadiseDevelopments/ParadiseClient-X) by SpigotRCE - `.toggletab`
- [LiquidBounce](https://github.com/CCBlueX/LiquidBounce) by CCBlueX - `Anticheat Detect`
- [FDPClient](https://github.com/SkidderMC/FDPClient) by SkidderMC - `Anticheat Detect`
- [Draggable Lists](https://github.com/mrmelon54/draggable_lists) by MrMelon54 - drag to reorder servers and packs
