Param()
$ErrorActionPreference = 'Stop'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$src = Join-Path $root 'src/main/java'
$build = Join-Path $root 'build'
$classes = Join-Path $build 'classes'
$jar = Join-Path $build 'infernovisuals-launcher.jar'
$dist = Join-Path $root 'dist'
$lib = Join-Path $root 'lib'
$gson = Join-Path $lib 'gson-2.11.0.jar'

New-Item -ItemType Directory -Force -Path $lib, $build, $classes, $dist | Out-Null

if (-not (Test-Path $gson)) {
  Write-Host 'Downloading gson...'
  $urls = @(
    'https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar',
    'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar'
  )

  foreach ($url in $urls) {
    try {
      Invoke-WebRequest -Uri $url -OutFile $gson
      break
    } catch {
      Write-Host "Failed from $url"
    }
  }

  if (-not (Test-Path $gson)) {
    throw 'Failed to download gson from all mirrors'
  }
}

Write-Host 'Compiling launcher sources...'
$sourcesFile = Join-Path $build 'sources.txt'
Get-ChildItem -Recurse -Path $src -Filter *.java | ForEach-Object { $_.FullName } | Set-Content -Encoding UTF8 $sourcesFile

& javac -encoding UTF-8 -cp $gson -d $classes "@$sourcesFile"
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

Write-Host 'Building launcher jar...'
& jar --create --file $jar --main-class com.infernovisuals.launcher.LauncherMain -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'jar failed' }

Write-Host 'Packaging .exe with jpackage...'
& jpackage --name InfernoVisualsLauncher --input $build --main-jar infernovisuals-launcher.jar --main-class com.infernovisuals.launcher.LauncherMain --type exe --win-console --dest $dist
if ($LASTEXITCODE -ne 0) { throw 'jpackage failed' }

Write-Host "Done. EXE in: $dist"
