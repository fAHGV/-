local ReplicatedStorage = game:GetService("ReplicatedStorage")

local remoteNames = { "AbilityRequest", "MovementRequest", "PurchaseItemRequest", "HUDSync", "KillFeedEvent", "MatchStateEvent" }
local folder = ReplicatedStorage:FindFirstChild("Remotes") or Instance.new("Folder")
folder.Name = "Remotes"
folder.Parent = ReplicatedStorage

for _, name in ipairs(remoteNames) do
    if not folder:FindFirstChild(name) then
        local ev = Instance.new("RemoteEvent")
        ev.Name = name
        ev.Parent = folder
    end
end
