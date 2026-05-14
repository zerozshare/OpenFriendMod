# OpenFriend — Minecraft Java Edition Friends List bridge

> ## ⚠️ Unofficial — not affiliated with Microsoft, Mojang, or the Xbox brand
>
> OpenFriend is an **independent, community-built** project. It is **not** developed, endorsed, supported, sponsored, certified, or otherwise officially connected to Microsoft Corporation, Mojang AB, Mojang Studios, or the Xbox brand. "Minecraft", "Xbox", "Xbox Live", "Microsoft", and "Mojang" are trademarks of their respective owners. Use OpenFriend on accounts you control, on servers you operate or have permission to operate on. You assume all risk associated with running this software.

---

OpenFriend backports the **snapshot 26.2 Friends List** (search by gamertag, add / accept / decline, Xbox-Live-driven Join, in-game "Open to Friends") to older Minecraft versions on the client side — without touching the vanilla launcher.

| | |
|---|---|
| **Mod version** | 1.0.0 |
| **Supported MC** | 1.16.5 → 1.21.11 (30 Fabric builds — see table below) |
| **Loader** | Fabric (Forge / NeoForge planned for v1.0.1) |
| **License** | MIT (see [LICENSE](LICENSE)) |
| **Issues / feedback** | https://github.com/zerozshare/OpenFriendMC/issues |

---

## ⚠️ Test coverage disclaimer

Only a subset of these 30 Fabric builds have been **smoke-tested end-to-end** (sign-in → friend add → Join works in-game). The rest compile and load cleanly, share the same overlay group as a tested representative, and are *expected* to work — but I haven't been able to verify each individually.

**If something is broken on your MC version, please open a GitHub Issue:**
**https://github.com/zerozshare/OpenFriendMC/issues**

Include:
- Your MC version
- The full launcher log (especially lines containing `OpenFriend` or `core:`)
- What you tried to do and what happened

I read every report and turn around fixes quickly.

| Confirmed working in-game | Built but not personally tested |
|---|---|
| 1.16.5, 1.20.4, 1.20.6, 1.21.1, 1.21.4, 1.21.6 | everything else (most likely fine — same overlay code) |

---

## Quick start

1. Install **Fabric Loader** (`fabricloader >= 0.16.0`, or `>= 0.11.0` for 1.16.5, `>= 0.12.0` for 1.17.x).
2. Download the jar matching your MC version from this release.
3. Drop it into your `mods/` folder.
4. Launch Minecraft. A **Friends** button appears on the title screen, pause screen, and multiplayer screen.
5. First launch shows a device-code sign-in screen — visit the URL, enter the code, sign in with the Microsoft account you use for Minecraft.
6. The OpenFriend overlay lets you search by gamertag, send / accept friend requests, and Join friends who are hosting.

No Fabric API dependency. The bundled Core binary (Windows / macOS / Linux × amd64 + arm64) auto-extracts to your platform's data directory on first launch.

## Version support matrix

| MC version | Jar | Overlay group | Java floor | Notes |
|---|---|---|---|---|
| 1.21.11 | `OpenFriend-fabric-1.21.11.jar` | group-d4 | 21 | new `AbstractButton.renderContents` API; `Identifier` rename |
| 1.21.10 | `OpenFriend-fabric-1.21.10.jar` | group-d3 | 21 | `KeyEvent` / `CharacterEvent` input |
| 1.21.9  | `OpenFriend-fabric-1.21.9.jar`  | group-d3 | 21 | |
| 1.21.8  | `OpenFriend-fabric-1.21.8.jar`  | group-d2 | 21 | `Matrix3x2fStack` poses |
| 1.21.7  | `OpenFriend-fabric-1.21.7.jar`  | group-d2 | 21 | |
| 1.21.6  | `OpenFriend-fabric-1.21.6.jar`  | group-d2 | 21 | |
| 1.21.5  | `OpenFriend-fabric-1.21.5.jar`  | group-d  | 21 | |
| 1.21.4  | `OpenFriend-fabric-1.21.4.jar`  | group-c  | 21 | |
| 1.21.3  | `OpenFriend-fabric-1.21.3.jar`  | group-c  | 21 | |
| 1.21.2  | `OpenFriend-fabric-1.21.2.jar`  | group-c  | 21 | |
| 1.21.1  | `OpenFriend-fabric-1.21.1.jar`  | group-c2 | 21 | |
| 1.21    | `OpenFriend-fabric-1.21.jar`    | group-c2 | 21 | |
| 1.20.6  | `OpenFriend-fabric-1.20.6.jar`  | group-c1c | 21 | |
| 1.20.5  | `OpenFriend-fabric-1.20.5.jar`  | group-c1c | 21 | |
| 1.20.4  | `OpenFriend-fabric-1.20.4.jar`  | group-c1b | 17 | **primary tested target** |
| 1.20.3  | `OpenFriend-fabric-1.20.3.jar`  | group-c1b | 17 | |
| 1.20.2  | `OpenFriend-fabric-1.20.2.jar`  | group-c1b | 17 | |
| 1.20.1  | `OpenFriend-fabric-1.20.1.jar`  | group-c1  | 17 | |
| 1.20    | `OpenFriend-fabric-1.20.jar`    | group-c1  | 17 | |
| 1.19.4  | `OpenFriend-fabric-1.19.4.jar`  | group-b   | 17 | PoseStack era |
| 1.19.3  | `OpenFriend-fabric-1.19.3.jar`  | group-b3  | 17 | |
| 1.19.2  | `OpenFriend-fabric-1.19.2.jar`  | group-b0  | 17 | |
| 1.19.1  | `OpenFriend-fabric-1.19.1.jar`  | group-b0  | 17 | |
| 1.19    | `OpenFriend-fabric-1.19.jar`    | group-b0  | 17 | |
| 1.18.2  | `OpenFriend-fabric-1.18.2.jar`  | group-b1  | 17 | `TextComponent` instead of `Component.literal` |
| 1.18.1  | `OpenFriend-fabric-1.18.1.jar`  | group-b1  | 17 | |
| 1.18    | `OpenFriend-fabric-1.18.jar`    | group-b1  | 17 | |
| 1.17.1  | `OpenFriend-fabric-1.17.1.jar`  | group-a2  | 16 | no `isHoveredOrFocused()` |
| 1.17    | `OpenFriend-fabric-1.17.jar`    | group-a2  | 16 | |
| 1.16.5  | `OpenFriend-fabric-1.16.5.jar`  | group-a   | 8  | log4j logger, `ConnectScreen` 3-arg, `TextureManager.bind` |

