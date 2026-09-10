-- Atomic Distributed Token Bucket Rate Limiter Script
-- KEYS[1]: Rate limit key (e.g. "rl:app_123")
-- ARGV[1]: capacity (number)
-- ARGV[2]: refill_tokens (number)
-- ARGV[3]: refill_period_seconds (number)
-- ARGV[4]: requested_tokens (number)
-- ARGV[5]: current_timestamp_millis (number)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_tokens = tonumber(ARGV[2])
local refill_period_seconds = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])
local now = tonumber(ARGV[5])

local refill_rate = refill_tokens / refill_period_seconds

-- Fetch current bucket state (stored as hash: "tokens", "last_updated")
local bucket = redis.call('HMGET', key, 'tokens', 'last_updated')
local current_tokens = tonumber(bucket[1])
local last_updated = tonumber(bucket[2])

if current_tokens == nil or last_updated == nil then
    -- Bucket has never been used or expired; initialize with full capacity
    current_tokens = capacity
    last_updated = now
else
    -- Calculate refilled tokens based on elapsed time
    local elapsed_seconds = math.max(0, (now - last_updated) / 1000.0)
    local generated_tokens = elapsed_seconds * refill_rate
    current_tokens = math.min(capacity, current_tokens + generated_tokens)
    last_updated = now
end

local allowed = 0
local remaining = current_tokens
local retry_after = 0
local reset_seconds = math.ceil((capacity - current_tokens) / refill_rate)
if reset_seconds < 1 then
    reset_seconds = 1
end

if current_tokens >= requested then
    allowed = 1
    remaining = current_tokens - requested
    -- Save new state
    redis.call('HMSET', key, 'tokens', remaining, 'last_updated', now)
    -- Expire bucket after 2 periods of inactivity
    local ttl = math.max(60, math.ceil(refill_period_seconds * 2))
    redis.call('EXPIRE', key, ttl)
else
    allowed = 0
    remaining = current_tokens
    -- Calculate seconds to wait until enough tokens are refilled
    local needed = requested - current_tokens
    retry_after = math.max(1, math.ceil(needed / refill_rate))
    -- Save current calculated tokens so we don't recalculate from ancient last_updated
    redis.call('HMSET', key, 'tokens', current_tokens, 'last_updated', now)
    local ttl = math.max(60, math.ceil(refill_period_seconds * 2))
    redis.call('EXPIRE', key, ttl)
end

-- Return: [allowed (1 or 0), remaining_tokens (integer/float), reset_seconds, retry_after]
return { allowed, math.floor(remaining), reset_seconds, retry_after }
