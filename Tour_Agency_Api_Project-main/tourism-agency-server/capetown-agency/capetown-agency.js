const express = require('express');
const app = express();
const port = 3006;

app.get('/rooms', (req, res) => {
  res.json({
    agency: 'Cape Town Agency',
    rooms: [
      { id: 1, name: 'Table Mountain Suite', price: 350, available: true },
      { id: 2, name: 'Sea View Room', price: 200, available: true },
      { id: 3, name: 'Budget Room', price: 85, available: true }
    ]
  });
});

app.listen(port, () => {
  console.log(`Cape Town Agency server running at http://localhost:${port}`);
});
