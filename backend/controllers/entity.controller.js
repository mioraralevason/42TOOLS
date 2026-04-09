const pool = require('../db');

// Create an entity
exports.createEntity = async (req, res) => {
  const { name } = req.body;
  if (!name) {
    return res.status(400).json({ error: 'Entity name is required.' });
  }
  try {
    const result = await pool.query(
      'INSERT INTO entity (name) VALUES ($1) RETURNING *',
      [name]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Get all entities
exports.getAllEntities = async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM entity');
    res.status(200).json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Get entity by ID
exports.getEntityById = async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query('SELECT * FROM entity WHERE id = $1', [id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Entity not found' });
    }
    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Update an entity
exports.updateEntity = async (req, res) => {
  const { id } = req.params;
  const { name } = req.body;
  if (!name) {
    return res.status(400).json({ error: 'Entity name is required.' });
  }
  try {
    const result = await pool.query(
      'UPDATE entity SET name = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 RETURNING *',
      [name, id]
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Entity not found' });
    }
    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Delete an entity
exports.deleteEntity = async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query('DELETE FROM entity WHERE id = $1 RETURNING *', [id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Entity not found' });
    }
    res.status(204).send(); // No content for successful deletion
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};
