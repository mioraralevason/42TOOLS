const express = require('express');
const router = express.Router();
const userEntityController = require('../controllers/userEntity.controller');

// Associate an entity to a user
router.post('/', userEntityController.createUserEntity);

// Get all associations
router.get('/', userEntityController.getAllUserEntities);

// Get association by ID
router.get('/:id', userEntityController.getUserEntityById);

// Update an association
router.put('/:id', userEntityController.updateUserEntity);

// Delete an association
router.delete('/:id', userEntityController.deleteUserEntity);

module.exports = router;
