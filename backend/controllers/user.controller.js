const pool = require('../db');
const bcrypt = require('bcryptjs');

// Create
exports.createUser = async (req, res) => {
  const { username, email, password } = req.body;
  if (!username || !email || !password) {
    return res.status(400).json({ error: 'Username, email, and password are required.' });
  }

  try {
    const hashedPassword = await bcrypt.hash(password, 10);
    const result = await pool.query(
      'INSERT INTO "user" (username, email, password) VALUES ($1, $2, $3) RETURNING id, username, email, created_at',
      [username, email, hashedPassword]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    if (err.code === '23505') { 
        return res.status(409).json({ error: 'User with this username or email already exists.' });
    }
    res.status(500).json({ error: 'Server error' });
  }
};

// Get all
exports.getAllUsers = async (req, res) => {
  try {
    const result = await pool.query('SELECT id, username, email, created_at FROM "user"');
    res.status(200).json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Get  by ID
exports.getUserById = async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query('SELECT id, username, email, created_at FROM "user" WHERE id = $1', [id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User not found' });
    }
    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Update
exports.updateUser = async (req, res) => {
  const { id } = req.params;
  const { username, email, password } = req.body;
  let hashedPassword;

  try {
    const user = await pool.query('SELECT * FROM "user" WHERE id = $1', [id]);
    if (user.rows.length === 0) {
        return res.status(404).json({ error: 'User not found' });
    }

    let updateFields = [];
    let queryParams = [];
    let paramCount = 1;

    if (username) {
      updateFields.push(`username = $${paramCount++}`);
      queryParams.push(username);
    }
    if (email) {
      updateFields.push(`email = $${paramCount++}`);
      queryParams.push(email);
    }
    if (password) {
      hashedPassword = await bcrypt.hash(password, 10);
      updateFields.push(`password = $${paramCount++}`);
      queryParams.push(hashedPassword);
    }

    if (updateFields.length === 0) {
      return res.status(400).json({ error: 'No fields to update.' });
    }

    updateFields.push(`updated_at = CURRENT_TIMESTAMP`);
    queryParams.push(id); 

    const query = `UPDATE "user" SET ${updateFields.join(', ')} WHERE id = $${paramCount} RETURNING id, username, email, created_at`;
    const result = await pool.query(query, queryParams);

    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    if (err.code === '23505') { 
        return res.status(409).json({ error: 'User with this username or email already exists.' });
    }
    res.status(500).json({ error: 'Server error' });
  }
};

// Delete
exports.deleteUser = async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query('DELETE FROM "user" WHERE id = $1 RETURNING id', [id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User not found' });
    }
    res.status(204).send(); 
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};
