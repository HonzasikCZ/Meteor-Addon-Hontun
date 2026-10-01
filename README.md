<p align="center">
  <img src="src/main/resources/assets/hontun/icon.png" width="120" alt="Hontun">
</p>

# METEOR ADDON - HONTUN

A Meteor Client addon for Minecraft 26.2 - fully recolorable interface that restyles Meteor's ClickGUI and Minecraft's own menus.

Built heavily with AI.

Requires [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) and Fabric API.
[ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus) is optional - without it the version
picker is simply hidden and everything else works unchanged.

## MODULES

- `Anti-Exploit` Cancels incoming packets that would crash or freeze your client, with an optional
  per-type debug log of exactly what was blocked.
- `Channel Fetcher` Records the plugin channels a server uses.
- `Channel Sender` Sends a custom payload on any channel, even unregistered ones. The data can be
  plain text, hex bytes or a 4-byte number.
- `Free Interact` Removes a couple of client-side interaction limits.
- `Gamemode Notify` Alerts you when someone changes their gamemode.

## COMMANDS

- `.cmdprobe` Enumerates commands that plugin hiders try to hide.
- `.center` `position` Snaps you to the middle of your block. Plain `.center` does the same.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`look` Snaps your view to the nearest of the 8 directions and levels your pitch.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`both` Does `position` and `look` at once.
- `.foreach` Runs a command per player or iteration, with optional delays. Use `%player%` as the
  placeholder for each player's name.
- `.reconnect` Rejoins the current server (alias `.rejoin`).
- `.toggletab` Locks the player list (tab) visible so you do not have to hold the key.
- `.server` `info` Meteor's server info plus backend version, world, channels and resource pack.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`channels` Lists the plugin channels captured so far.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`plugins` Scans the server's plugins, falls back to `bypass` when nothing shows up.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`plugins default` The same scan without the `bypass` fallback.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`plugins bypass` Only the tab-completion tricks that get past plugin hiders.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`players` Finds the real online roster and flags players hidden from the tab list.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;`software` Guesses the server software and any proxy in front, without sending anything.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Paper, Purpur, Folia, Leaf, Canvas, DivineMC, Pufferfish, Pluto, UniverseSpigot, Leaves,<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;CraftBukkit, Spigot, Vanilla, Fabric, Quilt, Forge, NeoForge, Sponge, Youer, Arclight,<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Minestom, PicoLimbo, NanoLimbo, LOOHP Limbo, QuasarMC.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Proxies: Velocity, Velocity-CTD, BungeeCord, Gate, ViaProxy.<br>
  &emsp;&emsp;&emsp;&ensp;&thinsp;&thinsp;&thinsp;&hairsp;&emsp;&emsp;&emsp;&emsp;&ensp;&thinsp;Behind server-side ViaVersion it reads the real backend version.

## THEMES

One color picker drives everything. Set `palette-color` and every accent, gradient and highlight
follows it live - no restart, no rebuilding the GUI.

Five looks, switchable in `ClickGUI -> GUI -> ui-mode`:

| Mode | Look |
|---|---|
| `Vanilla` | Minecraft menus stay untouched. The ClickGUI is built from Minecraft's own buttons and panels. |
| `HVanilla` | Minecraft's square shapes kept, drawn as translucent panels with a thin accent outline. |
| `HModern1` | Flat panels, soft gradients, rounded corners. |
| `HModern2` | Default. Chamfered corners, thin accent outlines with a soft glow, corner brackets, pixel icons next to labels. |
| `SmogClient` | Rounded black and white look from SmogClientPro, with the SF font and an animated particle background. Ignores `palette-color`. |

Every mode also has its own ClickGUI. `click-gui` sits right under `ui-mode` and switches with it
whenever you change the mode, but you can set it to any of the five. Window positions are shared,
so switching never moves your windows.

The ClickGUI also has a light mode. Tick `light-mode` in the same tab. Only the ClickGUI goes
light, Minecraft menus, chat, containers and the HUD stay dark.

## INTERFACE

- `Title screen` The Hontun badge replaces the Minecraft wordmark, tinted by the palette with a
  soft glow. The vanilla splash text is hidden. `Vanilla` mode keeps the original title.
- `Multiplayer` Session header with your skin head, the account you are logged in as, and whether a
  proxy is active. In `HModern2` the server list is drawn as cards with the server icon, one MOTD
  line, and a right-hand column of country flag, player count and ping.
- `Country flags` The hosting country of each server, resolved lazily in the background. Lookups
  only start once a server has finished pinging, so they never compete with Minecraft for DNS.
  Turn it off with `server-country-flags` if you would rather not send server addresses to a
  third-party lookup.
- `Connect screen` Replaces the single status line with a phase checklist and per-phase timings,
  visible from the first frame, so a stalled join shows you exactly where it stalled.
