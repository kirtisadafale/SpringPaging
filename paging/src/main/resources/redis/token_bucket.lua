-- Args:
-- KEYS[1] = bucket key
-- ARGV[1] = now_millis
-- ARGV[2] = capacity
-- ARGV[3] = refill_per_millis
-- ARGV[4] = tokens_required

local key = KEYS[1]
local now = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local refill = tonumber(ARGV[3])
local need = tonumber(ARGV[4])

local data = redis.call('HMGET', key, 'tokens', 'last')
local tokens = tonumber(data[1])
local last = tonumber(data[2])
if tokens == nil then tokens = capacity end
if last == nil then last = now end

local delta = math.max(0, now - last)
local added = delta * refill
tokens = math.min(capacity, tokens + added)
local allowed = 0
if tokens >= need then
    tokens = tokens - need
    allowed = 1
end

redis.call('HMSET', key, 'tokens', tokens, 'last', now)
redis.call('PEXPIRE', key, 86400000)

return { allowed, tokens }
