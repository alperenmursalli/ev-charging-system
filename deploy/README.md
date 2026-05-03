# EV Charging System Deployment

## First-time VPS setup

```bash
sudo apt update
sudo apt install -y docker.io docker-compose nginx
sudo systemctl enable docker nginx
sudo systemctl start docker nginx
```

Clone the project:

```bash
sudo mkdir -p /opt/ev-charging-system
sudo chown "$USER":"$USER" /opt/ev-charging-system
git clone <REPO_URL> /opt/ev-charging-system
cd /opt/ev-charging-system
cp .env.example .env
nano .env
```

Start the app:

```bash
docker-compose up -d --build
docker-compose logs -f evsystem
```

## Nginx

For IP-based publishing:

```bash
sudo cp deploy/nginx-evsystem.conf /etc/nginx/sites-available/evsystem
sudo ln -sf /etc/nginx/sites-available/evsystem /etc/nginx/sites-enabled/evsystem
sudo nginx -t
sudo systemctl reload nginx
```

For a domain, edit `server_name _;` in `/etc/nginx/sites-available/evsystem`:

```nginx
server_name example.com www.example.com;
```

Then enable HTTPS:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d example.com -d www.example.com
```

## Updating

```bash
APP_DIR=/opt/ev-charging-system bash deploy/deploy.sh
```

## Required Oracle Cloud ingress rules

Open TCP ports:

- `80`
- `443`
- `8081` only if you want to access the app directly without Nginx
