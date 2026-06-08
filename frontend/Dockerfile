FROM node:22-alpine
WORKDIR /app

COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .

EXPOSE 5173

# Run Vite in host mode so it's accessible outside the container
CMD ["npm", "run", "dev", "--", "--host"]
