local TowerService = {}
TowerService.__index = TowerService

function TowerService.new(projectileService, damageService)
    return setmetatable({ _projectiles = projectileService, _damage = damageService }, TowerService)
end

function TowerService:TickTower(tower, enemies)
    local target = enemies.Heroes[1] or enemies.Creeps[1]
    if not target then return end
    self._projectiles:SpawnProjectile({
        Origin = tower.Position,
        Target = target,
        Speed = 160,
        Damage = tower.TierDamage,
        OnHit = function(v) self._damage:ApplyDamage(tower, v, "Physical", tower.TierDamage) end,
    })
end

return TowerService
