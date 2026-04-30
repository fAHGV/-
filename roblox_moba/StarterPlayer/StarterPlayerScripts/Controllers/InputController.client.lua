local UserInputService = game:GetService("UserInputService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local keyToSlot = {
    [Enum.KeyCode.Q] = "Q",
    [Enum.KeyCode.W] = "W",
    [Enum.KeyCode.E] = "E",
    [Enum.KeyCode.R] = "R",
}

UserInputService.InputBegan:Connect(function(input, gp)
    if gp then return end
    local slot = keyToSlot[input.KeyCode]
    if not slot then return end
    Remotes.AbilityRequest:FireServer({
        Slot = slot,
        AbilityId = _G.LocalHeroAbilities[slot],
        Origin = _G.GetHeroOrigin(),
        TargetPosition = _G.GetMouseWorldPosition(),
    })
end)
