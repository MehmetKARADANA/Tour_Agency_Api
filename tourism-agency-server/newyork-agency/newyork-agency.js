const express = require('express');
const app = express();
const port = 3002;

app.get('/rooms', (req, res) => {
  res.json({
    agency: 'New York Agency',
    rooms: [
      { id: 1, name: 'Penthouse Suite', price: 500, available: true },
      { id: 2, name: 'Deluxe Room', price: 200, available: true },
      { id: 3, name: 'Standard Room', price: 100, available: true }
    ]
  });
});

app.listen(port, () => {
  console.log(`New York Agency server running at http://localhost:${port}`);
});
