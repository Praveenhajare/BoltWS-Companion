# BoltWS - bolt.jar class injector
# Replaces RunCommand*.class inside bolt.jar with patched versions.
# Called by repatch-bolt.bat

param(
    [Parameter(Mandatory=$true)][string]$BoltJar,
    [Parameter(Mandatory=$true)][string]$ClassesDir
)

$ErrorActionPreference = 'Stop'

$newClasses = Get-ChildItem -Path $ClassesDir -Filter 'RunCommand*.class'
if (-not $newClasses) {
    Write-Host "[inject] No RunCommand*.class found in $ClassesDir"
    exit 1
}

Add-Type -AssemblyName System.IO.Compression.FileSystem

$fs = [System.IO.File]::Open($BoltJar, 'Open', 'ReadWrite')
try {
    $zip = New-Object System.IO.Compression.ZipArchive($fs, 'Update')
    try {
        $pkgPath = 'com/techhamara/bolt/cli/commands/'

        # Remove existing RunCommand entries
        $removed = 0
        foreach ($e in @($zip.Entries)) {
            if ($e.FullName -like ($pkgPath + 'RunCommand*')) {
                $e.Delete()
                $removed++
            }
        }
        Write-Host "[inject] Removed $removed old RunCommand entries."

        # Add patched classes
        $added = 0
        foreach ($c in $newClasses) {
            $entryName = $pkgPath + $c.Name
            $newEntry = $zip.CreateEntry($entryName)
            $es = $newEntry.Open()
            $bytes = [System.IO.File]::ReadAllBytes($c.FullName)
            $es.Write($bytes, 0, $bytes.Length)
            $es.Close()
            $added++
        }
        Write-Host "[inject] Added $added patched RunCommand entries."
    } finally {
        $zip.Dispose()
    }
} finally {
    $fs.Close()
}

Write-Host "[inject] bolt.jar updated successfully."
exit 0
