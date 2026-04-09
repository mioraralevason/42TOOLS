const pool = require('../db');

// Associate 
exports.createUserEntity = async (req, res) => {
  const { userId, entityId } = req.body;
  if (!userId || !entityId) {
    return res.status(400).json({ error: 'User ID and Entity ID are required.' });
  }
  try {
    const result = await pool.query(
      'INSERT INTO user_entity (user_id, entity_id) VALUES ($1, $2) RETURNING *',
      [userId, entityId]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    if (err.code === '23505') { 
        return res.status(409).json({ error: 'This user-entity association already exists.' });
    }
    if (err.code === '23503') {
        return res.status(404).json({ error: 'User or Entity not found.' });
    }
    res.status(500).json({ error: 'Server error' });
  }
};

// Get all
exports.getAllUserEntities = async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM user_entity');
    res.status(200).json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Get by ID
exports.getUserEntityById = async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query('SELECT * FROM user_entity WHERE id = $1', [id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User-Entity association not found' });
    }
    res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};

// Update
exports.updateUserEntity = async (req, res) => {
    const { id } = req.params;
    const { userId, entityId } = req.body; 
    
    if (!userId && !entityId) {
        return res.status(400).json({ error: 'At least one of userId or entityId must be provided for update.' });
    }

    let updateFields = [];
    let queryParams = [];
    let paramCount = 1;

    if (userId) {
        updateFields.push(`user_id = $${paramCount++}`);
        queryParams.push(userId);
    }
    if (entityId) {
        updateFields.push(`entity_id = $${paramCount++}`);
        queryParams.push(entityId);
    }

    updateFields.push(`updated_at = CURRENT_TIMESTAMP`);
    queryParams.push(id); 

    try {
        const query = `UPDATE user_entity SET ${updateFields.join(', ')} WHERE id = $${paramCount} RETURNING *`;
        const result = await pool.query(query, queryParams);
        
        if (result.rows.length === 0) {
            return res.status(404).json({ error: 'User-Entity association not found' });
        }
        res.status(200).json(result.rows[0]);

    } catch (err) {
        console.error(err);
        if (err.code === '23505') { 
            return res.status(409).json({ error: 'This user-entity association already exists.' });
        }
        if (err.code === '23503') { 
            return res.status(404).json({ error: 'User or Entity not found.' });
        }
        res.status(500).json({ error: 'Server error' });
    }
};


// Delete
exports.deleteUserEntity = async (req, res) => {
  const { id } = req.params;
  try {
    const result = await pool.query('DELETE FROM user_entity WHERE id = $1 RETURNING *', [id]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User-Entity association not found' });
    }
    res.status(204).send(); 
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
};