- `Accounts` Themed account manager with a 3D model of the active account, search, and offline /
  Microsoft / The Altening / session-token login.
- `Proxies` Themed proxy manager with the full add form, live status and latency.
- `Versions` Themed protocol-version picker with the same per-version icons ViaFabricPlus uses,
  reachable from the multiplayer screen. Per-server overrides are available from `Edit` and from
  `Direct Connection`.
- `Containers` Inventories, chests, furnaces and other containers use the active theme, recipe book
  button included. The backdrop behind them is configurable (`container-background`).
- `Sliders` FOV, volume and every other slider get a themed track and knob.
- `Menu background` Animated gradient with accent particles instead of the panorama
  (`menu-background`). SmogClient has its own animated background.
- `Lag Notifier` Meteor's own lag-notifier element is restyled in the active theme (themed panel and
  accent), and shows how long the server has gone without responding in both seconds and
  milliseconds.
- `Chat` `[Hontun]` prefix in the accent color.

Every screen is themed live by the palette colour. Pick the mode in
`ClickGUI -> GUI -> ui-mode`, then click a theme below to expand its screenshots.

<details open>
<summary><b>HModern2</b> - chamfered corners, accent glow, corner brackets (default)</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/HModern2_main.png" alt="HModern2 title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/HModern2_multiplayer.png" alt="HModern2 server list"></td>
</tr>
<tr>
<td><b>Reorder by dragging</b><br><img width="100%" src="docs/screenshots/HModern2_multiplayer_drag.png" alt="HModern2 drag to reorder"></td>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/HModern2_accounts.png" alt="HModern2 accounts"></td>
</tr>
<tr>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/HModern2_versions.png" alt="HModern2 versions"></td>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/HModern2_proxies.png" alt="HModern2 proxies"></td>
</tr>
<tr>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/HModern2_clickgui.png" alt="HModern2 ClickGUI"></td>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/HModern2_clickgui_module.png" alt="HModern2 module settings"></td>
</tr>
</table>
</details>

<details>
<summary><b>HModern1</b> - flat panels, soft gradients, rounded corners</summary>
<table>
<tr>
<td width="50%"><b>Title screen</b><br><img width="100%" src="docs/screenshots/HModern1_main.png" alt="HModern1 title screen"></td>
<td width="50%"><b>Server list</b><br><img width="100%" src="docs/screenshots/HModern1_multiplayer.png" alt="HModern1 server list"></td>
</tr>
<tr>
<td><b>Reorder by dragging</b><br><img width="100%" src="docs/screenshots/HModern1_multiplayer_drag.png" alt="HModern1 drag to reorder"></td>
<td><b>Accounts</b><br><img width="100%" src="docs/screenshots/HModern1_accounts.png" alt="HModern1 accounts"></td>
</tr>
<tr>
<td><b>Versions</b><br><img width="100%" src="docs/screenshots/HModern1_versions.png" alt="HModern1 versions"></td>
<td><b>Proxies</b><br><img width="100%" src="docs/screenshots/HModern1_proxies.png" alt="HModern1 proxies"></td>
</tr>
<tr>
<td><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/HModern1_clickgui.png" alt="HModern1 ClickGUI"></td>
<td><b>Module settings</b><br><img width="100%" src="docs/screenshots/HModern1_clickgui_module.png" alt="HModern1 module settings"></td>
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
</table>
</details>

<details>
<summary><b>Vanilla</b> - untouched Minecraft menus, ClickGUI made from Minecraft's own textures</summary>
<table>
<tr>
<td width="50%"><b>ClickGUI</b><br><img width="100%" src="docs/screenshots/Vanilla_clickgui.png" alt="Vanilla ClickGUI"></td>
<td width="50%"><b>Module settings</b><br><img width="100%" src="docs/screenshots/Vanilla_clickgui_module.png" alt="Vanilla module settings"></td>
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

## BUILDING

Needs JDK 25.

```
./gradlew build
```

The jar lands in `build/libs/`.

## CREDITS

- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus)
- [Catppuccin Addon](https://github.com/X-C-0/catppuccin-addon) by Pindour - ClickGUI
- [DupersUnited](https://github.com/YAYLOLDEV/du-addon-public) by YAYLOLDEV - part of Real Version
- [Meteor Rejects](https://github.com/AntiCope/meteor-rejects) by Cloudburst - `Gamemode Notify`
- [AntiP2W-Addon](https://github.com/AntiP2WDevelopment/AntiP2W-Addon) by 0x06 - `Free Interact`, `Anti-Exploit`
- [ParadiseClient-X](https://github.com/ParadiseDevelopments/ParadiseClient-X) by SpigotRCE - `.toggletab`
