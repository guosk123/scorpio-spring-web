# npm run build

server="10.3.4.246"

ssh root@${server} 'cd /opt/components/fpc-apps/fpc-manager-web/static/web-static && rm -rf *'
scp -r dist/* root@${server}:/opt/components/fpc-apps/fpc-manager-web/static/web-static
scp -r dist/index.html root@${server}:/opt/components/fpc-apps/fpc-manager-web/template


# ssh root@10.0.0.246 'cd /opt/components/fpc-apps/fpc-manager-web/static/web-static && rm -rf *'
# scp -r dist/* root@10.0.0.246:/opt/components/fpc-apps/fpc-manager-web/static/web-static
# scp -r dist/index.html root@10.0.0.246:/opt/components/fpc-apps/fpc-manager-web/template

# scp -r menu.json root@10.0.0.236:/opt/components/machloop/config/fpc-apps/resource/fpc-manager-menu.json
# ssh root@10.0.0.236 'systemctl restart fpc-manager'

# Machloop@123
#  vagrant

# tfa@123
