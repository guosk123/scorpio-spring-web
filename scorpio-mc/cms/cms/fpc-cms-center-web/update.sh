npm run build

ssh root@10.0.4.153 'cd /opt/components/fpc-apps/fpc-cms-center-web/static/web-static && rm -rf *'
scp -r dist/* root@10.0.4.153:/opt/components/fpc-apps/fpc-cms-center-web/static/web-static
scp -r dist/index.html root@10.0.4.153:/opt/components/fpc-apps/fpc-cms-center-web/template

# ssh root@10.0.0.246 'cd /opt/components/fpc-apps/fpc-manager-web/static/web-static && rm -rf *'
# scp -r dist/* root@10.0.0.246:/opt/components/fpc-apps/fpc-manager-web/static/web-static
# scp -r dist/index.html root@10.0.0.246:/opt/components/fpc-apps/fpc-manager-web/template

# scp -r menu.json root@10.0.0.236:/opt/components/machloop/config/fpc-apps/resource/fpc-manager-menu.json
# ssh root@10.0.0.236 'systemctl restart fpc-manager'

# Machloop@123
#  vagrant

# tfa@123
