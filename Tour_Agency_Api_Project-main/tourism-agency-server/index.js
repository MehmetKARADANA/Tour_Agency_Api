const express = require('express');
const yaml = require('js-yaml');
const fs = require('fs');

const app = express();
const port = 4000;

// YAML dosyasını oku
let config;
try {
  config = yaml.load(fs.readFileSync('config.yaml', 'utf8'));
  console.log('Config:', config);
} catch (e) {
  console.log(e);
}

// Sunucu yapılandırmalarını yazdır (isteğe bağlı)
//console.log(config.servers);

// Ana rota için basit bir yanıt
app.get('/', (req, res) => {
  res.send('Merhaba, dünya!');
});                                             

// Oda bilgilerini dönen bir örnek rota
app.get('/rooms', async (req, res) => {
  const responses = await Promise.all(
    config.servers.map(async (server) => {
      // Simüle edilmiş gecikme süresi
      const delay = (server.distance_km / 10) * 10; // ms cinsinden gecikme
      await new Promise(resolve => setTimeout(resolve, delay));

      // Simüle edilmiş JSON cevabı
      return {
        agency: server.name,
        rooms: [
          { id: 1, name: 'Deniz Manzaralı', price: 100, available: true },
          { id: 2, name: 'Bahçe Manzaralı', price: 80, available: false },
        ]
      };
    })
  );

  res.json(responses);
});

// Sunucuyu başlat
app.listen(port, () => {
  console.log(`Sunucu http://localhost:${port} adresinde çalışıyor.`);
});
