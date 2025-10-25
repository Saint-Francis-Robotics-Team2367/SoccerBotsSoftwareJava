Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  JInput Native Library Installer" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Create native directory
$nativeDir = "native"
if (-not (Test-Path $nativeDir)) {
    Write-Host "Creating native directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $nativeDir | Out-Null
}

# Download URL
$downloadUrl = "https://repo1.maven.org/maven2/net/java/jinput/jinput-platform/2.0.9/jinput-platform-2.0.9-natives-windows.jar"
$downloadPath = "jinput-natives-temp.jar"

Write-Host "Downloading JInput native libraries..." -ForegroundColor Yellow
Invoke-WebRequest -Uri $downloadUrl -OutFile $downloadPath -UseBasicParsing
Write-Host "Downloaded successfully" -ForegroundColor Green

# Extract DLLs
Write-Host ""
Write-Host "Extracting native DLLs..." -ForegroundColor Yellow
Add-Type -Assembly System.IO.Compression.FileSystem

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
Write-Host "Extracted $extracted DLL files" -ForegroundColor Green

# Clean up
Write-Host ""
Write-Host "Cleaning up..." -ForegroundColor Yellow
Remove-Item $downloadPath -Force
Write-Host "Cleaned up temporary files" -ForegroundColor Green

# Verify
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Installation Complete!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

$dllFiles = Get-ChildItem -Path $nativeDir -Filter "*.dll"
Write-Host ""
Write-Host "Installed DLLs:" -ForegroundColor Yellow
foreach ($dll in $dllFiles) {
    Write-Host "  - $($dll.Name)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Restart your application: npm run dev" -ForegroundColor Gray
Write-Host "2. Plug in your controller via USB" -ForegroundColor Gray
Write-Host "3. Click Refresh in the Controllers panel" -ForegroundColor Gray
Write-Host ""
