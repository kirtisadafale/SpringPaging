-- Args:
-- KEYS[1] = bucket key
-- ARGV[1] = now_millis
-- ARGV[2] = capacity
-- ARGV[3] = refill_per_millis
-- ARGV[4] = tokens_required

local key = KEYS[1]
local now = tonumber(ARGV[1])
-- If the caller didn't provide a timestamp, fall back to Redis server time (seconds, microseconds)
if now == nil then
    local t = redis.call('TIME')
    -- t[1] = seconds, t[2] = microseconds
    now = tonumber(t[1]) * 1000 + math.floor(tonumber(t[2]) / 1000)
end
local capacity = tonumber(ARGV[2]) or 0
local refill = tonumber(ARGV[3]) or 0
local need = tonumber(ARGV[4]) or 1

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

-- Return stringified values so the Java Redis client decoders (ValueOutput) can handle them
return { tostring(allowed), tostring(tokens) }
