# Hontun theme screenshotter.
# Captures ONLY the Minecraft client area (no window title bar, no borders, no taskbar)
# for every theme x screen, and saves each into .\screenshots next to this script.
#
# It does NOT touch the addon. Minecraft does not have to be running to keep this file around,
# but it must be running (and visible / in front) while you actually capture.
#
# Usage:
#   1. Start Minecraft with Hontun.
#   2. Run:  powershell -ExecutionPolicy Bypass -File screenshot-themes.ps1
#   3. Follow the prompts. For each entry: get MC into that state, then press F9 to capture.
#      F10 = skip current entry, F8 = quit early.
#
# Why guided and not fully blind-auto: switching the ui-mode lives in Meteor's ClickGUI and the
# addon has no external hook or keybind for it (you asked not to add anything to the addon), so a
# script clicking blind into the ClickGUI dropdown would be unreliable. This drives the tedious
# part for you (exact client-area crop, naming, folder) while you just navigate and tap F9.

Add-Type @"
using System;
using System.Runtime.InteropServices;
using System.Text;
public class Win {
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int L, T, R, B; }
    [StructLayout(LayoutKind.Sequential)] public struct POINT { public int X, Y; }
    public delegate bool EnumProc(IntPtr h, IntPtr l);
    [DllImport("user32.dll")] public static extern bool EnumWindows(EnumProc f, IntPtr l);
    [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr h);
    [DllImport("user32.dll")] public static extern int GetClassName(IntPtr h, StringBuilder s, int n);
    [DllImport("user32.dll")] public static extern bool GetClientRect(IntPtr h, out RECT r);
    [DllImport("user32.dll")] public static extern bool ClientToScreen(IntPtr h, ref POINT p);
    [DllImport("user32.dll")] public static extern short GetAsyncKeyState(int k);
    [DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
}
"@
Add-Type -AssemblyName System.Drawing

[void][Win]::SetProcessDPIAware()

$THEMES  = @('Vanilla', 'HVanilla', 'HModern1', 'HModern2')
$SCREENS = @(
    @{ key = 'main';        hint = 'Title screen (the main menu with the Hontun logo)' },
    @{ key = 'multiplayer'; hint = 'Multiplayer server list' },
    @{ key = 'accounts';    hint = 'Accounts screen (Accounts button on the multiplayer screen)' },
    @{ key = 'versions';    hint = 'Versions screen (Version button on the multiplayer screen)' },
    @{ key = 'proxies';     hint = 'Proxies screen (Proxies button on the multiplayer screen)' }
)

$VK_F9 = 0x78; $VK_F10 = 0x79; $VK_F8 = 0x77

function Find-McWindow {
    $result = [IntPtr]::Zero
    $cb = [Win+EnumProc] {
        param($h, $l)
        if ([Win]::IsWindowVisible($h)) {
            $cn = New-Object System.Text.StringBuilder 256
            [void][Win]::GetClassName($h, $cn, 256)
            if ($cn.ToString() -eq 'GLFW30') { $script:result = $h; return $false }
        }
        return $true
    }
    [void][Win]::EnumWindows($cb, [IntPtr]::Zero)
    return $result
}

function Wait-Trigger {
    while ($true) {
        if (([Win]::GetAsyncKeyState($VK_F8) -band 0x8000) -ne 0) { return 'quit' }
        if (([Win]::GetAsyncKeyState($VK_F10) -band 0x8000) -ne 0) { Start-Sleep -Milliseconds 250; return 'skip' }
        if (([Win]::GetAsyncKeyState($VK_F9) -band 0x8000) -ne 0) { Start-Sleep -Milliseconds 250; return 'shoot' }
        Start-Sleep -Milliseconds 40
    }
}

function Capture-Client($hwnd, $path) {
    $r = New-Object Win+RECT
    if (-not [Win]::GetClientRect($hwnd, [ref]$r)) { return $false }
    $w = $r.R - $r.L; $h = $r.B - $r.T
    if ($w -le 0 -or $h -le 0) { return $false }
    $p = New-Object Win+POINT; $p.X = 0; $p.Y = 0
    [void][Win]::ClientToScreen($hwnd, [ref]$p)
    $bmp = New-Object System.Drawing.Bitmap $w, $h
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($p.X, $p.Y, 0, 0, (New-Object System.Drawing.Size $w, $h))
    $g.Dispose()
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    return $true
}

$outDir = Join-Path $PSScriptRoot 'screenshots'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$hwnd = Find-McWindow
if ($hwnd -eq [IntPtr]::Zero) {
    Write-Host 'Minecraft window (GLFW30) not found. Start Minecraft first, then run this again.' -ForegroundColor Red
    exit 1
}
Write-Host "Found Minecraft window. Saving into: $outDir" -ForegroundColor Green
Write-Host 'For each entry: get MC into that state, then press F9. (F10 = skip, F8 = quit)' -ForegroundColor Cyan
Write-Host ''

$done = 0
foreach ($theme in $THEMES) {
    Write-Host "==== THEME: $theme ====" -ForegroundColor Yellow
    Write-Host "   Switch ui-mode to '$theme' in ClickGUI -> Config -> GUI -> ui-mode." -ForegroundColor Yellow
    foreach ($s in $SCREENS) {
        $name = "{0}_{1}.png" -f $theme, $s.key
        Write-Host ("   [{0}] {1}  ->  press F9" -f $s.key, $s.hint)
        $t = Wait-Trigger
        if ($t -eq 'quit') { Write-Host 'Stopped.' -ForegroundColor Red; Write-Host "$done screenshot(s) saved in $outDir"; exit 0 }
        if ($t -eq 'skip') { Write-Host '      skipped'; continue }
        $hwnd = Find-McWindow
        $path = Join-Path $outDir $name
        if (Capture-Client $hwnd $path) { $done++; Write-Host "      saved $name" -ForegroundColor Green }
        else { Write-Host '      capture failed (is the window minimized?)' -ForegroundColor Red }
    }
    Write-Host ''
}
Write-Host "Done. $done screenshot(s) in $outDir" -ForegroundColor Green
