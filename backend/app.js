const express = require('express');
const cors = require('cors'); // Import cors
require('dotenv').config();
const pool = require('./db'); // Import the PostgreSQL pool
const entityRoutes = require('./routes/entity.routes');
const userRoutes = require('./routes/user.routes');
const userEntityRoutes = require('./routes/userEntity.routes');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware to parse JSON bodies
app.use(express.json());
// Use cors middleware
app.use(cors());

// Test DB connection
pool.query('SELECT NOW()', (err, res) => {
  if (err) {
    console.error('Error connecting to the database:', err.stack);
  } else {
    console.log('Connected to PostgreSQL at:', res.rows[0].now);
  }
});

// Use routes
app.use('/entities', entityRoutes);
app.use('/users', userRoutes);
app.use('/user-entities', userEntityRoutes);

// Basic route
app.get('/', (req, res) => {
  res.send('Welcome to the 42TOOLS API!');
});

// Start the server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
