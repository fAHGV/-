local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Players = game:GetService("Players")

local Remotes = ReplicatedStorage:WaitForChild("Remotes")
local playerGui = Players.LocalPlayer:WaitForChild("PlayerGui")

local hud = Instance.new("ScreenGui")
hud.Name = "MOBAHUD"
hud.ResetOnSpawn = false
hud.Parent = playerGui

local goldLabel = Instance.new("TextLabel")
goldLabel.Size = UDim2.fromOffset(220, 36)
goldLabel.Position = UDim2.fromScale(0.02, 0.92)
goldLabel.Parent = hud

Remotes.HUDSync.OnClientEvent:Connect(function(data)
    goldLabel.Text = string.format("Gold: %d  XP: %d", data.Gold or 0, data.XP or 0)
end)
