local ReplicatedStorage = game:GetService("ReplicatedStorage")
local GameConstants = require(ReplicatedStorage.Shared.GameConstants)

local CreepWaveService = {}
CreepWaveService.__index = CreepWaveService

function CreepWaveService.new(aiService)
    return setmetatable({ _ai = aiService, _wave = 0 }, CreepWaveService)
end

function CreepWaveService:Start()
    task.spawn(function()
        while true do
            task.wait(GameConstants.Map.WaveInterval)
            self._wave += 1
            for _, lane in ipairs(GameConstants.Map.Lanes) do
                self:SpawnLaneWave(lane)
            end
        end
    end)
end

function CreepWaveService:SpawnLaneWave(lane)
    local units = { "Melee", "Melee", "Melee", "Ranged" }
    if self._wave % 3 == 0 then table.insert(units, "Siege") end
    self._ai:CreateLaneWave(lane, units)
end

return CreepWaveService
