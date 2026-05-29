# BingoCV Worker 说明

## 目录用途

`bingocv-worker/` 目录是前端构建输出目录，用于存放 Vite 构建后的静态文件。

## 当前状态

- **当前后端**: Spring Boot (位于项目根目录的 `src/` 和 `pom.xml`)
- **构建输出**: 前端代码构建后输出到 `bingocv-worker/dist/`
- **配置位置**: `bingocv-web/vite.config.js` 中的 `VITE_OUT_DIR` 环境变量

## 未来规划

### Cloudflare Worker 版本

如果后续要开发 Cloudflare Worker 版本，可以：

1. **保持当前结构**
   - `bingocv-worker/dist/` 继续作为前端构建输出
   - 在 `bingocv-worker/` 下创建 `worker/` 目录存放 CF Worker 代码

2. **推荐的项目结构**
   ```
   bingoCV/
   ├── bingocv-web/           # Vue3 前端源码
   ├── bingocv-worker/        # Cloudflare 部署相关
   │   ├── dist/              # 前端构建输出（静态文件）
   │   ├── worker/            # CF Worker 后端代码
   │   └── wrangler.toml      # CF Worker 配置
   ├── src/                   # Spring Boot 后端（当前使用）
   └── pom.xml                # Maven 配置
   ```

3. **部署方式**
   - **Spring Boot 版本**: 传统服务器部署（JAR包）
   - **CF Worker 版本**: 使用 `wrangler deploy` 部署到 Cloudflare

## 注意事项

- ✅ **保留此目录**: 即使当前不使用 CF Worker，也应保留作为构建输出目录
- ✅ **不要删除**: `bingocv-web/vite.config.js` 配置了输出到此目录
- 📝 **未来扩展**: 如需开发 CF Worker 版本，可在此目录下添加相关代码

## 相关配置

### 前端构建配置

```javascript
// bingocv-web/vite.config.js
build: {
    outDir: env.VITE_OUT_DIR || 'dist',  // 默认输出到 dist
    // 通过 .env.release 配置为 ../bingocv-worker/dist
}
```

### 环境变量

```bash
# bingocv-web/.env.release
VITE_OUT_DIR = ../bingocv-worker/dist
```
