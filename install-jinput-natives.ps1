# PowerShell Script to Download and Install JInput Native Libraries
# Run this script to fix controller detection issues

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  JInput Native Library Installer" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Create native directory if it doesn't exist
$nativeDir = "native"
if (-not (Test-Path $nativeDir)) {
    Write-Host "Creating native directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $nativeDir | Out-Null
}

# Download URL for JInput native libraries
$jinputVersion = "2.0.9"
$downloadUrl = "https://repo1.maven.org/maven2/net/java/jinput/jinput-platform/$jinputVersion/jinput-platform-$jinputVersion-natives-windows.jar"
$downloadPath = "jinput-natives-temp.jar"

Write-Host "Downloading JInput native libraries..." -ForegroundColor Yellow
Write-Host "URL: $downloadUrl" -ForegroundColor Gray

try {
    # Download the jar file
    Invoke-WebRequest -Uri $downloadUrl -OutFile $downloadPath -UseBasicParsing
    Write-Host "✓ Downloaded successfully" -ForegroundColor Green
}
catch {
    Write-Host "✗ Download failed: $_" -ForegroundColor Red
    exit 1
}

# Extract DLLs from the jar
Write-Host ""
Write-Host "Extracting native DLLs..." -ForegroundColor Yellow

# JAR files are ZIP files
Add-Type -Assembly System.IO.Compression.FileSystem

try {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($downloadPath)

    $extracted = 0
    foreach ($entry in $zip.Entries) {
        if ($entry.Name -like "*.dll") {
            $destPath = Join-Path $nativeDir $entry.Name
            Write-Host "  Extracting: $($entry.Name)" -ForegroundColor Gray

            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $destPath, $true)
            $extracted++
        }
    }

    $zip.Dispose()

    if ($extracted -gt 0) {
        Write-Host "✓ Extracted $extracted DLL files" -ForegroundColor Green
    }
    else {
        Write-Host "✗ No DLL files found in archive" -ForegroundColor Red
    }
}
catch {
    Write-Host "✗ Extraction failed: $_" -ForegroundColor Red
    exit 1
}

# Clean up temporary file
Write-Host ""
Write-Host "Cleaning up..." -ForegroundColor Yellow
Remove-Item $downloadPath -Force
Write-Host "✓ Cleaned up temporary files" -ForegroundColor Green

# Verify installation
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Verification" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

$dllFiles = Get-ChildItem -Path $nativeDir -Filter "*.dll"

if ($dllFiles.Count -gt 0) {
    Write-Host "✓ Installation successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Installed DLLs:" -ForegroundColor Yellow
    foreach ($dll in $dllFiles) {
        Write-Host "  - $($dll.Name)" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "  Next Steps" -ForegroundColor Cyan
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "1. Restart your application: npm run dev" -ForegroundColor Yellow
    Write-Host "2. Plug in your controller via USB" -ForegroundColor Yellow
    Write-Host "3. Click the Refresh button in the Controllers panel" -ForegroundColor Yellow
    Write-Host "4. Your controllers should now appear!" -ForegroundColor Yellow
}
else {
    Write-Host "✗ No DLL files found after installation" -ForegroundColor Red
    Write-Host "Please try manual installation - see JINPUT_FIX.md" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Installation complete!" -ForegroundColor Cyan
