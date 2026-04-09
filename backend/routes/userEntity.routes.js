const express = require('express');
const router = express.Router();
const userEntityController = require('../controllers/userEntity.controller');


router.post('/', userEntityController.createUserEntity);

// Get All
router.get('/', userEntityController.getAllUserEntities);

// Getby ID
router.get('/:id', userEntityController.getUserEntityById);

// Update an asso
router.put('/:id', userEntityController.updateUserEntity);

// Delete 
router.delete('/:id', userEntityController.deleteUserEntity);

module.exports = router;
