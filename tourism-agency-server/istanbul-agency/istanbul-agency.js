const express = require('express');
const app = express();
const port = 3000;

app.get('/rooms', (req, res) => {
  res.json({
    agency: 'Istanbul Agency',
    rooms: [
      { id: 1, name: 'Deniz Manzaralı', price: 100, available: true },
      { id: 2, name: 'Bahçe Manzaralı', price: 80, available: false }
    ]
  });
});

app.listen(port, () => {
  console.log(`Istanbul Agency server running at http://localhost:${port}`);
});
