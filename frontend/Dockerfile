# Build stage
FROM node:20-slim AS builder
WORKDIR /app

# Set production environment for build
ENV NODE_ENV=production

# Copy package metadata first for efficient caching
COPY package*.json ./

# Install dependencies (use ci if lockfile exists)
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

# Copy source and build
COPY . .
RUN npm run build --if-present

# Production stage
FROM nginx:stable-alpine AS production

# Remove default html
RUN rm -rf /usr/share/nginx/html/*

# Copy built frontend
COPY --from=builder /app/dist /usr/share/nginx/html

# Custom nginx config for SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]