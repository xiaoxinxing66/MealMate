local key = KEYS[1];
local threadId = ARGV[1];

if (redis.call('HEXISTS', key, threadId) == 0) then
    return nil;
end;
local count = redis.call('HINCRBY', key, threadId, -1);

if (count == 0) then
    redis.call('DEL', key);
    return nil;
end;