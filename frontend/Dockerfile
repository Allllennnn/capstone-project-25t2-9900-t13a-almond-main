# frontend/Dockerfile

# ── Stage 1: Install dependencies ──
FROM node:18-alpine AS deps
WORKDIR /app

# Copy dependency manifests (package.json + lockfile) first to leverage Docker layer caching
COPY package.json package-lock.json ./
# As long as these two files don’t change, dependencies won’t be re-downloaded
RUN npm ci    # or use: yarn install --frozen-lockfile

# ── Stage 2: Build the project ──
FROM node:18-alpine AS build
WORKDIR /app
# Reuse node_modules from deps stage
COPY --from=deps /app/node_modules ./node_modules
# Copy source code and build
COPY . .
RUN npm run build

# ── Stage 3: Runtime ──
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx","-g","daemon off;"]
