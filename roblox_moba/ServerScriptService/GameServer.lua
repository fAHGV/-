local ReplicatedStorage = game:GetService("ReplicatedStorage")
local RunService = game:GetService("RunService")

local MatchStateService = require(script.Services.MatchStateService)
local AbilityService = require(script.Services.AbilityService)
local CreepWaveService = require(script.Services.CreepWaveService)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local function buildDeps()
    local deps = {}
    function deps:BuildAbilityContext(caster, heroState, payload)
        return {
            Caster = caster,
            Origin = payload.Origin,
            TargetPosition = payload.TargetPosition,
            Scaling = heroState.Scaling,
            ProjectileService = _G.ProjectileService,
            DamageService = _G.DamageService,
            MovementService = _G.MovementService,
            QueryService = _G.QueryService,
            CrowdControlService = _G.CrowdControlService,
        }
    end
    return deps
end

local match = MatchStateService.new()
local ability = AbilityService.new(buildDeps())
local creeps = CreepWaveService.new(_G.CreepAIService)

match:Start()
creeps:Start()

Remotes.AbilityRequest.OnServerEvent:Connect(function(player, payload)
    local heroState = _G.HeroService:GetState(player)
    if not heroState then return end
    ability:Cast(player, heroState, payload.AbilityId, payload)
end)

RunService.Heartbeat:Connect(function(dt)
    match:Tick(dt)
end)
