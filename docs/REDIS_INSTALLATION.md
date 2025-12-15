# 🚀 Redis Installation Guide

## For macOS (Your System)

### Step 1: Install Redis

```bash
brew install redis
```

**Expected Output:**
```
==> Downloading redis...
==> Installing redis...
✅ redis was successfully installed!
```

### Step 2: Start Redis

```bash
brew services start redis
```

**Expected Output:**
```
==> Successfully started `redis` (label: homebrew.mxcl.redis)
```

### Step 3: Verify Redis is Running

```bash
redis-cli ping
```

**Expected Output:**
```
PONG
```

✅ **If you see "PONG", Redis is working!**

---

## Alternative: Run Redis Without Service

If you just want to test without installing as a service:

```bash
# Start Redis in the current terminal
redis-server
```

Keep this terminal open while testing the app.

---

## For Linux (Ubuntu/Debian)

```bash
# Install Redis
sudo apt update
sudo apt install redis-server

# Start Redis
sudo systemctl start redis-server

# Enable on boot
sudo systemctl enable redis-server

# Verify
redis-cli ping
```

---

## For Windows

**Option 1: Use Docker**
```bash
docker run -d -p 6379:6379 redis:latest
```

**Option 2: Download from GitHub**
1. Go to: https://github.com/microsoftarchive/redis/releases
2. Download the latest `.msi` file
3. Install and run `redis-server.exe`

---

## 🧪 Test Redis After Installation

Once Redis is running, test it:

```bash
# Connect to Redis CLI
redis-cli

# Try some commands
127.0.0.1:6379> SET test "Hello Redis"
127.0.0.1:6379> GET test
# Should return: "Hello Redis"

127.0.0.1:6379> exit
```

---

## 🎯 Now Run the Spring Boot App!

After Redis is installed and running:

```bash
cd /Users/dipkothari/Project/redis-cache-example
./gradlew bootRun
```

Then open: **http://localhost:8080/swagger-ui.html**

---

## 🛠️ Troubleshooting

### Redis won't start?

```bash
# Check if Redis is already running
brew services list | grep redis

# Stop and restart
brew services stop redis
brew services start redis
```

### Port 6379 already in use?

```bash
# Find what's using port 6379
lsof -i :6379

# Kill the process (replace PID with actual number)
kill -9 <PID>
```

### Can't connect to Redis?

```bash
# Check Redis status
brew services info redis

# Check Redis logs
tail -f /usr/local/var/log/redis.log
```

---

## ✅ Quick Checklist

- [ ] Install Redis: `brew install redis`
- [ ] Start Redis: `brew services start redis`
- [ ] Verify: `redis-cli ping` returns `PONG`
- [ ] Run app: `./gradlew bootRun`
- [ ] Test: Open Swagger UI and test caching!

**You're ready to see Redis magic! 🚀**
