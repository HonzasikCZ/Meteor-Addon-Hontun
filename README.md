<p align="center">
  <img src="src/main/resources/assets/hontun/icon.png" width="120" alt="Hontun">
</p>

# METEOR ADDON - HONTUN

A Meteor Client addon for Minecraft 26.2 - client-side anti-crash, server recon, and a fully
recolorable interface that restyles Meteor's ClickGUI and Minecraft's own menus.

Built heavily with AI assistance.

Requires [Meteor Client](https://github.com/MeteorDevelopment/meteor-client) and Fabric API.
[ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus) is optional - without it the version
picker is simply hidden and everything else works unchanged.

## MODULES

- `Anti-Exploit` Cancels incoming packets that would crash or freeze your client, with an optional
  per-type debug log of exactly what was blocked.
- `Channel Fetcher` Records the plugin channels a server uses.
- `Channel Sender` Sends a custom payload on any channel, even unregistered ones.
- `Free Interact` Removes a couple of client-side interaction limits.
- `Gamemode Notify` Alerts you when someone changes their gamemode.
- `No World Border` Hides the world border on the client (visual and local collision).
- `World Guard Bypass` Position-spoof test for whether a server validates movement against region
  protection. Intended for testing your own server.

## COMMANDS

- `.cmdprobe` Enumerates commands a CommandWhitelist tries to hide.
- `.center` `position` snaps you to the middle of your block, `look` snaps your view to the nearest
  clean direction.
- `.foreach` Runs a command per player or iteration, with optional delays.
- `.reconnect` Rejoins the current server (alias `.rejoin`).
- `.server` Adds real backend version, world info, plugin channels, the pushed resource-pack
  (clickable URL + SHA-1) and a plugin scan to Meteor's server info. `.server channels` lists
  captured channels, `.server plugins` scans plugins, `.server players` probes the real online
  roster and flags players hidden from the tab list.

## THEMES

One color picker drives everything. Set `palette-color` and every accent, gradient and highlight
follows it live - no restart, no rebuilding the GUI.

Four looks, switchable in `ClickGUI -> Config -> GUI -> ui-mode`:

| Mode | Look |
|---|---|
| `Vanilla` | Addon draws no chrome at all. Plain Meteor and plain Minecraft. |
| `HVanilla` | Minecraft's square shapes kept, drawn as translucent panels with a thin accent outline. |
| `HModern1` | Flat panels, soft gradients, rounded corners. |
| `HModern2` | Default. Chamfered corners, thin accent outlines with a soft glow, corner brackets, pixel icons next to labels. |

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
- `Containers` Configurable backdrop behind inventories and chests (`container-background`).
- `Menu background` Animated gradient with accent particles instead of the panorama
  (`menu-background`).
- `Lag Notifier` Meteor's own lag-notifier element is restyled in the active theme (themed panel and
  accent), and shows how long the server has gone without responding in both seconds and
  milliseconds.
- `Chat` `[Hontun]` prefix in the accent color.

<img width="2559" height="1363" alt="obrazek" src="https://github.com/user-attachments/assets/5ad12503-3af1-4ab3-a05a-0b9107cdeaff" />
<img width="2559" height="1360" alt="obrazek" src="https://github.com/user-attachments/assets/6e69d76e-751c-4eda-b26e-8f26b6365862" />
<img width="2559" height="1362" alt="obrazek" src="https://github.com/user-attachments/assets/35a6967d-8d5e-4af9-89cd-1ef2f35851bc" />
<img width="2560" height="1361" alt="obrazek" src="https://github.com/user-attachments/assets/bf1c6a42-02de-40dd-abc1-ab4218b53d4b" />

## BUILDING

Needs JDK 25.

```
./gradlew build
```

The jar lands in `build/libs/`.

## CREDITS

- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- [Catppuccin Addon](https://github.com/X-C-0/catppuccin-addon)
- [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus)
- [DupersUnited](https://github.com/DupersUnited/dupersunited-mod)
- [Meteor Rejects](https://github.com/AntiCope/meteor-rejects)
- [Gurken's Gadgetry](https://github.com/stefexec/gurkens-gadgetry-public)
- [AntiP2W-Addon](https://github.com/AntiP2WDevelopment/AntiP2W-Addon)
