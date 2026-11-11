param(
    [Parameter(Mandatory)][string]$AndroidJar,
    [Parameter(Mandatory)][string]$Aapt2,
    [Parameter(Mandatory)][string]$R8Jar,
    [Parameter(Mandatory)][string]$LibVlcAar
)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
function Invoke-Checked([string]$Executable, [string[]]$Arguments) {
    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Executable failed ($LASTEXITCODE)" }
}
$build = Join-Path $PSScriptRoot 'build'
New-Item -ItemType Directory -Force "$build/vlc-classes" | Out-Null
Invoke-Checked python @('tools/package.py','extract',$LibVlcAar,$build)
Invoke-Checked javac @('-encoding','UTF-8','--release','8','-cp',"$AndroidJar;$build/vlc-original.jar",'-d',"$build/vlc-classes",'third-party/libvlc/AWindow.java')
Invoke-Checked python @('third-party/libvlc/patch-jar.py',"$build/vlc-original.jar","$build/vlc-classes","$build/vlc.jar")
foreach ($module in @('live-tv','cinema','fire-volume')) {
    $dir = Join-Path $build $module
    $classes = Join-Path $dir ([guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Force $classes | Out-Null
    $sources = @(Get-ChildItem "$module/src" -Recurse -Filter '*.java' | ForEach-Object FullName)
    $classpath = $AndroidJar
    $inputs = @("$dir/classes.jar")
    if ($module -ne 'fire-volume') {
        $classpath += ";$build/vlc.jar"
        $inputs += "$build/vlc.jar"
    }
    Invoke-Checked javac (@('-encoding','UTF-8','--release','8','-cp',$classpath,'-d',$classes) + $sources)
    Invoke-Checked jar @('cf',"$dir/classes.jar",'-C',$classes,'.')
    $dex = Join-Path $classes 'dex'
    New-Item -ItemType Directory $dex | Out-Null
    Invoke-Checked java (@('-cp',$R8Jar,'com.android.tools.r8.D8','--lib',$AndroidJar,'--min-api','26','--output',$dex) + $inputs)
    $link = @('link','-I',$AndroidJar,'--manifest',"$module/AndroidManifest.xml",'-o',"$dir/resources.apk")
    if (Test-Path "$module/res") {
        Invoke-Checked $Aapt2 @('compile','--dir',"$module/res",'-o',"$dir/res.zip")
        $link += "$dir/res.zip"
    }
    Invoke-Checked $Aapt2 $link
    Invoke-Checked python @('tools/package.py','assemble',"$dir/resources.apk",$dex,$LibVlcAar,$module,"$build/$module-unsigned.apk")
}