## Known limitations on legacy versions (1.16.5 – 1.19.x)

- **No real player skin heads** — currently shows a hashed-color placeholder. Skin lookup uses a Mojang API path that we haven't wired up for the PoseStack-era versions yet.
- **No auto sign-in from the launcher session** — legacy `User.getAccessToken()` returns an old-style Mojang yggdrasil token that doesn't pass MSA → XSTS handshake. Device-code is the working sign-in path; the Core remembers the refresh token, so it's a one-time setup.
- **Toast click-to-expand only inside the OpenFriend overlay** on 1.16.5 — the global `Screen.mouseClicked` mixin can't bind because 1.16.5's `Screen` doesn't override the method (it's a default interface method on `GuiEventListener`).

All three planned for v1.0.1.

## Source code

This release ships only the **1.16.5 source tree** under [`src-1.16.5/`](src-1.16.5/). It's the most complex variant (legacy PoseStack, log4j, public AbstractWidget fields, old `Button` constructor, no `isHoveredOrFocused()` / `Tooltip` / `getProfileId()` / `Component.literal` / `JsonParser.parseString` / `addRenderableWidget`) — so if you're reading the code to understand how the per-version overlays work or to send a fix, this is the most informative starting point.

The full multi-version build infrastructure (all 30 group overlays, build orchestrator, mixins refmap pipeline) lives in the private development tree. If you need source for a specific newer MC version to fix something, **open an Issue** with which version and what you want to change — I can either share that overlay or fold the fix in directly.

Build the 1.16.5 source:

```bash
cd src-1.16.5

# (optional) drop pre-built Core binaries into helper/src/main/resources/openfriend/
# from https://github.com/zerozshare/OpenFriendCore/releases — needed only to
# produce a runnable jar; source-only reading doesn't need them.

./gradlew :fabric:remapJar
ls fabric/build/libs/
```

## Components

OpenFriend is split into independently-shippable pieces, glued by JSON-RPC stdio IPC:

| Component | What it does | Repository |
|---|---|---|
| **OpenFriend Mod** *(this)* | Fabric client mod — UI, MC session handoff, in-game "Open to Friends", Join | this release |
| **OpenFriend Core** | Go binary — MSA / Xbox Live auth, Mojang Friends signaling, WebRTC bridge | [zerozshare/OpenFriendCore](https://github.com/zerozshare/OpenFriendCore) |
| **OpenFriend Bypass** | Paper plugin — optional online-mode bypass for the destination server | [zerozshare/OpenFriendBypass](https://github.com/zerozshare/OpenFriendBypass) |
| **OpenMix** | Standalone UI toolkit (UButton, UPanel, UScrollPane, UTabBar, UInput, UTheme, UDivider, URenderer, UComponent) extracted from this mod | [zerozshare/OpenMix](https://github.com/zerozshare/OpenMix) |

The bundled Core binary inside each jar auto-launches on first use and IPCs with the mod via stdin/stdout JSON-RPC. No extra setup required to *use* the mod.

**OpenMix** is the renderer-agnostic UI layer this mod is built on. It's published as a separate library so other Minecraft mods (and non-Minecraft projects) can reuse it without pulling in the Friends-List bridge. See [zerozshare/OpenMix](https://github.com/zerozshare/OpenMix).

## Roadmap — v1.0.1

- Forge + NeoForge builds (rework `common-mc/build.gradle.kts` for Architectury multiloader)
- Real player skin heads on 1.16.5 – 1.19.x (Mojang sessionserver fetch + texture cache)
- Auto sign-in on legacy (extract MSA token via launcher profile, skip device-code when possible)
- 26.1 / 26.1.1 / 26.1.2 Fabric builds (blocked on Architectury Loom stable supporting Java 25)
- Toast click on title / pause screen for 1.16.5

26.2+ ships the Friends List natively — OpenFriend is not needed on those versions.

## Feedback

Bugs, missing features, version-specific weirdness — please open a GitHub Issue:
**https://github.com/zerozshare/OpenFriendMC/issues**

Pull requests welcome on the 1.16.5 source tree included in this release.

## License

MIT. See [LICENSE](LICENSE). Third-party trademarks remain the property of their respective owners.
