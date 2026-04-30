local ReplicatedStorage = game:GetService("ReplicatedStorage")
local GameConstants = require(ReplicatedStorage.Shared.GameConstants)

local EconomyService = {}
EconomyService.__index = EconomyService

function EconomyService.new()
    return setmetatable({ _gold = {}, _xp = {} }, EconomyService)
end

function EconomyService:AddLastHit(player, unitType, elapsedSeconds)
    local minutes = elapsedSeconds / 60
    local scale = 1 + (GameConstants.Economy.CreepBountyScalePerMinute * minutes)
    local bounty = math.floor(GameConstants.Economy.BaseCreepGold * scale)
    self._gold[player] = (self._gold[player] or 0) + bounty
    self._xp[player] = (self._xp[player] or 0) + (unitType == "Hero" and 250 or 60)
    return bounty
end

return EconomyService
