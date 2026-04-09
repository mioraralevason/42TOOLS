const express = require('express');
const router = express.Router();
const entityController = require('../controllers/entity.controller');

// Create an entity
router.post('/', entityController.createEntity);

// Get all entities
router.get('/', entityController.getAllEntities);

// Get entity by ID
router.get('/:id', entityController.getEntityById);

// Update an entity
router.put('/:id', entityController.updateEntity);

// Delete an entity
router.delete('/:id', entityController.deleteEntity);

module.exports = router;
