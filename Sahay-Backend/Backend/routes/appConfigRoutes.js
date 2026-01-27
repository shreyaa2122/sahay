const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/appConfigController');

// Routes for Managing Apps
router.get('/', ctrl.getAllApps);       // Get list of all apps
router.post('/', ctrl.addApp);         // Add a new app
router.delete('/:id', ctrl.removeApp); // Delete an app by ID

module.exports = router;