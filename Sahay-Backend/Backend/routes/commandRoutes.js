const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/commandController');

// Voice Command Routes
router.post('/process', ctrl.handleCommand);
router.get('/history', ctrl.getHistory);
router.get('/stats', ctrl.getStats);
router.delete('/clear', ctrl.clearAll);
router.delete('/history/:id', ctrl.deleteCommand);

module.exports = router;