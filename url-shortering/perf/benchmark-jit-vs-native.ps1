param(
    [string]$JitBaseUrl = "http://api:8080",
    [string]$NativeBaseUrl = "http://api-native:8080",
    [string]$ComposeNetwork = "url-shortering_backend",
    [bool]$UseComposeNetwork = $true,
    [int]$Rate = 50,
    [string]$Duration = "3m",
    [int]$PreAllocatedVus = 50,
    [int]$MaxVus = 300,
    [int]$P95Ms = 400,
    [int]$P99Ms = 800,
    [double]$ErrorRate = 0.01
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outDir = Join-Path $scriptRoot "out"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

if ($UseComposeNetwork) {
    & docker network inspect $ComposeNetwork *> $null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker network '$ComposeNetwork' was not found. Start compose services first or pass -UseComposeNetwork:\$false."
    }
}

function Invoke-K6Scenario {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$BaseUrl
    )

    $summaryFile = Join-Path $outDir ("summary-" + $Name + ".json")
    $k6Mount = (Join-Path $scriptRoot "k6") + ":/scripts:ro"
    $outMount = $outDir + ":/out"

    Write-Host ""
    Write-Host "Running benchmark for $Name ($BaseUrl)..."

    $dockerArgs = @(
        "run", "--rm",
        "-e", "BASE_URL=$BaseUrl",
        "-e", "RATE=$Rate",
        "-e", "DURATION=$Duration",
        "-e", "PRE_ALLOCATED_VUS=$PreAllocatedVus",
        "-e", "MAX_VUS=$MaxVus",
        "-e", "P95_MS=$P95Ms",
        "-e", "P99_MS=$P99Ms",
        "-e", "ERROR_RATE=$ErrorRate",
        "-v", "$k6Mount",
        "-v", "$outMount"
    )
    if ($UseComposeNetwork) {
        $dockerArgs += @("--network", $ComposeNetwork)
    }
    $dockerArgs += @(
        "grafana/k6", "run", "/scripts/load-api.js", "--summary-export", "/out/summary-$Name.json"
    )

    & docker @dockerArgs | Out-Host

    if (-not (Test-Path $summaryFile)) {
        throw "k6 did not generate summary file for $Name."
    }

    $summary = Get-Content -Raw $summaryFile | ConvertFrom-Json

    function Get-Metric {
        param(
            [Parameter(Mandatory = $true)]$MetricNode,
            [Parameter(Mandatory = $true)][string]$PrimaryKey,
            [string]$FallbackKey = ""
        )

        if ($null -eq $MetricNode) { return 0.0 }
        if ($MetricNode.PSObject.Properties.Name -contains "values") {
            if ($MetricNode.values.PSObject.Properties.Name -contains $PrimaryKey) {
                return [double]$MetricNode.values.$PrimaryKey
            }
            if ($FallbackKey -and ($MetricNode.values.PSObject.Properties.Name -contains $FallbackKey)) {
                return [double]$MetricNode.values.$FallbackKey
            }
        }
        if ($MetricNode.PSObject.Properties.Name -contains $PrimaryKey) {
            return [double]$MetricNode.$PrimaryKey
        }
        if ($FallbackKey -and ($MetricNode.PSObject.Properties.Name -contains $FallbackKey)) {
            return [double]$MetricNode.$FallbackKey
        }
        return 0.0
    }

    [PSCustomObject]@{
        Name = $Name
        Rps = Get-Metric -MetricNode $summary.metrics.http_reqs -PrimaryKey "rate"
        P95 = Get-Metric -MetricNode $summary.metrics.http_req_duration -PrimaryKey "p(95)"
        P99 = Get-Metric -MetricNode $summary.metrics.http_req_duration -PrimaryKey "p(99)"
        ErrorRate = Get-Metric -MetricNode $summary.metrics.http_req_failed -PrimaryKey "rate" -FallbackKey "value"
        Checks = Get-Metric -MetricNode $summary.metrics.checks -PrimaryKey "rate" -FallbackKey "value"
        SummaryPath = $summaryFile
    }
}

function Format-Score {
    param([double]$Value)
    return [Math]::Round($Value, 2)
}

$jit = Invoke-K6Scenario -Name "jit" -BaseUrl $JitBaseUrl
$native = Invoke-K6Scenario -Name "native" -BaseUrl $NativeBaseUrl

Write-Host ""
Write-Host "Benchmark summary:"
$table = @($jit, $native) | Select-Object `
    Name, `
    @{Name = "RPS"; Expression = { Format-Score $_.Rps }}, `
    @{Name = "P95(ms)"; Expression = { Format-Score $_.P95 }}, `
    @{Name = "P99(ms)"; Expression = { Format-Score $_.P99 }}, `
    @{Name = "ErrorRate"; Expression = { Format-Score ($_.ErrorRate * 100) }}, `
    @{Name = "Checks"; Expression = { Format-Score ($_.Checks * 100) }}
$table | Format-Table -AutoSize

$jitWins = 0
$nativeWins = 0
if ($jit.Rps -gt $native.Rps) { $jitWins++ } else { $nativeWins++ }
if ($jit.P95 -lt $native.P95) { $jitWins++ } else { $nativeWins++ }
if ($jit.P99 -lt $native.P99) { $jitWins++ } else { $nativeWins++ }
if ($jit.ErrorRate -lt $native.ErrorRate) { $jitWins++ } else { $nativeWins++ }

Write-Host ""
if ($jitWins -ge $nativeWins) {
    Write-Host "Recommendation: JIT profile performed better or equivalent for this workload."
} else {
    Write-Host "Recommendation: Native profile performed better for this workload."
}

Write-Host "Summary files:"
Write-Host " - $($jit.SummaryPath)"
Write-Host " - $($native.SummaryPath)"
