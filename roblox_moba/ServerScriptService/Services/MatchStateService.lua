local ReplicatedStorage = game:GetService("ReplicatedStorage")
local GameConstants = require(ReplicatedStorage.Shared.GameConstants)
local Signal = require(ReplicatedStorage.Shared.Signal)

local MatchStateService = {}
MatchStateService.__index = MatchStateService

function MatchStateService.new()
    return setmetatable({
        Phase = GameConstants.MatchPhases.Lobby,
        Elapsed = 0,
        OnPhaseChanged = Signal.new(),
    }, MatchStateService)
end

function MatchStateService:Start()
    self:SetPhase(GameConstants.MatchPhases.MatchStart)
    task.delay(10, function() self:SetPhase(GameConstants.MatchPhases.LanePhase) end)
    task.delay(12 * 60, function() self:SetPhase(GameConstants.MatchPhases.MidGame) end)
end

function MatchStateService:SetPhase(phase)
    self.Phase = phase
    self.OnPhaseChanged:Fire(phase)
end

function MatchStateService:Tick(dt)
    self.Elapsed += dt
end

return MatchStateService
